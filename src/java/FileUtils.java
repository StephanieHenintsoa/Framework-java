package file;

import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

public class FileUtils {
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB par défaut
    
    public static FileUpload processUploadedFile(HttpServletRequest request, String paramName) 
            throws Exception {
        Part filePart = request.getPart(paramName);
        if (filePart == null) {
            throw new Exception("Aucun fichier trouvé pour le paramètre : " + paramName);
        }
        
        FileUpload file = new FileUpload(filePart);
        validateFileSize(file);
        return file;
    }

    public static Collection<FileUpload> processMultipleFiles(HttpServletRequest request, String paramName) 
            throws Exception {
        Collection<Part> fileParts = request.getParts();
        Collection<FileUpload> files = new ArrayList<>();
        
        for (Part part : fileParts) {
            if (part.getName().equals(paramName)) {
                FileUpload file = new FileUpload(part);
                validateFileSize(file);
                files.add(file);
            }
        }
        return files;
    }

    private static void validateFileSize(FileUpload file) throws Exception {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new Exception("La taille du fichier dépasse la limite autorisée de " + 
                (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
    }
}
