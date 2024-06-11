package map;

import exception.RequestException;
import utils.*;

import java.lang.reflect.Method;

public class Mapping {
    String className;
    String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void reflectMethod() throws RequestException {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(String.class) && !returnType.equals(ModelView.class)) {
                throw new RequestException("La méthode " + methodName + " dans " + className + " doit retourner un String ou un ModelView");
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RequestException("Erreur lors de la réflexion sur la méthode : " + e.getMessage());
        }
    }
}
