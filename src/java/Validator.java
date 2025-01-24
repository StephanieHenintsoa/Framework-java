package validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static ValidationErrors validate(Object object) {
        ValidationErrors errors = new ValidationErrors();
        
        if (object == null) {
            return errors;
        }

        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            validateField(object, field, errors);
        }

        return errors;
    }

    private static void validateField(Object object, Field field, ValidationErrors errors) {
        try {
            Object value = field.get(object);

            System.out.println("Validating field: " + field.getName() + ", value: " + value);

            if (field.isAnnotationPresent(annotation.NotNull.class)) {
                annotation.NotNull notNull = field.getAnnotation(annotation.NotNull.class);
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    errors.addError(field.getName(), notNull.message());
                }
            }

            if (field.isAnnotationPresent(annotation.Size.class) && value != null) {
                annotation.Size size = field.getAnnotation(annotation.Size.class);
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.length() < size.min() || strValue.length() > size.max()) {
                        String message = size.message()
                            .replace("{min}", String.valueOf(size.min()))
                            .replace("{max}", String.valueOf(size.max()));
                        errors.addError(field.getName(), message);
                    }
                }
            }

            if (field.isAnnotationPresent(annotation.Email.class) && value != null) {
                annotation.Email email = field.getAnnotation(annotation.Email.class);
                if (value instanceof String && !EMAIL_PATTERN.matcher((String) value).matches()) {
                    errors.addError(field.getName(), email.message());
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
