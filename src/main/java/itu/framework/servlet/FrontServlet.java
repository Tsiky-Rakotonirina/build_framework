package itu.framework.servlet;

import itu.framework.listener.FrameworkListener;
import itu.framework.scan.ControllerScanner;
import itu.framework.scan.ModelView;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
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
        
        // Si le path est vide, mettre "/"
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        
        // Récupération des mappings depuis ServletContext
        @SuppressWarnings("unchecked")
        Map<String, ControllerScanner.MethodInfo> mappings = 
            (Map<String, ControllerScanner.MethodInfo>) getServletContext().getAttribute(FrameworkListener.MAPPINGS_KEY);
        
        // Création de la clé METHOD:URL
        String key = httpMethod + ":" + path;
        
        if (mappings == null) {
            PrintWriter out = resp.getWriter();
            out.print("<html><body>");
            out.print("<p>ServletContext non initialisé (aucun mapping disponible)</p>");
            out.print("</body></html>");
            return;
        }
        
        // Recherche du mapping (d'abord avec la méthode HTTP spécifique, puis avec ANY)
        ControllerScanner.MethodInfo methodInfo = mappings.get(key);
        
        if (methodInfo == null) {
            PrintWriter out = resp.getWriter();
            out.print("<html><body>");
            out.print("<p>Aucun mapping trouvé pour: " + key + "</p>");
            out.print("</body></html>");
            return;
        }
        
        // Vérification du type de retour de la méthode
        try {
            Method method = methodInfo.getMethod();
            Class<?> returnType = method.getReturnType();
            
            // Instanciation du contrôleur
            Object controllerInstance = methodInfo.getControllerClass().getDeclaredConstructor().newInstance();
            
            // Invocation de la méthode
            Object result = method.invoke(controllerInstance);
            
            // Gestion selon le type de retour
            if (returnType.equals(String.class)) {
                // Si String, afficher avec out.print
                PrintWriter out = resp.getWriter();
                out.print((String) result);
            } else if (returnType.equals(ModelView.class)) {
                // Si ModelView, faire un dispatcher
                ModelView modelView = (ModelView) result;
                String viewPath = modelView.getView();
                RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                dispatcher.forward(req, resp);
            } else {
                // Si autre type, afficher message non supporté
                PrintWriter out = resp.getWriter();
                out.print("Type de retour non supporté: " + returnType.getName());
            }
            
        } catch (Exception e) {
            PrintWriter out = resp.getWriter();
            out.print("<html><body>");
            out.print("<h3>Erreur lors de l'exécution de la méthode:</h3>");
            out.print("<pre>" + e.getMessage() + "</pre>");
            out.print("</body></html>");
            e.printStackTrace();
        }
    }
}
