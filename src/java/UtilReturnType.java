package util;
import utils.*;
import exception.*;
import annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class UtilReturnType {

    public static void validateReturnType(Method method, Class<?> controllerClass) throws RequestException {
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(String.class) && !returnType.equals(ModelView.class)&& method.isAnnotationPresent(Restapi.class) == false) {
            throw new RequestException("La méthode " + method.getName() + " dans " + controllerClass.getName() + " a un type de retour invalide : " + returnType.getName());
        }
    }
    public static void validateMethodAnnotations(Method method) throws BuildException {
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
    
}
