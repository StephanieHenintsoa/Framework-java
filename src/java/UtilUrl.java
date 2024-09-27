package util;
import annotation.*;
import map.*;
import exception.*;

import java.lang.reflect.Method;
import java.util.HashMap;

public class UtilUrl {
    public HashMap<String, Mapping> urlMapping = new HashMap<>();


    public static String getUrlFromMethod(Method method) {
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

    public static void validateUrlMapping(String url,HashMap<String, Mapping> urlMapping  ) throws RequestException {
        if (urlMapping.containsKey(url)) {
            throw new RequestException("Duplication d'URL détectée pour : " + url);
        }
    }
        
}
