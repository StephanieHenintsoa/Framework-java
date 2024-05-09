package controller;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class FrontController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    { 
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    { 
        try {
            PrintWriter out = response.getWriter();
            String url = request.getRequestURI(); 
            
            out.println("<h1>" + " Bienvenue " + "</h1>");
            out.println("<h3> Lien: " + url + " </h3>");
        } 
        
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}