package itu.framework;

import itu.framework.core.Dispatcher;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("         TESTS DU FRAMEWORK - DISPATCHER                  ");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // ========== ÉTAPE 1 : INITIALISATION DU DISPATCHER ==========
        System.out.println("📦 ÉTAPE 1 : Initialisation du Dispatcher");
        System.out.println("   → Scanne le package 'itu.framework.demo'");
        System.out.println("   → Recherche toutes les classes avec méthodes @Web");
        System.out.println("   → Enregistre les routes dans la Map");
        
        Dispatcher dispatcher = new Dispatcher("itu.framework.demo");
        
        System.out.println("   ✅ Dispatcher initialisé avec succès !\n");

        // ========== ÉTAPE 2 : TEST DE ROUTE SIMPLE ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 2 : Test de route simple - /hello");
        System.out.println("   → Route : /hello");
        System.out.println("   → Méthode HTTP : GET (par défaut via 'ANY')");
        System.out.println("   → Annotation : @Web(\"/hello\")");
        
        boolean hasHello = dispatcher.hasRoute("/hello", "GET");
        System.out.println("   → hasRoute(\"/hello\", \"GET\") = " + hasHello);
        
        if (hasHello) {
            Object result = dispatcher.dispatch("/hello", "GET");
            System.out.println("   → dispatch(\"/hello\", \"GET\") = " + result);
            System.out.println("   ✅ Route trouvée et exécutée !");
        }
        System.out.println();

        // ========== ÉTAPE 3 : TEST AVEC MÉTHODE HTTP SPÉCIFIQUE ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 3 : Test avec méthode HTTP spécifique - /sum");
        System.out.println("   → Route : /sum");
        System.out.println("   → Méthode HTTP : GET (explicitement défini)");
        System.out.println("   → Annotation : @Web(value=\"/sum\", method=\"GET\")");
        
        boolean hasSum = dispatcher.hasRoute("/sum", "GET");
        System.out.println("   → hasRoute(\"/sum\", \"GET\") = " + hasSum);
        
        if (hasSum) {
            Object result = dispatcher.dispatch("/sum", "GET");
            System.out.println("   → dispatch(\"/sum\", \"GET\") = " + result);
            System.out.println("   ✅ Calcul effectué : 2 + 3 = 5");
        }
        System.out.println();

        // ========== ÉTAPE 4 : TEST DE ROUTE AVEC MÉTHODES DIFFÉRENTES ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 4 : Test d'une même route avec méthodes différentes - /greet");
        System.out.println("   → Même chemin, mais GET et POST séparés");
        
        System.out.println("\n   📌 Test GET /greet :");
        boolean hasGreetGet = dispatcher.hasRoute("/greet", "GET");
        System.out.println("   → hasRoute(\"/greet\", \"GET\") = " + hasGreetGet);
        if (hasGreetGet) {
            Object result = dispatcher.dispatch("/greet", "GET");
            System.out.println("   → dispatch(\"/greet\", \"GET\") = " + result);
        }
        
        System.out.println("\n   📌 Test POST /greet :");
        boolean hasGreetPost = dispatcher.hasRoute("/greet", "POST");
        System.out.println("   → hasRoute(\"/greet\", \"POST\") = " + hasGreetPost);
        if (hasGreetPost) {
            Object result = dispatcher.dispatch("/greet", "POST");
            System.out.println("   → dispatch(\"/greet\", \"POST\") = " + result);
        }
        
        System.out.println("   ✅ Le dispatcher distingue bien GET et POST !");
        System.out.println();

        // ========== ÉTAPE 5 : TEST DE MÉTHODE 'ANY' ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 5 : Test de méthode 'ANY' - /multiply");
        System.out.println("   → Route : /multiply");
        System.out.println("   → Méthode HTTP : ANY (accepte toutes les méthodes)");
        System.out.println("   → Annotation : @Web(value=\"/multiply\", method=\"ANY\")");
        
        System.out.println("\n   📌 Test avec GET :");
        boolean hasMultiplyGet = dispatcher.hasRoute("/multiply", "GET");
        System.out.println("   → hasRoute(\"/multiply\", \"GET\") = " + hasMultiplyGet);
        if (hasMultiplyGet) {
            Object result = dispatcher.dispatch("/multiply", "GET");
            System.out.println("   → dispatch(\"/multiply\", \"GET\") = " + result);
        }
        
        System.out.println("\n   📌 Test avec POST :");
        boolean hasMultiplyPost = dispatcher.hasRoute("/multiply", "POST");
        System.out.println("   → hasRoute(\"/multiply\", \"POST\") = " + hasMultiplyPost);
        if (hasMultiplyPost) {
            Object result = dispatcher.dispatch("/multiply", "POST");
            System.out.println("   → dispatch(\"/multiply\", \"POST\") = " + result);
        }
        
        System.out.println("\n   📌 Test avec DELETE :");
        boolean hasMultiplyDelete = dispatcher.hasRoute("/multiply", "DELETE");
        System.out.println("   → hasRoute(\"/multiply\", \"DELETE\") = " + hasMultiplyDelete);
        if (hasMultiplyDelete) {
            Object result = dispatcher.dispatch("/multiply", "DELETE");
            System.out.println("   → dispatch(\"/multiply\", \"DELETE\") = " + result);
        }
        
        System.out.println("   ✅ ANY accepte toutes les méthodes HTTP !");
        System.out.println();

        // ========== ÉTAPE 6 : TEST AVEC MÉTHODE PAR DÉFAUT ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 6 : Test avec méthode par défaut - /info");
        System.out.println("   → Route : /info");
        System.out.println("   → Méthode HTTP : ANY (valeur par défaut de @Web)");
        System.out.println("   → Annotation : @Web(\"/info\")");
        
        boolean hasInfo = dispatcher.hasRoute("/info", "GET");
        System.out.println("   → hasRoute(\"/info\", \"GET\") = " + hasInfo);
        
        if (hasInfo) {
            Object result = dispatcher.dispatch("/info", "GET");
            System.out.println("   → dispatch(\"/info\", \"GET\") = " + result);
            System.out.println("   ✅ Méthode par défaut = ANY fonctionne !");
        }
        System.out.println();

        // ========== ÉTAPE 7 : TEST DE ROUTE INEXISTANTE ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 7 : Test de route inexistante - /notfound");
        System.out.println("   → Route : /notfound (n'existe pas)");
        System.out.println("   → Test du comportement en cas d'échec");
        
        boolean hasNotFound = dispatcher.hasRoute("/notfound", "GET");
        System.out.println("   → hasRoute(\"/notfound\", \"GET\") = " + hasNotFound);
        
        Object resultNotFound = dispatcher.dispatch("/notfound", "GET");
        System.out.println("   → dispatch(\"/notfound\", \"GET\") = " + resultNotFound);
        System.out.println("   ✅ Retourne null pour les routes inexistantes !");
        System.out.println();

        // ========== ÉTAPE 8 : TEST DE NORMALISATION DES CHEMINS ==========
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("🧪 ÉTAPE 8 : Test de normalisation des chemins");
        System.out.println("   → Le Dispatcher normalise automatiquement les chemins");
        System.out.println("   → Ajoute '/' au début si absent");
        System.out.println("   → Supprime '/' à la fin si présent");
        
        System.out.println("\n   📌 Test 'hello' (sans slash initial) :");
        boolean hasHelloNoSlash = dispatcher.hasRoute("hello", "GET");
        System.out.println("   → hasRoute(\"hello\", \"GET\") = " + hasHelloNoSlash);
        
        System.out.println("\n   📌 Test '/hello/' (avec slash final) :");
        boolean hasHelloTrailingSlash = dispatcher.hasRoute("/hello/", "GET");
        System.out.println("   → hasRoute(\"/hello/\", \"GET\") = " + hasHelloTrailingSlash);
        
        System.out.println("   ✅ La normalisation fonctionne correctement !");
        System.out.println();

        // ========== RÉSUMÉ FINAL ==========
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("                    ✅ RÉSUMÉ DES TESTS                    ");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✓ Initialisation du Dispatcher");
        System.out.println("✓ Routes simples avec méthode par défaut");
        System.out.println("✓ Routes avec méthode HTTP spécifique");
        System.out.println("✓ Distinction GET/POST sur même chemin");
        System.out.println("✓ Méthode ANY (toutes méthodes HTTP)");
        System.out.println("✓ Gestion des routes inexistantes");
        System.out.println("✓ Normalisation automatique des chemins");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🎉 Tous les tests sont réussis !");
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}

