package controller;

import map.*;
import exception.*;
import utils.*;
import annotation.*;

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

            validatePackage(namePackage);
            
            this.scanner = new ControllerScanner();
            this.controllers = this.scanner.findControllers(namePackage);
            validateControllers(controllers);
            mapControllers();
        } catch (BuildException | RequestException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validatePackage(String namePackage) throws BuildException {
        if (namePackage == null || namePackage.isEmpty()) {
            throw new BuildException("Le package des contrôleurs n'est pas spécifié ou est vide.");
        }
    }

    private void validateControllers(List<Class<?>> controllers) throws BuildException {
        if (controllers.isEmpty()) {
            throw new BuildException("Aucun contrôleur trouvé dans le package spécifié : " + namePackage);
        }
    }
    private void mapControllers() throws Exception {
        for (Class<?> controllerClass : controllers) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Annotation_Get.class) || method.isAnnotationPresent(Annotation_Post.class) || method.isAnnotationPresent(RequestMapping.class)) {
                    String url = getUrlFromMethod(method);
                    validateUrlMapping(url);
                    validateReturnType(method, controllerClass);
                    try {
                        validateMethodAnnotations(method); // Ajoutez cette ligne
                    } catch (BuildException e) {
                        throw new BuildException("Erreur dans le contrôleur " + controllerClass.getName() + ": " + e.getMessage());
                    }
                    urlMapping.put(url, new Mapping(controllerClass.getName(), method.getName()));
                }
            }
        }
    }

    


    private String getUrlFromMethod(Method method) {
        if (method.isAnnotationPresent(Annotation_Get.class)) {
            return method.getAnnotation(Annotation_Get.class).value();
        } 
        else if(method.isAnnotationPresent(Annotation_Post.class)){
            return method.getAnnotation(Annotation_Post.class).value();
        }
        else if(method.isAnnotationPresent(RequestMapping.class)){
            return method.getAnnotation(RequestMapping.class).value();
        } 

        return null;
    }
    
    private void validateUrlMapping(String url) throws RequestException {
        if (urlMapping.containsKey(url)) {
            throw new RequestException("Duplication d'URL détectée pour : " + url);
        }
    }

    private void validateReturnType(Method method, Class<?> controllerClass) throws RequestException {
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(String.class) && !returnType.equals(ModelView.class)) {
            throw new RequestException("La méthode " + method.getName() + " dans " + controllerClass.getName() + " a un type de retour invalide : " + returnType.getName());
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
            handleControllerResult(result, request, response, out);
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
    
        Method method = getControllerMethod(mapping, controllerClass);
        Object[] params = getMethodParameters(request, method);
        return method.invoke(controllerInstance, params);
    }

    private Method getControllerMethod(Mapping mapping, Class<?> controllerClass) throws NoSuchMethodException {
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(mapping.getMethodName())) {
                return method;
            }
        }
        throw new NoSuchMethodException("Méthode  " + mapping.getMethodName() + " non trouvée dans " + controllerClass.getName());
    }

    private Object[] getMethodParameters(HttpServletRequest request, Method method) throws Exception {
        Object[] params = new Object[method.getParameterCount()];
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        System.out.println(params.length);
        System.out.println(paramAnnotations.length);

        Class<?>[] paramTypes = method.getParameterTypes();

        for (int i = 0; i < paramAnnotations.length; i++) {
            params[i] = getParameter(request, paramAnnotations[i], paramTypes[i]);
        }

        return params;
    }

    private Object getParameter(HttpServletRequest request, Annotation[] annotations, Class<?> paramType) 
        throws Exception 
    {
        for (Annotation annotation : annotations) {
            if (annotation instanceof RequestParam) {
                String paramName = ((RequestParam) annotation).name();
                String paramValue = request.getParameter(paramName);
                return convertParameter(paramValue, paramType);
            } 
            else if (annotation instanceof ModelAttribute) {
                return getModelAttribute(request, paramType, (ModelAttribute) annotation);
            }
           
        }
         // Ajouter une vérification pour le type MySession
         if (paramType.equals(Session.class)) {
            System.out.println("Session");
            return new Session(request.getSession());
        }
        return null;
    }
    private void validateMethodAnnotations(Method method) throws BuildException {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (Annotation[] annotations : paramAnnotations) {
            boolean hasRequiredAnnotation = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestParam || annotation instanceof ModelAttribute) {
                    hasRequiredAnnotation = true;
                    break;
                }
            }
            // if (!hasRequiredAnnotation) {
            //     throw new BuildException("Annotation manquante pour un des paramètres de la méthode: " + method.getName());
            // }
        }
    }
    

    private Object getModelAttribute(HttpServletRequest request, Class<?> paramType, ModelAttribute modelAttribute) throws Exception {
        Object modelAttributeInstance = paramType.getDeclaredConstructor().newInstance();
        for (java.lang.reflect.Field field : paramType.getDeclaredFields()) {
            String fieldName = field.getName();
            String fieldValue = request.getParameter(fieldName);
            field.setAccessible(true);
            field.set(modelAttributeInstance, convertParameter(fieldValue, field.getType()));
        }
        return modelAttributeInstance;
    }

    private Object convertParameter(String value, Class<?> targetType) throws Exception {
        try {
            if (targetType.equals(String.class)) {
                return value;
            } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            }else if (targetType.equals(Date.class) || targetType.equals(Date.class)) {
                return Date.valueOf(value);
            }

            // Ajouter plus de conversions si nécessaire
            return null;
        } catch (Exception e) {
            throw new RequestException("Le type de données insérées n'est pas compatible");
        }
    }

    private void handleControllerResult(Object result, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
        throws IOException, ServletException, RequestException {
        if (result instanceof String) {
            out.println("<h3>Résultat: " + result + "</h3>");
        } else if (result instanceof ModelView) {
            forwardToView((ModelView) result, request, response);
        } else {
            throw new RequestException("Type de résultat non reconnu");
        }
    }


    private void forwardToView(ModelView modelView, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            String viewUrl = modelView.getUrl();
            HashMap<String, Object> data = modelView.getData();

            for (String key : data.keySet()) {
                request.setAttribute(key, data.get(key));
            }
            RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
            dispatcher.forward(request, response);
    }

    private void handleException(HttpServletResponse response, int statusCode, Exception e) throws IOException {
        e.printStackTrace();
        response.sendError(statusCode, e.getMessage());
    }
}
