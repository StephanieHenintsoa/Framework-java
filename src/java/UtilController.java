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

    public static Method getControllerMethodByMapping(Mapping mapping, String httpMethod) throws Exception {
        Class<?> controllerClass = Class.forName(mapping.getClassName());
        String methodName = mapping.getMethodName(httpMethod);
        if (methodName == null) {
            throw new NoSuchMethodException("Aucune méthode trouvée pour la méthode HTTP : " + httpMethod);
        }
        return controllerClass.getMethod(methodName);
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

    public static Method getControllerMethod(Mapping mapping, Class<?> controllerClass, String httpMethod) throws NoSuchMethodException {
        String methodName = mapping.getMethodName(httpMethod);
        if (methodName == null) {
            throw new NoSuchMethodException("No method found for HTTP method: " + httpMethod);
        }
        return controllerClass.getMethod(methodName);
    }
}