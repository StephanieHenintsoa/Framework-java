package util;
import annotation.*;
import map.*;
import exception.*;
import verb.*;

import java.lang.reflect.Method;
import java.util.HashMap;

public class UtilUrl {
    public HashMap<String, Mapping> urlMapping = new HashMap<>();

    public static String getUrlFromMethod(Method method) throws RequestException {
        if (method.isAnnotationPresent(URL.class)) {
            if (method.isAnnotationPresent(Annotation_Get.class) || method.isAnnotationPresent(Annotation_Post.class)) {
                return method.getAnnotation(URL.class).value();
            } 
        }
        else if (method.isAnnotationPresent(RequestMapping.class)) {
            return method.getAnnotation(URL.class).value();
        }
        throw new RequestException("Aucune annotation d'URL valide trouvée pour la méthode");
    }

    public static void validateUrlMapping(String url, HashMap<String, Mapping> urlMapping) throws RequestException {
        if (urlMapping.containsKey(url)) {
            throw new RequestException("Duplication d'URL détectée pour : " + url);
        }
    }
}