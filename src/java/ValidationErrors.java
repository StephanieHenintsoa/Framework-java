package validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {
    private Map<String, List<String>> errors;

    public ValidationErrors() {
        this.errors = new HashMap<>();
    }

    public void addError(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
    }

    public List<String> getFieldErrors(String field) {
        return errors.getOrDefault(field, new ArrayList<>());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public Map<String, List<String>> getAllErrors() {
        return errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
