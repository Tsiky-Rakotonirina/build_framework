package itu.framework.scan;

import itu.framework.annotation.Controller;
import itu.framework.annotation.HttpMethod;
import itu.framework.annotation.Json;
import itu.framework.annotation.RequestParameter;
import itu.framework.annotation.Session;
import itu.framework.annotation.Url;
import itu.framework.web.UploadFile;
import itu.framework.scan.ParameterTypeValidator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
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
        // Liste des noms de paramètres de la méthode
        private List<String> parameterNames;
        // Liste des types de paramètres
        private List<Class<?>> parameterTypes;
        // Liste des types génériques des paramètres
        private List<Type> genericParameterTypes;
        // Liste des clés RequestParameter (null si pas d'annotation)
        private List<String> parameterKeys;
        // Pattern pour matcher les URL avec variables ex: /employe/{id}
        private Pattern pathPattern;
        // Noms des variables dans l'ordre
        private List<String> pathParamNames;
        // L'URL telle qu'annotée (ex: /employe/{id})
        private String urlPattern;
        // Indique si la méthode est annotée avec @Json
        private boolean isJsonMethod;
        // Index du paramètre annoté @Session (-1 si aucun)
        private int sessionParameterIndex;
        
        public MethodInfo(Class<?> controllerClass, Method method) {
            this.controllerClass = controllerClass;
            this.method = method;
            this.parameterNames = new ArrayList<>();
            this.parameterTypes = new ArrayList<>();
            this.genericParameterTypes = new ArrayList<>();
            this.parameterKeys = new ArrayList<>();
            this.pathParamNames = new ArrayList<>();
            this.isJsonMethod = false;
            this.sessionParameterIndex = -1;
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
        
        public List<Type> getGenericParameterTypes() {
            return genericParameterTypes;
        }
        
        public void setGenericParameterTypes(List<Type> genericParameterTypes) {
            this.genericParameterTypes = genericParameterTypes;
        }
        
        public List<String> getParameterKeys() {
            return parameterKeys;
        }
        
        public void setParameterKeys(List<String> parameterKeys) {
            this.parameterKeys = parameterKeys;
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

        public boolean isJsonMethod() { return isJsonMethod; }

        public void setJsonMethod(boolean jsonMethod) { isJsonMethod = jsonMethod; }
        
        public int getSessionParameterIndex() { return sessionParameterIndex; }
        
        public void setSessionParameterIndex(int sessionParameterIndex) { 
            this.sessionParameterIndex = sessionParameterIndex; 
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
            // Vérification pour @Url (obligatoire)
            if (method.isAnnotationPresent(Url.class)) {
                // VALIDATION: La méthode ne doit pas être statique
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(
                        "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() + 
                        "." + method.getName() + "() annotée @Url ne peut pas être statique."
                    );
                }
                
                // VALIDATION: La méthode doit être publique
                if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(
                        "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() + 
                        "." + method.getName() + "() annotée @Url doit être publique."
                    );
                }
                
                Url urlAnnotation = method.getAnnotation(Url.class);
                String url = urlAnnotation.value();
                
                // Vérifier si @HttpMethod est présent
                HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
                String[] httpMethods;
                
                if (httpMethodAnnotation != null) {
                    // Si @HttpMethod spécifié, utiliser uniquement cette méthode
                    httpMethods = new String[] { httpMethodAnnotation.value().toUpperCase() };
                } else {
                    // Sinon, enregistrer pour GET et POST
                    httpMethods = new String[] { "GET", "POST" };
                }
                
                // Créer le MethodInfo une seule volta
                MethodInfo methodInfo = new MethodInfo(controllerClass, method);
                methodInfo.setUrlPattern(url);

                // Vérifier si la méthode est annotée avec @Json
                if (method.isAnnotationPresent(Json.class)) {
                    methodInfo.setJsonMethod(true);
                }

                // Détecter des variables de chemin {name} et construire un Pattern
                List<String> pathParams = new ArrayList<>();
                if (url.contains("{")) {
                    Matcher m = Pattern.compile("\\{([^/}]+)\\}").matcher(url);
                    while (m.find()) {
                        pathParams.add(m.group(1));
                    }

                    // Remplacer {name} par un groupe capture ([^/]+)
                    String regex = "^" + url.replaceAll("\\{[^/}]+\\}", "([^/]+)") + "$";
                    Pattern pathPattern = Pattern.compile(regex);

                    methodInfo.setPathParamNames(pathParams);
                    methodInfo.setPathPattern(pathPattern);
                }
                
                // Extraction des paramètres de la méthode
                Parameter[] parameters = method.getParameters();
                Type[] genericParameterTypesArray = method.getGenericParameterTypes();
                List<String> paramNames = new ArrayList<>();
                List<Class<?>> paramTypes = new ArrayList<>();
                List<Type> genericParamTypes = new ArrayList<>();
                List<String> paramKeys = new ArrayList<>();
                
                boolean hasMapParam = false;
                boolean hasSessionParam = false;
                int sessionParamIndex = -1;
                
                for (int i = 0; i < parameters.length; i++) {
                    Parameter param = parameters[i];
                    Class<?> paramType = param.getType();
                    Type genericType = genericParameterTypesArray[i];
                    
                    // Vérifier si le paramètre est annoté @Session
                    boolean isSessionParam = param.isAnnotationPresent(Session.class);
                    
                    if (isSessionParam) {
                        // Vérifier qu'il n'y a qu'un seul @Session
                        if (hasSessionParam) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() +
                                "." + method.getName() + "() ne peut avoir qu'UN SEUL paramètre annoté @Session."
                            );
                        }
                        
                        // Vérifier que le type est Map
                        if (paramType != Map.class && paramType != HashMap.class) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: Le paramètre annoté @Session doit être de type Map<String, Object> dans " +
                                controllerClass.getSimpleName() + "." + method.getName() + "()"
                            );
                        }
                        
                        hasSessionParam = true;
                        sessionParamIndex = i;
                        // Un paramètre @Session ne compte pas comme Map standard
                    }
                    
                    // VALIDATION: String, byte[], Map<String, Object>, @Session Map OU classes POJO personnalisées
                    if (paramType == String.class) {
                        // OK - String accepté
                    } else if (paramType == byte[].class) {
                        // OK - byte[] accepté pour les uploads de fichiers
                    } else if (paramType.getName().equals("itu.framework.web.UploadFile")) {
                        // OK - UploadFile accepté pour les uploads de fichiers
                    } else if (paramType == Map.class || paramType == HashMap.class) {
                        // Si c'est un @Session, on le laisse passer
                        if (!isSessionParam) {
                            // Vérifier qu'il n'y a qu'un seul Map non-@Session
                            if (hasMapParam) {
                                throw new IllegalArgumentException(
                                    "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() + 
                                    "." + method.getName() + "() ne peut avoir qu'UN SEUL paramètre de type Map (hors @Session)."
                                );
                            }
                            hasMapParam = true;
                        }
                        if (hasMapParam) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() +
                                "." + method.getName() + "() ne peut avoir qu'UN SEUL paramètre de type Map."
                            );
                        }

                        // 🔒 Vérification Map<String, Object> ou Map<String, UploadFile>
                        if (!(genericType instanceof ParameterizedType)) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: Le paramètre Map doit être typé Map<String, Object> ou Map<String, UploadFile> dans " +
                                controllerClass.getSimpleName() + "." + method.getName() + "()"
                            );
                        }

                        ParameterizedType paramTypeGeneric = (ParameterizedType) genericType;
                        Type[] typeArgs = paramTypeGeneric.getActualTypeArguments();

                        if (typeArgs.length != 2 || typeArgs[0] != String.class) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: Le paramètre Map doit avoir String comme premier type dans " +
                                controllerClass.getSimpleName() + "." + method.getName() + "()"
                            );
                        }
                        
                        // Vérifier que le second type est Object ou UploadFile
                        boolean isValidSecondType = typeArgs[1] == Object.class || 
                                                   (typeArgs[1] instanceof Class<?> && 
                                                    ((Class<?>) typeArgs[1]).getName().equals("itu.framework.web.UploadFile"));
                        
                        if (!isValidSecondType) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: Le paramètre Map doit être STRICTEMENT de type Map<String, Object> ou Map<String, UploadFile> dans " +
                                controllerClass.getSimpleName() + "." + method.getName() + "()"
                            );
                        }

                        hasMapParam = true;
                    } else if (paramType.isPrimitive() || paramType.isInterface()) {
                        // Interdire les types primitifs, interfaces
                        throw new IllegalArgumentException(
                            "[ControllerScanner] ERREUR: La méthode " + controllerClass.getSimpleName() + 
                            "." + method.getName() + "() a un paramètre '" + param.getName() + 
                            "' de type " + paramType.getSimpleName() + 
                            ". Les paramètres doivent être String, byte[], Map, @Session Map ou des classes POJO."
                        );
                    } else {
                        // OK - Classe POJO personnalisée (sprint 8 bis)
                        // Validation: le type POJO doit être dans le package parent du contrôleur
                        ParameterTypeValidator.validateParameterType(
                            paramType, 
                            controllerClass, 
                            param.getName(), 
                            method.getName()
                        );
                    }
                    
                    paramNames.add(param.getName());
                    paramTypes.add(paramType);
                    genericParamTypes.add(genericType);
                    
                    // Vérifier si le paramètre a l'annotation @RequestParameter
                    RequestParameter reqParam = param.getAnnotation(RequestParameter.class);
                    if (reqParam != null) {
                        paramKeys.add(reqParam.key());
                    } else {
                        paramKeys.add(null);
                    }
                }

                // VALIDATION: Si des url avec {}          
                if (!pathParams.isEmpty()) {
                    for (String pathParam : pathParams) {
                        int idx = paramNames.indexOf(pathParam);
                        if (idx == -1) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: L'URL '" + url + "' demande la variable de chemin '{" +
                                pathParam + "}' mais aucun paramètre de méthode nommé '" + pathParam + "' n'a été trouvé."
                            );
                        }

                        Class<?> candidateType = paramTypes.get(idx);
                        if (candidateType != String.class) {
                            throw new IllegalArgumentException(
                                "[ControllerScanner] ERREUR: Le paramètre '" + pathParam + "' lié à '{" + pathParam +
                                "}' doit être de type String."
                            );
                        }
                    }
                }
                
                methodInfo.setParameterNames(paramNames);
                methodInfo.setParameterTypes(paramTypes);
                methodInfo.setGenericParameterTypes(genericParamTypes);
                methodInfo.setParameterKeys(paramKeys);
                methodInfo.setSessionParameterIndex(sessionParamIndex);

                // Enregistrer pour chaque méthode HTTP
                for (String httpMethod : httpMethods) {
                    String key = httpMethod + ":" + url;
                    
                    // VALIDATION: Vérifier les URL dupliquées
                    if (mappings.containsKey(key)) {
                        MethodInfo existing = mappings.get(key);
                        throw new IllegalArgumentException(
                            "[ControllerScanner] ERREUR: L'URL '" + url + "' pour la méthode HTTP '" + httpMethod + 
                            "' est déjà mappée par " + existing.getControllerClass().getSimpleName() + 
                            "." + existing.getMethod().getName() + "(). Conflit avec " + 
                            controllerClass.getSimpleName() + "." + method.getName() + "()."
                        );
                    }
                    
                    mappings.put(key, methodInfo);
                    
                }
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
