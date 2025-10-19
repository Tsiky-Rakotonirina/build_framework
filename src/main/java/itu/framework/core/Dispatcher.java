package itu.framework.core;

import itu.framework.annotation.Web;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dispatcher {
    public record Handler(Object instance, Method method, String httpMethod) {}

    private final Map<String, Handler> routes = new HashMap<>();

    public Dispatcher(String basePackage) {
        register(basePackage);
    }

    private void register(String basePackage) {
        List<Class<?>> classes = ClassScanner.scan(basePackage);
        for (Class<?> c : classes) {
            for (Method m : c.getDeclaredMethods()) {
                Web web = m.getAnnotation(Web.class);
                if (web == null) continue;
                try {
                    m.setAccessible(true);
                    Object inst = c.getDeclaredConstructor().newInstance();
                    String path = normalize(web.value());
                    String key = path + "#" + web.method().toUpperCase();
                    routes.put(key, new Handler(inst, m, web.method().toUpperCase()));
                } catch (ReflectiveOperationException ignored) {}
            }
        }
    }

    private String normalize(String p) {
        if (p == null || p.isEmpty()) return "/";
        if (!p.startsWith("/")) p = "/" + p;
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    public boolean hasRoute(String path, String method) {
        String key = normalize(path) + "#" + method.toUpperCase();
        String any = normalize(path) + "#ANY";
        return routes.containsKey(key) || routes.containsKey(any);
    }

    public Object dispatch(String path, String method, Object... args) throws Exception {
        String key = normalize(path) + "#" + method.toUpperCase();
        Handler h = routes.getOrDefault(key, routes.get(normalize(path) + "#ANY"));
        if (h == null) return null;
        return h.method.invoke(h.instance, args);
    }
}
