package itu.framework.scan;

import itu.framework.web.UploadFile;
import java.util.HashMap;
import java.util.Map;

/**
 * Validateur pour les types de paramètres des méthodes des contrôleurs.
 * Vérifie que les types personnalisés (POJO) proviennent du même package parent que les contrôleurs.
 */
public class ParameterTypeValidator {
    
    /**
     * Vérifie si un type de paramètre est valide pour une méthode de contrôleur.
     * Les types autorisés sont:
     * - Types primitifs et wrappers de Java (String, etc.)
     * - Types du framework (UploadFile, Map)
     * - Types personnalisés (POJO) qui doivent être dans le package parent du contrôleur
     * 
     * @param paramType Le type du paramètre à valider
     * @param controllerClass La classe du contrôleur contenant la méthode
     * @param paramName Le nom du paramètre (pour les messages d'erreur)
     * @param methodName Le nom de la méthode (pour les messages d'erreur)
     * @throws IllegalArgumentException Si le type n'est pas valide
     */
    public static void validateParameterType(Class<?> paramType, 
                                            Class<?> controllerClass, 
                                            String paramName, 
                                            String methodName) {
        // Types autorisés de la bibliothèque Java
        if (isJavaStandardType(paramType)) {
            return;
        }
        
        // Types du framework
        if (isFrameworkType(paramType)) {
            return;
        }
        
        // Types personnalisés (POJO) - doivent être dans le package parent du contrôleur
        if (!isInSameParentPackage(paramType, controllerClass)) {
            String controllerPackage = controllerClass.getPackage().getName();
            String paramTypePackage = paramType.getPackage() != null ? paramType.getPackage().getName() : "";
            String parentPackage = getParentPackage(controllerPackage);
            
            throw new IllegalArgumentException(
                "[ParameterTypeValidator] ERREUR: Le type '" + paramType.getSimpleName() + 
                "' du paramètre '" + paramName + "' dans la méthode " + 
                controllerClass.getSimpleName() + "." + methodName + "() " +
                "n'est pas valide.\n" +
                "Les types personnalisés (POJO) doivent être créés dans le package parent du contrôleur ou ses sous-packages.\n" +
                "Package du contrôleur: " + controllerPackage + "\n" +
                "Package parent attendu: " + parentPackage + ".*\n" +
                "Package du type: " + paramTypePackage
            );
        }
    }
    
    /**
     * Vérifie si un type est un type standard de Java (bibliothèque Java)
     */
    private static boolean isJavaStandardType(Class<?> type) {
        // Types primitifs
        if (type.isPrimitive()) {
            return true;
        }
        
        // String et types courants
        if (type == String.class || 

            type == Object.class) {
            return true;
        }
        
        // Types wrapper
        if (type == Integer.class || 
            type == Long.class || 
            type == Double.class || 
            type == Float.class ||
            type == Boolean.class ||
            type == Character.class ||
            type == Byte.class ||
            type == Short.class) {
            return true;
        }
        
        // Collections Java standard
        if (type == Map.class || 
            type == HashMap.class ||
            type == java.util.List.class ||
            type == java.util.ArrayList.class) {
            return true;
        }
        
        // Types java.time
        if (type.getPackage() != null && type.getPackage().getName().startsWith("java.time")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si un type est un type du framework
     */
    private static boolean isFrameworkType(Class<?> type) {
        if (type.getPackage() == null) {
            return false;
        }
        
        String packageName = type.getPackage().getName();
        
        // Types du framework ITU
        if (packageName.startsWith("itu.framework")) {
            return true;
        }
        
        // UploadFile spécifiquement
        if (type == UploadFile.class) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si deux classes sont dans le même package parent.
     * Par exemple: com.example.controllers et com.example.models partagent com.example
     */
    private static boolean isInSameParentPackage(Class<?> type1, Class<?> type2) {
        if (type1.getPackage() == null || type2.getPackage() == null) {
            return false;
        }
        
        String package1 = type1.getPackage().getName();
        String package2 = type2.getPackage().getName();
        
        String parent1 = getParentPackage(package1);
        String parent2 = getParentPackage(package2);
        
        // Les deux doivent avoir le même package parent
        return parent1.equals(parent2);
    }
    
    /**
     * Extrait le package parent d'un package complet.
     * Par exemple: com.example.controllers -> com.example
     */
    private static String getParentPackage(String fullPackage) {
        if (fullPackage == null || fullPackage.isEmpty()) {
            return "";
        }
        
        int lastDot = fullPackage.lastIndexOf('.');
        if (lastDot == -1) {
            return fullPackage; // Pas de parent, retourner le package lui-même
        }
        
        return fullPackage.substring(0, lastDot);
    }
}
