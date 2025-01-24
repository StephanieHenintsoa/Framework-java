package utils;
import validation.*;
import java.util.HashMap;
import java.util.Map;

public class ModelView {
    private String url;
    private HashMap<String, Object> data;
    private ValidationErrors validationErrors;

    public ModelView() {
        this.url = "";
        this.data = new HashMap<>();
        this.validationErrors = new ValidationErrors();
    }

    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public void addObject(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }

    public void removeData(String key) {
        this.data.remove(key);
    }

    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }

    public int dataSize() {
        return this.data.size();
    }
   
    public void setValidationErrors(ValidationErrors validationErrors) {
        this.validationErrors = validationErrors;
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }

    public boolean hasErrors() {
        return validationErrors != null && validationErrors.hasErrors();
    }

    @Override
    public String toString() {
        return "ModelView{" +
                "url='" + url + '\'' +
                ", data=" + data +
                '}';
    }

    public static void main(String[] args) {
        ModelView modelView = new ModelView();
        modelView.setUrl("http://example.com");
        modelView.addObject("key1", "value1");
        modelView.addObject("key2", 123);

        System.out.println(modelView);
    }
}
