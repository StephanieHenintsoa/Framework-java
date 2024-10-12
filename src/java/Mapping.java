package map;

import exception.RequestException;
import utils.*;
import verb.VerbAction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Mapping {
    private String className;
    private List<VerbAction> verbsAction;

    public Mapping(String className) {
        this.className = className;
        this.verbsAction = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<VerbAction> getVerbsAction() {
        return verbsAction;
    }

    public void addVerbAction(String verb, String method) {
        this.verbsAction.add(new VerbAction(verb, method));
    }

    public String getMethodName(String verb) {
        for (VerbAction action : verbsAction) {
            if (action.getVerbe().equalsIgnoreCase(verb)) {
                return action.getMethode();
            }
        }
        return null;
    }

    public void reflectMethod(String verb) throws RequestException {
        String methodName = getMethodName(verb);
        if (methodName == null) {
            throw new RequestException("No method found for verb: " + verb);
        }

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