package controller;

import map.*;
import exception.*;
import utils.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import annotation.Annotation_Get;

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
            if (namePackage == null || namePackage.isEmpty()) {
                throw new BuildException("Le package des contrôleurs n'est pas spécifié ou est vide.");
            }
            this.scanner = new ControllerScanner();
            this.controllers = this.scanner.findControllers(namePackage);
            if (this.controllers.isEmpty()) {
                throw new BuildException("Aucun contrôleur trouvé dans le package spécifié : " + namePackage);
            }

            mapControllers();
        } catch (BuildException | RequestException e) {
            throw new ServletException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mapControllers() throws RequestException {
        for (Class<?> controllerClass : controllers) {
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Annotation_Get.class)) {
                    Annotation_Get annotation = method.getAnnotation(Annotation_Get.class);
                    String url = annotation.value();
                    if (urlMapping.containsKey(url)) {
                        throw new RequestException("Duplication d'URL détectée pour : " + url);
                    }
                    // Vérifier le type de retour
                    Class<?> returnType = method.getReturnType();
                    if (!returnType.equals(String.class) && !returnType.equals(ModelView.class)) {
                        throw new RequestException("La méthode " + method.getName() + " dans " + controllerClass.getName() + " a un type de retour invalide : " + returnType.getName());
                    }
                    Mapping mapping = new Mapping(controllerClass.getName(), method.getName());
                    urlMapping.put(url, mapping);
                }
            }
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

    private String extractUrl(String fullUrl) {
        String contextPath = "/framework";
        if (fullUrl.startsWith(contextPath)) {
            return fullUrl.substring(contextPath.length());
        }
        return fullUrl;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            String fullUrl = request.getRequestURI();
            String url = extractUrl(fullUrl);

            out.println("<h1>Bienvenue</h1>");
            out.println("<h3>Lien: " + url + "</h3>");

            Mapping mapping = urlMapping.get(url);

            if (mapping != null) {
                out.println("<h3>URL: " + url + " - Mapping: " + mapping.getClassName() + "#" + mapping.getMethodName() + "</h3>");

                Class<?> controllerClass = Class.forName(mapping.getClassName());

                Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                Method method = controllerClass.getMethod(mapping.getMethodName());

                Object result = method.invoke(controllerInstance);

                // Vérifier le type de retour
                if (result instanceof String) {
                    out.println("<h3>Résultat: " + result + "</h3>");
                } else if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    String viewUrl = modelView.getUrl();
                    HashMap<String, Object> data = modelView.getData();

                    for (String key : data.keySet()) {
                        request.setAttribute(key, data.get(key));
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewUrl);
                    dispatcher.forward(request, response);
                } else {
                    throw new RequestException("Type de résultat non reconnu pour l'URL : " + url);
                }
            } else {
                throw new RequestException("URL non trouvée : " + url);
            }
        } catch (RequestException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur interne s'est produite.");
        }
    }
}
