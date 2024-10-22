package util;
import utils.*;
import file.*;
import annotation.*;
import exception.*;
import file.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

public class UtilParameter {
    
    public static Object getModelAttribute(HttpServletRequest request, Class<?> paramType, ModelAttribute modelAttribute) throws Exception {
        Object modelAttributeInstance = paramType.getDeclaredConstructor().newInstance();
        for (java.lang.reflect.Field field : paramType.getDeclaredFields()) {
            String fieldName = field.getName();
            String fieldValue = request.getParameter(fieldName);
            field.setAccessible(true);
            field.set(modelAttributeInstance, UtilParameter.convertParameter(fieldValue, field.getType()));
        }
        return modelAttributeInstance;
    }
    
    public static Object[] getMethodParameters(HttpServletRequest request, Method method) throws Exception {
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

    public static Object getParameter(HttpServletRequest request, Annotation[] annotations, Class<?> paramType) 
        throws Exception 
    {
   
        if (paramType.equals(FileUpload.class)) {
            String paramName = "file";
            
            // Chercher si un nom spécifique est défini via @RequestParam
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestParam) {
                    paramName = ((RequestParam) annotation).name();
                    break;
                }
            }
            
            return FileUtils.processUploadedFile(request, paramName);
        }
        
        // Traitement des autres types de paramètres
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
        
        // Vérification pour le type Session
        if (paramType.equals(Session.class)) {
            System.out.println("Session");
            return new Session(request.getSession());
        }
        return null;
    }

    public static Object convertParameter(String value, Class<?> targetType) throws Exception {
        try {
            if (value == null) {
                return null;
            }
            
            if (targetType.equals(String.class)) {
                return value;
            } else if (targetType.equals(int.class) || targetType.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(boolean.class) || targetType.equals(Boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (targetType.equals(Date.class)) {
                return Date.valueOf(value);
            }
            return null;

        } catch (Exception e) {
            throw new RequestException("Le type de données insérées n'est pas compatible");
        }
    }
}