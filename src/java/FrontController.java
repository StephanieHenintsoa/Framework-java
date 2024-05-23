package controller;

import map.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import annotation.Annotation_Get;
import utils.ControllerScanner;

public class FrontController extends HttpServlet {
    private String namePackage;
    private ControllerScanner scanner;
    private List<Class<?>> controllers;
    private HashMap<String, Mapping> urlMapping = new HashMap<>();

    @Override
    public void init(ServletConfig configurer) throws ServletException {
        try {
            super.init(configurer);
            ServletContext context = configurer.getServletContext();
            namePackage = context.getInitParameter("package-controller");
            this.scanner = new ControllerScanner();
            this.controllers = this.scanner.findControllers(namePackage);

            
            for (Class<?> controllerClass : controllers) {
                Method[] methods = controllerClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Annotation_Get.class)) {
                        Annotation_Get annotation = method.getAnnotation(Annotation_Get.class);
                        String url = annotation.value();
                        Mapping mapping = new Mapping(controllerClass.getName(), method.getName());
                        urlMapping.put(url, mapping);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String url = request.getRequestURI();

            out.println("<h1>" + "Bienvenue " + "</h1>");
            out.println("<h3>Lien: " + url + " </h3>");


            Mapping mapping = urlMapping.get("/first");

            if (mapping != null) {
                out.println("<h3>URL: " + url + " - Mapping: " + mapping.getClassName() + "#" + mapping.getMethodName() + "</h3>");
            } else {
                out.println("<h3>Aucune méthode associée à ce chemin</h3>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
