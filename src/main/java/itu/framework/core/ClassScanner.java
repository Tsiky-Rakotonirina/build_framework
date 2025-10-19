package itu.framework.core;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    public static List<Class<?>> scan(String basePackage) {
        List<Class<?>> classes = new ArrayList<>();
        String pkgPath = basePackage.replace('.', '/');
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(pkgPath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanFile(basePackage, URLDecoder.decode(url.getFile(), java.nio.charset.StandardCharsets.UTF_8), classes);
                    case "jar" -> scanJar(basePackage, pkgPath, url, classes);
                }
            }
        } catch (IOException ignored) {}
        return classes;
    }

    private static void scanFile(String base, String path, List<Class<?>> out) {
        File dir = new File(path);
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) scanFile(base + "." + f.getName(), f.getAbsolutePath(), out);
            else if (f.getName().endsWith(".class")) {
                String cn = base + "." + f.getName().substring(0, f.getName().length() - 6);
                try { out.add(Class.forName(cn)); } catch (ClassNotFoundException ignored) {}
            }
        }
    }

    private static void scanJar(String base, String pkgPath, URL url, List<Class<?>> out) {
        try {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            try (JarFile jar = conn.getJarFile()) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry e = entries.nextElement();
                    String name = e.getName();
                    if (name.startsWith(pkgPath) && name.endsWith(".class") && !e.isDirectory()) {
                        String cn = name.substring(0, name.length() - 6).replace('/', '.');
                        try { out.add(Class.forName(cn)); } catch (ClassNotFoundException ignored) {}
                    }
                }
            }
        } catch (IOException ignored) {}
    }
}
