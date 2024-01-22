package controller;

import map.*;
import model.export.ExportController;
import exception.*;
import utils.*;
import util.*;
import annotation.*;
import verb.VerbAction;
import validation.*;
import guard.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Date;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletResponse;


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
        System.out.println("contenuuu====>"+contentType);
        
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            System.out.println("contenuuu====>"+uploadDirectory);
            request.setAttribute("uploadDirectory", uploadDirectory);
        }
        processRequest(request, response);
    }
    
    private String extractUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return "";
        }
    
        int lastSlashIndex = fullUrl.lastIndexOf("/");
        if (lastSlashIndex == -1 || lastSlashIndex == fullUrl.length() - 1) {
            return ""; 
        }
        
        System.out.println("extract: " + fullUrl.substring(lastSlashIndex + 1));
        return fullUrl.substring(lastSlashIndex + 1);
    }      

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            System.out.println("##### context path: " + request.getContextPath());
            System.out.println("####### GET URI: " + request.getRequestURI());
            String url = request.getContextPath() + "/" + extractUrl(request.getRequestURI());
            System.out.println("URL====>"+url);
            String httpMethod = request.getMethod();
            System.out.println("methodd===>"+httpMethod);
            
            Mapping mapping = urlMapping.get(url);
            System.out.println("url fenooooo");
            System.out.println("urlMapping===>"+urlMapping);
            System.out.println("URL====>"+url);
            System.out.println("mappping==>"+mapping);
            if (mapping == null) {
                throw new RequestException("URL non trouvée : " + url);
            }
            
            String methodName = mapping.getMethodName(httpMethod);
            if (methodName == null && httpMethod.equals("GET")) {
                methodName = mapping.getMethodName("DEFAULT");
            }
            if (methodName == null) {
                out.println("<h1>Méthode HTTP non autorisée pour cette URL ===>:"+url+"ayant comme Verb =: "+httpMethod+"</h1>");
                return;
            }
            
            Object result = invokeControllerMethod(request, response, mapping, httpMethod);

            // Vérifier si un export est demandé via ExportController
            ExportController.exportResponse(result, request, response);

            // Si pas d’export, traiter normalement
            if (request.getParameter("export") == null) {
                UtilController.handleControllerResult(result, request, response, out, mapping, httpMethod);
            }
            
        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
    
    private Object invokeControllerMethod(HttpServletRequest request, HttpServletResponse response, Mapping mapping, String httpMethod) throws Exception {
        if (request.getAttribute("validationInProgress") != null) {
            return null;
        }
        
        try {
            Class<?> controllerClass = Class.forName(mapping.getClassName());
            Object controllerInstance = UtilController.createInstance(mapping.getClassName());
            Method method = UtilController.getControllerMethod(mapping, controllerClass, httpMethod);
            
            // Vérification de l'authentification
            if (!AuthGuard.checkAuthentication(request, response, controllerClass, method)) {
                return null;
            }
            
            // Détection si la requête est un upload de fichier
            String contentType = request.getContentType();
            boolean isMultipart = contentType != null && contentType.startsWith("multipart/form-data");
            Object[] params;
            
            if (isMultipart) {
                // Pour les uploads de fichiers
                params = UtilParameter.getMethodParameter(request, method);
            } else {
                // Pour les autres types de requêtes
                params = UtilParameter.getMethodParameters(request, response, method);
            }
            
            for (int i = 0; i < method.getParameters().length; i++) {
                if (method.getParameters()[i].isAnnotationPresent(ModelAttribute.class) && params[i] != null) {
                    System.out.println("Param " + i + ": " + params[i]);
                    ValidationErrors errors = Validator.validate(params[i]);
                    
                    if (errors.hasErrors()) {
                        Map<String, List<String>> fieldErrors = errors.getAllErrors();
                        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
                            String fieldName = entry.getKey();
                            List<String> errorMessages = entry.getValue();
                            request.setAttribute("error_" + fieldName, errorMessages);
                        }
                        
                        request.setAttribute("validationInProgress", true);
                        request.setAttribute("validationErrors", errors);
                        request.setAttribute("formData", params[i]);
                        
                        System.err.println("Validation Errors:");
                        errors.getAllErrors().forEach((field, messages) -> {
                            System.err.println("Field: " + field + ", Errors: " + messages);
                        });
                        
                        request.getRequestDispatcher("user/formulaire.jsp").forward(request, response);
                        return null;
                    }
                }
            }
            
            return method.invoke(controllerInstance, params);
            
        } catch (Exception e) {
            System.err.println("An error occurred during method invocation:");
            e.printStackTrace();
            throw e;
        }
    }
    private void handleException(HttpServletResponse response, int statusCode, Exception e) throws IOException {
        e.printStackTrace();
        response.sendError(statusCode, e.getMessage());
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }
}