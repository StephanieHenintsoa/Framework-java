package controller;

import map.*;
import exception.*;
import utils.*;
import util.*;
import annotation.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Date;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Field;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

public class FrontController extends HttpServlet {
    private String namePackage;
    private String nameProject;
    private ControllerScanner scanner;
    private List<Class<?>> controllers;
    private HashMap<String, Mapping> urlMapping = new HashMap<>();

    @Override
    public void init(ServletConfig configurer) throws ServletException {
        super.init(configurer);
        try {
            ServletContext context = configurer.getServletContext();

            namePackage = context.getInitParameter("package-controller");
            nameProject = context.getInitParameter("name-project");

            UtilPackage.validatePackage(namePackage);
            
            this.scanner = new ControllerScanner();
            this.controllers = this.scanner.findControllers(namePackage);
            UtilController.validateControllers(controllers,namePackage);
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
                if (method.isAnnotationPresent(Annotation_Get.class) || method.isAnnotationPresent(Annotation_Post.class) || method.isAnnotationPresent(RequestMapping.class)) {
                    String url = UtilUrl.getUrlFromMethod(method);
                    UtilUrl.validateUrlMapping(url,urlMapping);
                    UtilReturnType.validateReturnType(method, controllerClass);
                    try {
                        UtilReturnType.validateMethodAnnotations(method); 
                    } catch (BuildException e) {
                        throw new BuildException("Erreur dans le contrôleur " + controllerClass.getName() + ": " + e.getMessage());
                    }
                    urlMapping.put(url, new Mapping(controllerClass.getName(), method.getName()));
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
        processRequest(request, response);
    }

    private String extractUrl(String fullUrl) {
        String contextPath = "/"+nameProject;
        if (fullUrl.startsWith(contextPath)) {
            return fullUrl.substring(contextPath.length());
        }

        System.out.println("full url: " + fullUrl);
        return fullUrl;
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String url = extractUrl(request.getRequestURI());
            Mapping mapping = urlMapping.get(url);
            if (mapping == null) {
                throw new RequestException("URL non trouvée : " + url);
            }
            Object result = invokeControllerMethod(request, mapping);
            UtilController.handleControllerResult(result, request, response, out,mapping);
            } catch (BuildException e) {
                handleException(response, HttpServletResponse.SC_BAD_REQUEST, e);
            } catch (RequestException e) {
                handleException(response, HttpServletResponse.SC_NOT_FOUND, e);
            } catch (Exception e) {
                handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
    }
    
    private Object invokeControllerMethod(HttpServletRequest request, Mapping mapping) throws Exception {
        Class<?> controllerClass = Class.forName(mapping.getClassName());
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
    
        // Instancier les attributs Session
        for (Field field : controllerClass.getDeclaredFields()) {
            if (field.getType().equals(Session.class)) {
                field.setAccessible(true);
                field.set(controllerInstance, new Session(request.getSession()));
            }
        }
        Method method = UtilController.getControllerMethod(mapping, controllerClass);
        Object[] params = UtilParameter.getMethodParameters(request, method);
        return method.invoke(controllerInstance, params);
    }
    private void handleException(HttpServletResponse response, int statusCode, Exception e) throws IOException {
        e.printStackTrace();
        response.sendError(statusCode, e.getMessage());
    }
}