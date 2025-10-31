package itu.framework.servlet;

import itu.framework.listener.FrameworkListener;
import itu.framework.scan.ControllerScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"})

public class FrontServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "POST");
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String httpMethod) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        
        // Récupération de l'URI demandée
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Récupération des mappings depuis ServletContext
        Map<String, ControllerScanner.MethodInfo> mappings = 
            (Map<String, ControllerScanner.MethodInfo>) getServletContext()
                .getAttribute(FrameworkListener.MAPPINGS_KEY);
        
        // Création de la clé METHOD:URL
        String key = httpMethod + ":" + path;
        
        try (PrintWriter w = resp.getWriter()) {
            w.println("<html><head><meta charset='UTF-8'><title>FrontServlet</title></head><body>");
            w.println("<h2>FrontServlet</h2>");
            w.println("<p>Requested URL: <strong>" + req.getRequestURL().toString() + "</strong></p>");
            
            // Vérification si le mapping existe
            if (mappings != null && mappings.containsKey(key)) {
                ControllerScanner.MethodInfo methodInfo = mappings.get(key);
                w.println("<hr>");
                w.println("<h3>Mapping trouvé ✓</h3>");
                w.println("<ul>");
                w.println("<li><strong>URL:</strong> " + path + "</li>");
                w.println("<li><strong>Méthode HTTP:</strong> " + httpMethod + "</li>");
                w.println("<li><strong>Classe:</strong> " + methodInfo.getControllerClass().getName() + "</li>");
                w.println("<li><strong>Méthode de classe:</strong> " + methodInfo.getMethod().getName() + "()</li>");
                w.println("</ul>");
            } else if (mappings != null) {
                // Essayer avec ANY
                String anyKey = "ANY:" + path;
                if (mappings.containsKey(anyKey)) {
                    ControllerScanner.MethodInfo methodInfo = mappings.get(anyKey);
                    w.println("<hr>");
                    w.println("<h3>Mapping trouvé (ANY) ✓</h3>");
                    w.println("<ul>");
                    w.println("<li><strong>URL:</strong> " + path + "</li>");
                    w.println("<li><strong>Méthode HTTP:</strong> ANY (accepte " + httpMethod + ")</li>");
                    w.println("<li><strong>Classe:</strong> " + methodInfo.getControllerClass().getName() + "</li>");
                    w.println("<li><strong>Méthode de classe:</strong> " + methodInfo.getMethod().getName() + "()</li>");
                    w.println("</ul>");
                } else {
                    w.println("<hr>");
                    w.println("<p><em>Aucun mapping trouvé pour: " + key + "</em></p>");
                }
            } else {
                w.println("<hr>");
                w.println("<p><em>ServletContext non initialisé (aucun mapping disponible)</em></p>");
            }
            
            w.println("</body></html>");
        }
    }
}
