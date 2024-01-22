package model.export;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Classe utilitaire pour gérer les exports de données (JSON, XML, CSV, ...).
 */
public class ExportController {

    public static void exportResponse(Object data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String format = request.getParameter("export"); // ex: ?export=json

        if (format == null) {
            return; // pas d’export demandé
        }

        response.setCharacterEncoding("UTF-8");

        switch (format.toLowerCase()) {
            case "json":
                response.setContentType("application/json");
                String json = new Gson().toJson(data);
                response.getWriter().write(json);
                break;

            case "xml":
                response.setContentType("application/xml");
                response.getWriter().write(toXml(data));
                break;

            case "csv":
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
                response.getWriter().write(toCsv(data));
                break;

            default:
                response.setContentType("text/plain");
                response.getWriter().write("Format d'export non supporté : " + format);
        }
    }

    // Conversion en XML (simple, à remplacer par JAXB si tu veux un vrai XML)
    private static String toXml(Object data) {
        return "<data>" + data.toString() + "</data>";
    }

    // Conversion en CSV (exemple basique, à personnaliser pour tes objets)
    private static String toCsv(Object data) {
        return "value\n" + data.toString();
    }
    //c est moi
}
