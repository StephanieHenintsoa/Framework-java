package util;
import exception.*;

public class UtilPackage {

    public static void validatePackage(String namePackage) throws BuildException {
        if (namePackage == null || namePackage.isEmpty()) {
            throw new BuildException("Le package des contrôleurs n'est pas spécifié ou est vide.");
        }
    }
}
