package util;

import utils.*;
import exception.*;
import annotation.*;
import map.*;
import verb.VerbAction;

import javax.servlet.ServletException;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UtilController {

    public static void validateControllers(List<Class<?>> controllers, String namePackage) throws BuildException {
        if (controllers == null || controllers.isEmpty()) {
            throw new BuildException("Aucun contrôleur trouvé dans le package spécifié : " + namePackage);
        }
    }

   

    public static void forwardToView(ModelView modelView, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String viewUrl = modelView.getUrl();
        HashMap<String, Object> data = modelView.getData();

        for (String key : data.keySet()) {
            request.setAttribute(key, data.get(key));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
        dispatcher.forward(request, response);
    }

    public static void handleControllerResult(Object result, HttpServletRequest request, HttpServletResponse response, PrintWriter out, Mapping mapping, String httpMethod)
            throws Exception, ServletException, RequestException {
        
        Method method = UtilController.getControllerMethodByMapping(mapping, httpMethod);
        boolean isRestApi = method.isAnnotationPresent(Restapi.class);
        
        if (isRestApi) {
            response.setContentType("application/json");
            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                String jsonData = new Gson().toJson(modelView.getData());
                out.print(jsonData);
            } else {
                String jsonResult = new Gson().toJson(result);
                out.print(jsonResult);
            }
        } else {
            if (result instanceof String) {
                out.println("<h3>Résultat: " + result + "</h3>");
            } else if (result instanceof ModelView) {
                forwardToView((ModelView) result, request, response);
            } else {
                throw new RequestException("Type de résultat non reconnu");
            }
        }  
    }
    
    public static Constructor<?> getDeclaredConstructor(String className, Class<?>... parameterTypes) 
            throws ClassNotFoundException, NoSuchMethodException {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            
            if (!constructor.canAccess(null)) {
                constructor.setAccessible(true);
            }
            
            return constructor;
        } catch (ClassNotFoundException e) {
            System.err.println("Classe non trouvée : " + className);
            throw e;
        } catch (NoSuchMethodException e) {
            System.err.println("Constructeur avec les paramètres spécifiés non trouvé pour la classe : " + className);
            throw e;
        } catch (SecurityException e) {
            System.err.println("Erreur de sécurité lors de l'accès au constructeur : " + e.getMessage());
            throw e;
        }
    }


    
    public static Object createInstance(String className) throws Exception {
        try {
            // Charge la classe à partir de son nom complet
            Class<?> clazz = Class.forName(className);
            
            // Récupère le constructeur par défaut
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            
            // Rend le constructeur accessible si nécessaire (cas des constructeurs privés)
            if (!constructor.canAccess(null)) {
                constructor.setAccessible(true);
            }
            
            // Crée et retourne une nouvelle instance
            return constructor.newInstance();
            
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Classe non trouvée - " + className);
            throw new Exception("Impossible de trouver la classe " + className, e);
            
        } catch (NoSuchMethodException e) {
            System.err.println("Erreur: Constructeur par défaut non trouvé pour " + className);
            throw new Exception("Constructeur par défaut non trouvé pour " + className, e);
            
        } catch (SecurityException e) {
            System.err.println("Erreur: Problème de sécurité lors de l'accès au constructeur - " + className);
            throw new Exception("Problème de sécurité lors de l'accès au constructeur de " + className, e);
            
        } catch (InstantiationException e) {
            System.err.println("Erreur: Impossible d'instancier la classe " + className);
            throw new Exception("Impossible d'instancier la classe " + className, e);
            
        } catch (IllegalAccessException e) {
            System.err.println("Erreur: Accès illégal au constructeur - " + className);
            throw new Exception("Accès illégal au constructeur de " + className, e);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur: Arguments invalides pour le constructeur - " + className);
            throw new Exception("Arguments invalides pour le constructeur de " + className, e);
            
        } catch (InvocationTargetException e) {
            System.err.println("Erreur: Exception levée par le constructeur - " + className);
            throw new Exception("Exception levée par le constructeur de " + className, e);
        }
    }
    
    public static Method getControllerMethod(Mapping mapping, Class<?> controllerClass, String httpMethod) throws Exception {
        for (Method method : controllerClass.getDeclaredMethods()) {
            System.out.println("Checking method: " + method.getName());
            System.out.println("mappibg=====>"+mapping.getMethodName(httpMethod));
            if (method.getName().equals(mapping.getMethodName(httpMethod))) {
                System.out.println("rreturn method==>"+method.getName());
                return method;
            }
        }
        throw new NoSuchMethodException(controllerClass.getName() + "." + mapping.getMethodName(httpMethod));
    }
    public static Method getControllerMethodByMapping(Mapping mapping, String httpMethod) throws Exception {
        Class<?> controllerClass = Class.forName(mapping.getClassName());
        System.out.println("controller name: " + controllerClass.getName());
        String methodName = mapping.getMethodName(httpMethod);
        System.out.println("getController httpMethod==>" + httpMethod);
        System.out.println("getController Mapping==>" + methodName);
        
        if (methodName == null) {
            throw new NoSuchMethodException("Aucune méthode trouvée pour la méthode HTTP : " + httpMethod);
        }
    
        //miraraha arguments
        for (Method method : controllerClass.getDeclaredMethods()) {
            // Vérifier si c'est la bonne méthode par son nom
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        
        throw new NoSuchMethodException("Méthode " + methodName + " non trouvée dans " + controllerClass.getName());
    }
    
}