package itu.framework.web;

import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Une Map synchronisée avec HttpSession.
 * Toutes les modifications (put, remove, clear) sont immédiatement répercutées dans HttpSession.
 * De même, les lectures (get, containsKey, etc.) lisent directement depuis HttpSession pour refléter
 * tout changement effectué côté HttpSession.
 * 
 * Cette classe assure une synchronisation bidirectionnelle :
 * - Les modifications sur SessionMap → modifient HttpSession
 * - Les modifications sur HttpSession → visibles via SessionMap
 */
public class SessionMap extends HashMap<String, Object> {
    
    private final HttpSession httpSession;
    
    /**
     * Crée une SessionMap liée à une HttpSession.
     * @param httpSession La session HTTP à synchroniser
     */
    public SessionMap(HttpSession httpSession) {
        super();
        this.httpSession = httpSession;
        // Charger les données initiales de la session
        loadFromSession();
    }
    
    /**
     * Charge toutes les données de HttpSession dans cette Map.
     * Appelé à l'initialisation et peut être rappelé pour resynchroniser.
     */
    private void loadFromSession() {
        super.clear();
        if (httpSession != null) {
            Enumeration<String> attributeNames = httpSession.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attrName = attributeNames.nextElement();
                super.put(attrName, httpSession.getAttribute(attrName));
            }
        }
    }
    
    /**
     * Retourne la HttpSession sous-jacente.
     * @return La HttpSession
     */
    public HttpSession getHttpSession() {
        return httpSession;
    }
    
    // ========== Méthodes de modification - synchronisées avec HttpSession ==========
    
    @Override
    public Object put(String key, Object value) {
        // Mettre à jour HttpSession immédiatement
        if (httpSession != null) {
            httpSession.setAttribute(key, value);
        }
        return super.put(key, value);
    }
    
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m != null) {
            for (Entry<? extends String, ? extends Object> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    @Override
    public Object remove(Object key) {
        // Supprimer de HttpSession immédiatement
        if (httpSession != null && key instanceof String) {
            httpSession.removeAttribute((String) key);
        }
        return super.remove(key);
    }
    
    @Override
    public void clear() {
        // Supprimer tous les attributs de HttpSession
        if (httpSession != null) {
            Enumeration<String> attributeNames = httpSession.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attrName = attributeNames.nextElement();
                httpSession.removeAttribute(attrName);
            }
        }
        super.clear();
    }
    
    // ========== Méthodes de lecture - lecture directe depuis HttpSession ==========
    
    @Override
    public Object get(Object key) {
        // Lire directement depuis HttpSession pour avoir la valeur la plus récente
        if (httpSession != null && key instanceof String) {
            Object sessionValue = httpSession.getAttribute((String) key);
            // Synchroniser le cache local si nécessaire
            if (sessionValue != null) {
                super.put((String) key, sessionValue);
            } else {
                super.remove(key);
            }
            return sessionValue;
        }
        return super.get(key);
    }
    
    @Override
    public boolean containsKey(Object key) {
        // Vérifier dans HttpSession
        if (httpSession != null && key instanceof String) {
            return httpSession.getAttribute((String) key) != null;
        }
        return super.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        // Recharger depuis HttpSession pour être sûr
        loadFromSession();
        return super.containsValue(value);
    }
    
    @Override
    public int size() {
        // Compter les attributs de HttpSession
        if (httpSession != null) {
            int count = 0;
            Enumeration<String> attributeNames = httpSession.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                attributeNames.nextElement();
                count++;
            }
            return count;
        }
        return super.size();
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Set<String> keySet() {
        // Recharger depuis HttpSession pour être sûr
        loadFromSession();
        return super.keySet();
    }
    
    @Override
    public Collection<Object> values() {
        // Recharger depuis HttpSession pour être sûr
        loadFromSession();
        return super.values();
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        // Recharger depuis HttpSession pour être sûr
        loadFromSession();
        return super.entrySet();
    }
    
    @Override
    public String toString() {
        // Recharger depuis HttpSession pour être sûr
        loadFromSession();
        return super.toString();
    }
    
    /**
     * Invalide la session HTTP (déconnexion).
     * Après cet appel, la session est détruite et une nouvelle devra être créée.
     */
    public void invalidate() {
        super.clear();
        if (httpSession != null) {
            try {
                httpSession.invalidate();
            } catch (IllegalStateException e) {
                // Session déjà invalidée
            }
        }
    }
}
