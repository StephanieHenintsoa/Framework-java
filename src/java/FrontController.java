package controller;

import map.*;
import exception.*;
import utils.*;
import util.*;
import annotation.*;
import verb.VerbAction;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Date;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class FrontController extends HttpServlet {
    private String namePackage;
    private String nameProject;
    private ControllerScanner scanner;
    private List<Class<?>> controllers;
    private HashMap<String, Mapping> urlMapping = new HashMap<>();
    private String uploadDirectory;

    @Override
    public void init(ServletConfig configurer) throws ServletException {
        super.init(configurer);
        try {
            ServletContext context = configurer.getServletContext();

            namePackage = context.getInitParameter("package-controller");
            nameProject = context.getInitParameter("name-project");
            uploadDirectory = context.getInitParameter("upload-directory");

            if (uploadDirectory == null) {
                uploadDirectory = context.getRealPath("/WEB-INF/uploads");
            }

            java.io.File uploadDir = new java.io.File(uploadDirectory);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            UtilPackage.validatePackage(namePackage);
            
            this.scanner = new ControllerScanner();
            this.controllers = this.scanner.findControllers(namePackage);
            UtilController.validateControllers(controllers, namePackage);
            mapControllers();
        } catch (BuildException | RequestException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mapControllers() throws Exception {
        for (Class<?> controllerClass : controllers) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(URL.class)) {
                    String url = method.getAnnotation(URL.class).value();
                    Mapping mapping = urlMapping.computeIfAbsent(url, k -> new Mapping(controllerClass.getName()));
    
                    if (method.isAnnotationPresent(Annotation_Get.class)) {
                        mapping.addVerbAction("GET", method.getName());
                    } else if (method.isAnnotationPresent(Annotation_Post.class)) {
                        mapping.addVerbAction("POST", method.getName());
                    } else if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping rm = method.getAnnotation(RequestMapping.class);
                        String httpMethod = rm.method();
                        String mappingUrl = rm.value();
                        
                        if (!mappingUrl.isEmpty()) {
                            url = mappingUrl;
                            mapping = urlMapping.computeIfAbsent(url, k -> new Mapping(controllerClass.getName()));
                        }
                        
                        mapping.addVerbAction(httpMethod, method.getName());
                    } else {
                        mapping.addVerbAction("GET", method.getName());
                    }
    
                    UtilReturnType.validateReturnType(method, controllerClass);
                    UtilReturnType.validateMethodAnnotations(method);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            request.setAttribute("uploadDirectory", uploadDirectory);
        }
        processRequest(request, response);
    }

    private String extractUrl(String fullUrl) {
        String contextPath = "/" + nameProject;
        if (fullUrl.startsWith(contextPath)) {
            return fullUrl.substring(contextPath.length());
        }
        return fullUrl;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String url = extractUrl(request.getRequestURI());
            String httpMethod = request.getMethod();
            
            Mapping mapping = urlMapping.get(url);
            if (mapping == null) {
                throw new RequestException("URL non trouvée : " + url);
            }
            
            String methodName = mapping.getMethodName(httpMethod);
            if (methodName == null && httpMethod.equals("GET")) {
                methodName = mapping.getMethodName("DEFAULT");
            }
            if (methodName == null) {
                out.println("<h1>Méthode HTTP non autorisée pour cette URL:"+url+"ayant comme Verb : "+httpMethod+"</h1>");
                return;
            }
            
            Object result = invokeControllerMethod(request, mapping, httpMethod);
            UtilController.handleControllerResult(result, request, response, out, mapping, httpMethod);
            
        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
 
    private Object invokeControllerMethod(HttpServletRequest request, Mapping mapping, String httpMethod) throws Exception {
        Class<?> controllerClass = Class.forName(mapping.getClassName());
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
    
        for (Field field : controllerClass.getDeclaredFields()) {
            if (field.getType().equals(Session.class)) {
                field.setAccessible(true);
                field.set(controllerInstance, new Session(request.getSession()));
            }
        }
        Method method = UtilController.getControllerMethod(mapping, controllerClass, httpMethod);
        Object[] params = UtilParameter.getMethodParameters(request, method);
        return method.invoke(controllerInstance, params);
    }

    private void handleException(HttpServletResponse response, int statusCode, Exception e) throws IOException {
        e.printStackTrace();
        response.sendError(statusCode, e.getMessage());
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }
}