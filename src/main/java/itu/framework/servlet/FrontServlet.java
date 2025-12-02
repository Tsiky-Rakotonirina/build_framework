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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            
            // Préparation des arguments de la méthode depuis request.getParameter()
            List<String> paramNames = methodInfo.getParameterNames();
            List<Class<?>> paramTypes = methodInfo.getParameterTypes();
            List<String> paramKeys = methodInfo.getParameterKeys();
            Object[] args = new Object[paramNames.size()];
            
            for (int i = 0; i < paramNames.size(); i++) {
                String paramName = paramNames.get(i);
                Class<?> paramType = paramTypes.get(i);
                String paramKey = (paramKeys != null && i < paramKeys.size()) ? paramKeys.get(i) : null;
                
                // Si c'est un Map, remplir avec tous les paramètres HTTP
                if (paramType == Map.class || paramType == HashMap.class) {
                    Map<String, Object> allParams = new HashMap<>();
                    
                    // Récupérer tous les paramètres de la requête
                    java.util.Enumeration<String> parameterNames = req.getParameterNames();
                    while (parameterNames.hasMoreElements()) {
                        String paramKey2 = parameterNames.nextElement();
                        String value = req.getParameter(paramKey2);
                        allParams.put(paramKey2, value);
                    }
                    
                    args[i] = allParams;
                } else if (paramType == String.class) {
                    // Si String, chercher par nom puis par @RequestParameter key
                    String paramValue = req.getParameter(paramName);
                    if (paramValue == null && paramKey != null) {
                        paramValue = req.getParameter(paramKey);
                    }
                    args[i] = paramValue;
                } else {
                    // Sprint 8 bis: POJO parameter binding
                    // Chercher les paramètres de la forme "paramName.field"
                    Object pojoInstance = paramType.getDeclaredConstructor().newInstance();
                    
                    // Récupérer tous les paramètres HTTP
                    java.util.Enumeration<String> parameterNames = req.getParameterNames();
                    while (parameterNames.hasMoreElements()) {
                        String httpParamName = parameterNames.nextElement();
                        
                        // Vérifier si le paramètre commence par "paramName."
                        String prefix = paramName + ".";
                        if (httpParamName.startsWith(prefix)) {
                            String fieldName = httpParamName.substring(prefix.length());
                            String fieldValue = req.getParameter(httpParamName);
                            
                            // Utiliser la réflexion pour définir la valeur du champ
                            try {
                                java.lang.reflect.Field field = paramType.getDeclaredField(fieldName);
                                field.setAccessible(true);
                                
                                // Conversion de type selon le type du champ
                                Class<?> fieldType = field.getType();
                                if (fieldType == String.class) {
                                    field.set(pojoInstance, fieldValue);
                                } else if (fieldType == int.class || fieldType == Integer.class) {
                                    field.set(pojoInstance, Integer.parseInt(fieldValue));
                                } else if (fieldType == long.class || fieldType == Long.class) {
                                    field.set(pojoInstance, Long.parseLong(fieldValue));
                                } else if (fieldType == double.class || fieldType == Double.class) {
                                    field.set(pojoInstance, Double.parseDouble(fieldValue));
                                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                                    field.set(pojoInstance, Boolean.parseBoolean(fieldValue));
                                } else {
                                    field.set(pojoInstance, fieldValue);
                                }
                            } catch (NoSuchFieldException e) {
                                // Ignorer les champs qui n'existent pas dans la classe
                            }
                        }
                    }
                    
                    args[i] = pojoInstance;
                }
            }
            
            // Invocation de la méthode avec les arguments extraits
            Object result = method.invoke(controllerInstance, args);
            
            // Gestion selon le type de retour
            if (returnType.equals(String.class)) {
                // Si String, afficher avec out.print
                PrintWriter out = resp.getWriter();
                out.print((String) result);
            } else if (returnType.equals(ModelView.class)) {
                // Si ModelView, faire un dispatcher et injecter les données si présentes
                ModelView modelView = (ModelView) result;
                HashMap<String, Object> data = modelView.getData();

                // Injecter chaque paire clé/valeur comme attribut de requête
                if (data != null && !data.isEmpty()) {
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
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
