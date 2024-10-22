package file;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Part;

public class FileUpload {
    private String fileName;
    private String contentType;
    private long size;
    private InputStream content;
    private static final Map<String, String[]> ALLOWED_TYPES = new HashMap<>();
    
    static {
        ALLOWED_TYPES.put("image", new String[]{"image/jpeg", "image/png", "image/gif"});
        ALLOWED_TYPES.put("document", new String[]{"application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        ALLOWED_TYPES.put("spreadsheet", new String[]{"application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
    }

    public FileUpload(Part filePart) throws Exception {
        this.fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        this.contentType = filePart.getContentType();
        this.size = filePart.getSize();
        this.content = filePart.getInputStream();
    }

    public boolean isAllowedType(String category) {
        String[] allowedTypes = ALLOWED_TYPES.get(category);
        if (allowedTypes == null) return false;
        for (String type : allowedTypes) {
            if (type.equals(this.contentType)) return true;
        }
        return false;
    }

    public void save(String path) throws Exception {
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        Files.copy(content, Paths.get(path, fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    // Getters
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
}
