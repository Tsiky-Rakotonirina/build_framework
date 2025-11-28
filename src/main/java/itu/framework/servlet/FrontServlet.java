package itu.framework.servlet;

import itu.framework.listener.FrameworkListener;
import itu.framework.scan.ControllerScanner;
import itu.framework.scan.ControllerScanner.MethodInfo;
import itu.framework.scan.ModelView;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

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

//         change dans /build/scan le modelView en liste de Hashmap

// ensuite utilise le dans /use/java/testController au niveau de /hello

// /build/Front§Servlet mets au moment ou on sait que ca retourne un model view, il faut faire e sorte que ca verfiei si model a des donnees si oui ben aficher aussi dans le view contenu dans getView
        
        // Recherche du mapping : exact -> ANY -> patterns avec variables {id}
        ControllerScanner.MethodInfo methodInfo = mappings.get(key);

        // Essayer la clé ANY exact
        if (methodInfo == null) {
            String anyKey = "ANY:" + path;
            methodInfo = mappings.get(anyKey);
        }

        // Si toujours null, essayer de matcher les patterns enregistrés (ex: /employe/{id})
        if (methodInfo == null) {
            for (Map.Entry<String, ControllerScanner.MethodInfo> entry : mappings.entrySet()) {
                String mapKey = entry.getKey();
                // garder uniquement les mappings pour la même méthode ou ANY
                if (!(mapKey.startsWith(httpMethod + ":") || mapKey.startsWith("ANY:"))) {
                    continue;
                }

                ControllerScanner.MethodInfo mi = entry.getValue();
                Pattern p = mi.getPathPattern();
                if (p == null) continue;

                Matcher matcher = p.matcher(path);
                if (matcher.matches()) {
                    methodInfo = mi;
                    // extraire les variables de chemin et les mettre en attributs de requête
                    List<String> names = mi.getPathParamNames();
                    if (names != null) {
                        for (int i = 0; i < names.size(); i++) {
                            String paramName = names.get(i);
                            String value = matcher.group(i + 1);
                            req.setAttribute(paramName, value);
                        }
                    }
                    break;
                }
            }
        }

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
                // Si ModelView, faire un dispatcher et injecter les données si présentes
                ModelView modelView = (ModelView) result;
                List<HashMap<String, Object>> modelList = modelView.getModelList();

                // Injecter chaque paire clé/valeur comme attribut de requête
                if (modelList != null && !modelList.isEmpty()) {
                    for (HashMap<String, Object> map : modelList) {
                        if (map != null) {
                            for (Map.Entry<String, Object> e : map.entrySet()) {
                                req.setAttribute(e.getKey(), e.getValue());
                            }
                        }
                    }
                    // Mettre aussi la liste entière sous l'attribut 'model'
                    req.setAttribute("model", modelList);
                }

                String viewPath = modelView.getView();
                if (!viewPath.startsWith("/")) {
                    viewPath = "/" + viewPath;
                }
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
