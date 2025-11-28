package itu.framework.scan;

import itu.framework.annotation.Controller;
import itu.framework.annotation.RequestParameter;
import itu.framework.annotation.Web;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // Liste des noms de paramètres de la méthode
        private List<String> parameterNames;
        // Liste des types de paramètres
        private List<Class<?>> parameterTypes;
        // Liste des clés RequestParameter (null si pas d'annotation)
        private List<String> parameterKeys;
        
        public MethodInfo(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
            this.parameterNames = new ArrayList<>();
            this.parameterTypes = new ArrayList<>();
            this.parameterKeys = new ArrayList<>();
        }
        
        public Class<?> getControllerClass() {
            return controllerClass;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public List<String> getParameterNames() {
            return parameterNames;
        }
        
        public void setParameterNames(List<String> parameterNames) {
            this.parameterNames = parameterNames;
        }
        
        public List<Class<?>> getParameterTypes() {
            return parameterTypes;
        }
        
        public void setParameterTypes(List<Class<?>> parameterTypes) {
            this.parameterTypes = parameterTypes;
        }
        
        public List<String> getParameterKeys() {
            return parameterKeys;
        }
        
        public void setParameterKeys(List<String> parameterKeys) {
            this.parameterKeys = parameterKeys;
        }
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
                
                // Clé = "METHOD:URL"
                String key = httpMethod + ":" + url;
                
                MethodInfo methodInfo = new MethodInfo(controllerClass, method);
                
                // Extraction des paramètres de la méthode
                Parameter[] parameters = method.getParameters();
                List<String> paramNames = new ArrayList<>();
                List<Class<?>> paramTypes = new ArrayList<>();
                List<String> paramKeys = new ArrayList<>();
                
                for (Parameter param : parameters) {
                    paramNames.add(param.getName());
                    paramTypes.add(param.getType());
                    
                    // Vérifier si le paramètre a l'annotation @RequestParameter
                    RequestParameter reqParam = param.getAnnotation(RequestParameter.class);
                    if (reqParam != null) {
                        paramKeys.add(reqParam.key());
                    } else {
                        paramKeys.add(null);
                    }
                }
                
                methodInfo.setParameterNames(paramNames);
                methodInfo.setParameterTypes(paramTypes);
                methodInfo.setParameterKeys(paramKeys);
                
                mappings.put(key, methodInfo);
                
                String paramInfo = paramNames.isEmpty() ? "()" : "(" + String.join(", ", paramNames) + ")";
                System.out.println("[ControllerScanner] Mapping ajouté: " + key + 
                                 " -> " + controllerClass.getSimpleName() + "." + method.getName() + paramInfo);
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
