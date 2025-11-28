package itu.framework.scan;

import itu.framework.annotation.Controller;
import itu.framework.annotation.Web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scanner qui détecte les contrôleurs et leurs mappings
 */
public class ControllerScanner {
    
    /**
     * Classe interne pour stocker les informations d'un mapping
     */
    public static class MethodInfo {
        private Class<?> controllerClass;
        private Method method;
        // Pattern pour matcher les URL avec variables ex: /employe/{id}
        private Pattern pathPattern;
        // Noms des variables dans l'ordre
        private List<String> pathParamNames;
        // L'URL telle qu'annotée (ex: /employe/{id})
        private String urlPattern;
        
        public MethodInfo(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
            this.pathParamNames = new ArrayList<>();
        }
        
        public Class<?> getControllerClass() {
            return controllerClass;
        }
        
        public Method getMethod() {
            return method;
        }

        public Pattern getPathPattern() {
            return pathPattern;
        }

        public void setPathPattern(Pattern pathPattern) {
            this.pathPattern = pathPattern;
        }

        public List<String> getPathParamNames() {
            return pathParamNames;
        }

        public void setPathParamNames(List<String> pathParamNames) {
            this.pathParamNames = pathParamNames;
        }

        public String getUrlPattern() { return urlPattern; }

        public void setUrlPattern(String urlPattern) { this.urlPattern = urlPattern; }
    }
    
    /**
     * Scanne un package pour trouver les contrôleurs et enregistrer leurs mappings
     * @param basePackage Le package à scanner
     * @return Une Map avec clé = "METHOD:URL" et valeur = MethodInfo (classe + méthode)
     */
    public static Map<String, MethodInfo> scanControllers(String basePackage) {
        Map<String, MethodInfo> mappings = new HashMap<>();
        
        System.out.println("\n[ControllerScanner] Début du scan du package: " + basePackage);
        
        // Récupère toutes les classes du package
        List<Class<?>> classes = ClassScanner.scan(basePackage);
        
        System.out.println("[ControllerScanner] Classes trouvées: " + classes.size());
        
        // Parcourt chaque classe
        for (Class<?> clazz : classes) {
            // Vérifie si la classe a l'annotation @Controller
            if (clazz.isAnnotationPresent(Controller.class)) {
                System.out.println("[ControllerScanner] Contrôleur trouvé: " + clazz.getName());
                scanControllerMethods(clazz, mappings);
            }
        }
        
        printMappings(mappings);
        return mappings;
    }
    
    /**
     * Scanne les méthodes d'un contrôleur pour trouver les mappings
     */
    private static void scanControllerMethods(Class<?> controllerClass, Map<String, MethodInfo> mappings) {
        Method[] methods = controllerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            // Vérification pour @Web
            if (method.isAnnotationPresent(Web.class)) {
                Web webAnnotation = method.getAnnotation(Web.class);
                String url = webAnnotation.value();
                String httpMethod = webAnnotation.method().toUpperCase();

                // Clé = "METHOD:URL" (la key contient la pattern telle qu'annotée, ex: /employe/{id})
                String key = httpMethod + ":" + url;

                MethodInfo methodInfo = new MethodInfo(controllerClass, method);
                methodInfo.setUrlPattern(url);

                // Détecter des variables de chemin {name} et construire un Pattern
                if (url.contains("{")) {
                    List<String> paramNames = new ArrayList<>();
                    Matcher m = Pattern.compile("\\{([^/}]+)\\}").matcher(url);
                    while (m.find()) {
                        paramNames.add(m.group(1));
                    }

                    // Remplacer {name} par un groupe catch-all (sans slash)
                    String regex = "^" + url.replaceAll("\\{[^/}]+\\}", "([^/]+)") + "$";
                    Pattern pathPattern = Pattern.compile(regex);

                    methodInfo.setPathParamNames(paramNames);
                    methodInfo.setPathPattern(pathPattern);
                }

                mappings.put(key, methodInfo);

                System.out.println("[ControllerScanner] Mapping ajouté: " + key + 
                                 " -> " + controllerClass.getSimpleName() + "." + method.getName() + "()");
            }
        }
    }
    
    /**
     * Affiche tous les mappings (pour debug)
     */
    private static void printMappings(Map<String, MethodInfo> mappings) {
        System.out.println("\n========== MAPPINGS TROUVÉS ==========");
        if (mappings.isEmpty()) {
            System.out.println("Aucun mapping trouvé");
        } else {
            for (Map.Entry<String, MethodInfo> entry : mappings.entrySet()) {
                MethodInfo info = entry.getValue();
                System.out.println(entry.getKey() + " -> " + 
                                 info.getControllerClass().getSimpleName() + "." + 
                                 info.getMethod().getName() + "()");
            }
        }
        System.out.println("======================================\n");
    }
}
