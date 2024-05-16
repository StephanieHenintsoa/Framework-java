package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.*;
import javax.servlet.*;

import utils.ControllerScanner;

public class FrontController extends HttpServlet {
    private String namePackage;
    private ControllerScanner scanner;
    private List<Class<?>> controller;


    @Override
    public void init(ServletConfig configurer) throws ServletException {
        try {
            super.init(configurer);
            ServletContext context = configurer.getServletContext();
            namePackage = context.getInitParameter("package-controller");
            this.scanner = new ControllerScanner();
            this.controller = this.scanner.findControllers(namePackage);
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

            out.println("<h1>" + " Bienvenue " + "</h1>");
            out.println("<h3> Lien: " + url + " </h3>");

            for (Class<?> controllerClass : controller) {
                out.println("Controller existant:" + controllerClass.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
