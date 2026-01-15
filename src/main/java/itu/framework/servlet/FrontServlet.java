package itu.framework.servlet;

import itu.framework.listener.FrameworkListener;
import itu.framework.scan.ControllerScanner;
import itu.framework.scan.ControllerScanner.MethodInfo;
import itu.framework.web.ModelView;
import itu.framework.web.JsonResponse;
import itu.framework.web.LocalDateAdapter;
import itu.framework.web.UploadFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "FrontServlet", urlPatterns = {"/"}, loadOnStartup = 1)
public class FrontServlet extends HttpServlet {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp, "POST");
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String httpMethod) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        
        // Récupération de l'URI demandée
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = requestURI.substring(contextPath.length());
        
        // Si le path est vide, mettre "/"
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        
        // Récupération des mappings depuis ServletContext
        @SuppressWarnings("unchecked")
        Map<String, ControllerScanner.MethodInfo> mappings = 
            (Map<String, ControllerScanner.MethodInfo>) getServletContext().getAttribute(FrameworkListener.MAPPINGS_KEY);
        
        // Création de la clé METHOD:URL
        String key = httpMethod + ":" + path;
        
        if (mappings == null) {
            sendHtmlMessage(resp, "<p>ServletContext non initialisé (aucun mapping disponible)</p>");
            return;
        }

        ControllerScanner.MethodInfo methodInfo = resolveMethodInfo(req, httpMethod, path, key, mappings);

        if (methodInfo == null) {
            sendHtmlMessage(resp, "<p>Aucun mapping trouvé pour: " + key + "</p>");
            return;
        }
        
        try {
            Method method = methodInfo.getMethod();
            Class<?> returnType = method.getReturnType();
            Object controllerInstance = methodInfo.getControllerClass().getDeclaredConstructor().newInstance();
            Object[] args = buildMethodArguments(req, httpMethod, methodInfo);
            Object result = method.invoke(controllerInstance, args);
            processResult(resp, returnType, result, req, methodInfo);
        } catch (Exception e) {
            renderExecutionError(resp, e);
        }
    }

    private ControllerScanner.MethodInfo resolveMethodInfo(HttpServletRequest req,
                                                           String httpMethod,
                                                           String path,
                                                           String lookupKey,
                                                           Map<String, ControllerScanner.MethodInfo> mappings) {
        ControllerScanner.MethodInfo methodInfo = mappings.get(lookupKey);
        if (methodInfo != null) {
            return methodInfo;
        }

        String anyKey = "ANY:" + path;
        methodInfo = mappings.get(anyKey);
        if (methodInfo != null) {
            return methodInfo;
        }

        return findPatternMatch(req, httpMethod, path, mappings);
    }

    private ControllerScanner.MethodInfo findPatternMatch(HttpServletRequest req,
                                                         String httpMethod,
                                                         String path,
                                                         Map<String, ControllerScanner.MethodInfo> mappings) {
        for (Map.Entry<String, ControllerScanner.MethodInfo> entry : mappings.entrySet()) {
            String mapKey = entry.getKey();
            if (!(mapKey.startsWith(httpMethod + ":") || mapKey.startsWith("ANY:"))) {
                continue;
            }

            ControllerScanner.MethodInfo mi = entry.getValue();
            Pattern p = mi.getPathPattern();
            if (p == null) {
                continue;
            }

            Matcher matcher = p.matcher(path);
            if (!matcher.matches()) {
                continue;
            }

            attachPathVariables(req, mi, matcher);
            return mi;
        }
        return null;
    }

    private void attachPathVariables(HttpServletRequest req,
                                     ControllerScanner.MethodInfo methodInfo,
                                     Matcher matcher) {
        List<String> names = methodInfo.getPathParamNames();
        if (names == null || names.isEmpty()) {
            return;
        }

        for (int i = 0; i < names.size(); i++) {
            req.setAttribute(names.get(i), matcher.group(i + 1));
        }
    }

    private Object[] buildMethodArguments(HttpServletRequest req,
                                          String httpMethod,
                                          ControllerScanner.MethodInfo methodInfo) throws ReflectiveOperationException, ServletException, IOException {
        List<String> paramNames = methodInfo.getParameterNames();
        List<Class<?>> paramTypes = methodInfo.getParameterTypes();
        List<String> paramKeys = methodInfo.getParameterKeys();
        List<java.lang.reflect.Type> genericTypes = methodInfo.getGenericParameterTypes();
        Object[] args = new Object[paramNames.size()];
        
        // Extraire les fichiers uploadés
        Map<String, UploadFile> uploadedFiles = extractUploadedFiles(req);

        for (int i = 0; i < paramNames.size(); i++) {
            String paramName = paramNames.get(i);
            Class<?> paramType = paramTypes.get(i);
            String paramKey = (paramKeys != null && i < paramKeys.size()) ? paramKeys.get(i) : null;
            java.lang.reflect.Type genericType = (genericTypes != null && i < genericTypes.size()) ? genericTypes.get(i) : null;

            if (paramType == UploadFile.class) {
                // Si le type est UploadFile, mettre le premier fichier de getParts
                String fileKey = (paramKey != null) ? paramKey : paramName;
                UploadFile file = uploadedFiles.get(fileKey);
                if (file == null && !uploadedFiles.isEmpty()) {
                    // Prendre le premier fichier disponible
                    file = uploadedFiles.values().iterator().next();
                }
                args[i] = file;
            } else if (paramType == Map.class || paramType == HashMap.class) {
                // Vérifier le type paramétré de la Map
                if (isMapOfUploadFile(genericType)) {
                    // Map<String, UploadFile>
                    args[i] = uploadedFiles;
                } else {
                    // Map<String, Object> - comportement par défaut
                    args[i] = createParamMap(req, httpMethod, uploadedFiles);
                }
            } else if (paramType == String.class) {
                args[i] = resolveStringParameter(req, paramName, paramKey);
            } else if (paramType == byte[].class) {
                // Chercher le fichier uploadé par le nom du paramètre ou @RequestParameter
                String fileKey = (paramKey != null) ? paramKey : paramName;
                UploadFile uploadFile = uploadedFiles.get(fileKey);
                args[i] = (uploadFile != null) ? uploadFile.getContent() : null;
            } else {
                args[i] = bindPojoParameter(req, paramName, paramType, uploadedFiles);
            }
        }

        return args;
    }
    
    /**
     * Vérifie si le type générique est Map<String, UploadFile>
     */
    private boolean isMapOfUploadFile(java.lang.reflect.Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }
        
        ParameterizedType paramType = (ParameterizedType) genericType;
        java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();
        
        if (typeArgs.length != 2) {
            return false;
        }
        
        // Vérifier que le premier argument est String et le second est UploadFile
        return typeArgs[0] == String.class && typeArgs[1] == UploadFile.class;
    }
    
    private Map<String, UploadFile> extractUploadedFiles(HttpServletRequest req) throws ServletException, IOException {
        Map<String, UploadFile> files = new HashMap<>();
        
        try {
            for (Part part : req.getParts()) {
                String partName = part.getName();
                if (part.getSize() > 0) {
                    byte[] fileContent = readPartContent(part);
                    String submittedFileName = part.getSubmittedFileName();
                    String mimeType = part.getContentType();
                    
                    // Extraire l'extension du nom de fichier
                    String extension = "";
                    if (submittedFileName != null && submittedFileName.contains(".")) {
                        extension = submittedFileName.substring(submittedFileName.lastIndexOf(".") + 1);
                    }
                    
                    UploadFile uploadFile = new UploadFile(submittedFileName, extension, mimeType, fileContent);
                    files.put(partName, uploadFile);
                }
            }
        } catch (Exception e) {
            // getParts() échoue si ce n'est pas multipart/form-data, on ignore
        }
        
        return files;
    }
    
    private byte[] readPartContent(Part part) throws IOException {
        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(part.getInputStream());
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        bis.close();
        return baos.toByteArray();
    }

    private Map<String, Object> createParamMap(HttpServletRequest req, String httpMethod, Map<String, UploadFile> uploadedFiles) {
        Map<String, Object> allParams = new HashMap<>();
        if (!"POST".equals(httpMethod)) {
            return allParams;
        }
        java.util.Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            allParams.put(key, req.getParameter(key));
        }
        
        // Ajouter les fichiers uploadés à la Map
        allParams.putAll(uploadedFiles);
        
        return allParams;
    }

    private String resolveStringParameter(HttpServletRequest req, String paramName, String paramKey) {
        Object attrValue = req.getAttribute(paramName);
        if (attrValue != null) {
            return attrValue.toString();
        }

        String paramValue = req.getParameter(paramName);
        if (paramValue != null) {
            return paramValue;
        }

        if (paramKey != null) {
            return req.getParameter(paramKey);
        }

        return null;
    }

    private Object bindPojoParameter(HttpServletRequest req, String paramName, Class<?> paramType, Map<String, UploadFile> uploadedFiles) throws ReflectiveOperationException {
        Object pojoInstance = paramType.getDeclaredConstructor().newInstance();
        Map<String, Integer> arrayAutoIndices = new HashMap<>();
        Map<String, Integer> arrayCurrentIndices = new HashMap<>();
        Map<String, Integer> nestedPathUsage = new HashMap<>();
        java.util.Enumeration<String> parameterNames = req.getParameterNames();

        // Traiter d'abord les paramètres HTTP
        while (parameterNames.hasMoreElements()) {
            String httpParamName = parameterNames.nextElement();
            String prefix = paramName + ".";
            if (!httpParamName.startsWith(prefix)) {
                continue;
            }

            String nestedPath = httpParamName.substring(prefix.length());

            if (isArrayEntryMarker(nestedPath)) {
                ensureArrayEntry(pojoInstance, nestedPath, arrayAutoIndices, arrayCurrentIndices);
                continue;
            }

            String[] values = req.getParameterValues(httpParamName);
            if (values == null || values.length == 0) {
                values = new String[] { req.getParameter(httpParamName) };
            }

            for (String value : values) {
                if (value == null) {
                    continue;
                }
                int usage = nestedPathUsage.getOrDefault(nestedPath, 0);
                boolean forceNewListEntry = usage > 0;
                assignNestedField(pojoInstance, nestedPath, value, arrayAutoIndices, arrayCurrentIndices, false, forceNewListEntry);
                nestedPathUsage.put(nestedPath, usage + 1);
            }
        }
        
        // Traiter ensuite les fichiers uploadés pour les champs UploadFile, Map<String, UploadFile> et byte[]
        for (Field field : paramType.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            
            if (field.getType() == UploadFile.class) {
                // Si le champ est de type UploadFile
                if (uploadedFiles.containsKey(fieldName)) {
                    field.set(pojoInstance, uploadedFiles.get(fieldName));
                } else if (!uploadedFiles.isEmpty()) {
                    // Prendre le premier fichier disponible
                    field.set(pojoInstance, uploadedFiles.values().iterator().next());
                }
            } else if (field.getType() == Map.class || field.getType() == HashMap.class) {
                // Vérifier si c'est Map<String, UploadFile>
                java.lang.reflect.Type genericFieldType = field.getGenericType();
                if (isMapOfUploadFile(genericFieldType)) {
                    field.set(pojoInstance, uploadedFiles);
                }
            } else if (field.getType() == byte[].class) {
                // Compatibilité avec l'ancien système byte[]
                if (uploadedFiles.containsKey(fieldName)) {
                    UploadFile uploadFile = uploadedFiles.get(fieldName);
                    field.set(pojoInstance, uploadFile.getContent());
                }
            }
        }

        return pojoInstance;
    }

    private void processResult(HttpServletResponse resp,
                               Class<?> returnType,
                               Object result,
                               HttpServletRequest req,
                               MethodInfo methodInfo) throws ServletException, IOException {
        // Vérifier si la méthode est annotée avec @Json
        if (methodInfo.isJsonMethod()) {
            handleJsonResponse(resp, returnType, result);
            return;
        }

        if (returnType.equals(String.class)) {
            PrintWriter out = resp.getWriter();
            out.print((String) result);
            return;
        }

        if (returnType.equals(ModelView.class)) {
            ModelView modelView = (ModelView) result;
            HashMap<String, Object> data = modelView.getData();

            if (data != null && !data.isEmpty()) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            String viewPath = modelView.getView();
            if (!viewPath.startsWith("/")) {
                viewPath = "/" + viewPath;
            }
            RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
            dispatcher.forward(req, resp);
            return;
        }

        PrintWriter out = resp.getWriter();
        out.print("Type de retour non supporté: " + returnType.getName());
    }

    private void sendHtmlMessage(HttpServletResponse resp, String message) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print("<html><body>");
        out.print(message);
        out.print("</body></html>");
    }

    private void renderExecutionError(HttpServletResponse resp, Exception e) throws IOException {
        PrintWriter out = resp.getWriter();
        out.print("<html><body>");
        out.print("<h3>Erreur lors de l'exécution de la méthode:</h3>");
        out.print("<pre>" + e.getMessage() + "</pre>");
        out.print("</body></html>");
        e.printStackTrace();
    }

    private static final Pattern ARRAY_SEGMENT_PATTERN = Pattern.compile("^(.+?)\\[(\\d*)\\]$");

    private void assignNestedField(Object root,
                                   String fieldPath,
                                   String value,
                                   Map<String, Integer> arrayAutoIndices,
                                   Map<String, Integer> arrayCurrentIndices,
                                   boolean allowCreateOnly,
                                   boolean forceNewListEntry) throws ReflectiveOperationException {
        if (value == null && !allowCreateOnly) {
            return;
        }

        String[] parts = fieldPath.split("\\.");
        Object current = root;
        StringBuilder pathBuilder = new StringBuilder();

        for (int idx = 0; idx < parts.length; idx++) {
            Segment segment = parseSegment(parts[idx]);
            String fieldName = segment.name;
            String arrayKey = pathBuilder.length() == 0 ? fieldName : pathBuilder + "." + fieldName;

            Field field = findField(current.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
            field.setAccessible(true);

            boolean isLastSegment = idx == parts.length - 1;

            if (segment.isArray) {
                List<Object> list = getOrCreateList(field, current);
                int targetIndex = resolveArrayIndex(arrayKey, segment, forceNewListEntry, arrayAutoIndices, arrayCurrentIndices);
                Object element = ensureListElement(list, targetIndex, field);
                current = element;

                if (isLastSegment) {
                    return;
                }
            } else {
                if (isLastSegment) {
                    if (value == null) {
                        return;
                    }
                    setFieldValue(field, current, value);
                    return;
                }
                Object nextValue = field.get(current);
                if (nextValue == null) {
                    nextValue = field.getType().getDeclaredConstructor().newInstance();
                    field.set(current, nextValue);
                }
                current = nextValue;
            }

            if (pathBuilder.length() == 0) {
                pathBuilder.append(fieldName);
            } else {
                pathBuilder.append('.').append(fieldName);
            }
        }
    }

    private boolean isArrayEntryMarker(String nestedPath) {
        return nestedPath.endsWith("[]") && !nestedPath.contains(".");
    }

    private void ensureArrayEntry(Object root,
                                  String nestedPath,
                                  Map<String, Integer> arrayAutoIndices,
                                  Map<String, Integer> arrayCurrentIndices) throws ReflectiveOperationException {
        assignNestedField(root, nestedPath, null, arrayAutoIndices, arrayCurrentIndices, true, true);
    }

    private int resolveArrayIndex(String arrayKey,
                                  Segment segment,
                                  boolean forceNewEntry,
                                  Map<String, Integer> autoIndices,
                                  Map<String, Integer> currentIndices) {
        if (segment.hasExplicitIndex()) {
            currentIndices.put(arrayKey, segment.explicitIndex);
            return segment.explicitIndex;
        }

        Integer current = currentIndices.get(arrayKey);
        if (current == null || forceNewEntry) {
            int next = autoIndices.getOrDefault(arrayKey, 0);
            autoIndices.put(arrayKey, next + 1);
            current = next;
            currentIndices.put(arrayKey, current);
        }
        return current;
    }

    private List<Object> getOrCreateList(Field field, Object target) throws IllegalAccessException {
        Object existing = field.get(target);
        if (existing == null) {
            List<Object> list = new ArrayList<>();
            field.set(target, list);
            return list;
        }
        if (existing instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) existing;
            return list;
        }
        throw new IllegalArgumentException("Champ " + field.getName() + " n'est pas une List");
    }

    private Object ensureListElement(List<Object> list, int index, Field field) throws ReflectiveOperationException {
        while (list.size() <= index) {
            list.add(null);
        }
        Object element = list.get(index);
        if (element == null) {
            Class<?> elementClass = getListElementType(field);
            if (elementClass == null) {
                throw new IllegalArgumentException("Impossible de déterminer le type des éléments pour " + field.getName());
            }
            element = elementClass.getDeclaredConstructor().newInstance();
            list.set(index, element);
        }
        return element;
    }

    private Class<?> getListElementType(Field field) {
        java.lang.reflect.Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?>) {
                return (Class<?>) args[0];
            }
        }
        return null;
    }

    private Segment parseSegment(String raw) {
        Matcher matcher = ARRAY_SEGMENT_PATTERN.matcher(raw);
        if (matcher.matches()) {
            String name = matcher.group(1);
            String indexPart = matcher.group(2);
            if (indexPart != null && !indexPart.isEmpty()) {
                return new Segment(name, true, Integer.parseInt(indexPart));
            }
            return new Segment(name, true, null);
        }
        return new Segment(raw, false, null);
    }

    private Field findField(Class<?> targetClass, String name) {
        Class<?> current = targetClass;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private void setFieldValue(Field field, Object target, String value) throws IllegalAccessException {
        if (value == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            field.set(target, value);
            return;
        }
        if (value.isEmpty()) {
            return;
        }
        if (fieldType == int.class || fieldType == Integer.class) {
            field.set(target, Integer.parseInt(value));
        } else if (fieldType == long.class || fieldType == Long.class) {
            field.set(target, Long.parseLong(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(target, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(target, Boolean.parseBoolean(value));
        } else if (fieldType == LocalDate.class) {
            field.set(target, LocalDate.parse(value));
        } else {
            field.set(target, value);
        }
    }

    private static final class Segment {
        final String name;
        final boolean isArray;
        final Integer explicitIndex;

        private Segment(String name, boolean isArray, Integer explicitIndex) {
            this.name = name;
            this.isArray = isArray;
            this.explicitIndex = explicitIndex;
        }

        boolean hasExplicitIndex() {
            return explicitIndex != null;
        }
    }

    private void handleJsonResponse(HttpServletResponse resp, Class<?> returnType, Object result) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        JsonResponse jsonResponse;
        
        // Si le résultat est déjà une JsonResponse
        if (result instanceof JsonResponse) {
            jsonResponse = (JsonResponse) result;
        }
        // Si le résultat est un ModelView, extraire les données
        else if (returnType.equals(ModelView.class)) {
            ModelView modelView = (ModelView) result;
            HashMap<String, Object> data = modelView.getData();
            jsonResponse = JsonResponse.success(200, "Résultat retourné", data.isEmpty() ? null : data);
        }
        // Sinon, envelopper l'objet dans une réponse de succès
        else {
            jsonResponse = JsonResponse.success(200, "Résultat retourné", result);
        }
        
        // Sérialiser la réponse en JSON et l'envoyer
        String jsonString = gson.toJson(jsonResponse);
        out.print(jsonString);
        out.flush();
    }

}

