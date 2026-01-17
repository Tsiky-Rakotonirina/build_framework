package itu.framework.listener;

import itu.framework.scan.ControllerScanner;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.Map;

/**
 * Listener qui s'exécute au démarrage de l'application
 * Il scanne les contrôleurs et sauvegarde les mappings dans le ServletContext
 * 
 * @WebListener permet de déclarer ce listener sans avoir besoin de web.xml
 */
@WebListener
public class FrameworkListener implements ServletContextListener {
    
    public static final String MAPPINGS_KEY = "urlMappings";
    public static final String SCAN_PACKAGE_PARAM = "scanPackage";
    public static final String AUTH_ATTRIBUTE_KEY = "authAttribute";
    public static final String ROLE_ATTRIBUTE_KEY = "roleAttribute";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("\n========================================");
        System.out.println("    FRAMEWORK INITIALIZATION START");
        System.out.println("========================================");
        
        ServletContext servletContext = sce.getServletContext();
        
        // Récupération du package à scanner depuis web.xml
        String scanPackage = servletContext.getInitParameter(SCAN_PACKAGE_PARAM);
        
        if (scanPackage == null || scanPackage.trim().isEmpty()) {
            System.err.println("[FrameworkListener] ERREUR: Le paramètre '" + SCAN_PACKAGE_PARAM + 
                             "' n'est pas défini dans web.xml");
            System.err.println("[FrameworkListener] Veuillez ajouter dans web.xml:");
            System.err.println("  <context-param>");
            System.err.println("    <param-name>" + SCAN_PACKAGE_PARAM + "</param-name>");
            System.err.println("    <param-value>votre.package.controllers</param-value>");
            System.err.println("  </context-param>");
            return;
        }
        
        System.out.println("[FrameworkListener] Package à scanner: " + scanPackage);
        
        // Récupération des paramètres d'autorisation depuis web.xml (init-param du servlet)
        String authAttribute = servletContext.getInitParameter(AUTH_ATTRIBUTE_KEY);
        String roleAttribute = servletContext.getInitParameter(ROLE_ATTRIBUTE_KEY);
        
        if (authAttribute != null && !authAttribute.trim().isEmpty()) {
            servletContext.setAttribute(AUTH_ATTRIBUTE_KEY, authAttribute);
            System.out.println("[FrameworkListener] Auth attribute configuré: " + authAttribute);
        }
        
        if (roleAttribute != null && !roleAttribute.trim().isEmpty()) {
            servletContext.setAttribute(ROLE_ATTRIBUTE_KEY, roleAttribute);
            System.out.println("[FrameworkListener] Role attribute configuré: " + roleAttribute);
        }
        
        // Scan des contrôleurs et récupération des mappings
        // Map avec clé = "METHOD:URL" et valeur = MethodInfo (classe + méthode)
        Map<String, ControllerScanner.MethodInfo> mappings = ControllerScanner.scanControllers(scanPackage);
        
        // Sauvegarde des mappings dans le ServletContext
        servletContext.setAttribute(MAPPINGS_KEY, mappings);
        
        System.out.println("[FrameworkListener] " + mappings.size() + " mapping(s) sauvegardé(s) dans ServletContext");
        System.out.println("========================================");
        System.out.println("    FRAMEWORK INITIALIZATION COMPLETE");
        System.out.println("========================================\n");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[FrameworkListener] Application arrêtée");
    }
}
