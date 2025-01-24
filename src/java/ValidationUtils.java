// Classe utilitaire pour la validation
package utils;

import annotation.*;
import validation.*;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class ValidationUtils {
    public static ValidationErrors validate(Object form) {
        ValidationErrors errors = new ValidationErrors();
        
        for (Field field : form.getClass().getDeclaredFields()) {
            ValidateField annotation = field.getAnnotation(ValidateField.class);
            if (annotation != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(form);
                    String fieldName = field.getName();
                    
                    // Validation required
                    if (annotation.required() && (value == null || value.toString().trim().isEmpty())) {
                        errors.addError(fieldName, "Le champ " + fieldName + " est obligatoire");
                        continue;
                    }
                    
                    if (value != null && value instanceof String) {
                        String strValue = (String) value;
                        
                        // Validation longueur minimum
                        if (strValue.length() < annotation.minLength()) {
                            errors.addError(fieldName, "Le champ " + fieldName + " doit contenir au moins " 
                                + annotation.minLength() + " caractères");
                        }
                        
                        // Validation longueur maximum
                        if (strValue.length() > annotation.maxLength()) {
                            errors.addError(fieldName, "Le champ " + fieldName + " ne doit pas dépasser " 
                                + annotation.maxLength() + " caractères");
                        }
                        
                        // Validation pattern regex
                        if (!annotation.pattern().isEmpty() && !Pattern.matches(annotation.pattern(), strValue)) {
                            errors.addError(fieldName, annotation.message());
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return errors;
    }
}
