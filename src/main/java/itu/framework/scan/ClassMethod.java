package itu.framework.scan;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe pour stocker les mappings URL -> MethodInfo
 * Remplace l'ancien HashMap<String, MethodInfo>
 */
public class ClassMethod {
    private String key;
    private String url;
    private Map<String, ControllerScanner.MethodInfo> mappings;
    
    public ClassMethod() {
        this.mappings = new HashMap<>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, ControllerScanner.MethodInfo> getMappings() {
        return mappings;
    }
    
    public void setMappings(Map<String, ControllerScanner.MethodInfo> mappings) {
        this.mappings = mappings;
    }
    
    /**
     * Ajoute un mapping
     */
    public void addMapping(String key, ControllerScanner.MethodInfo methodInfo) {
        this.mappings.put(key, methodInfo);
    }
    
    /**
     * Récupère un mapping par clé
     */
    public ControllerScanner.MethodInfo getMapping(String key) {
        return this.mappings.get(key);
    }
}
