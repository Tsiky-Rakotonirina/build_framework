package itu.framework.web;

import java.util.List;

/**
 * Classe pour encapsuler les réponses JSON de l'API REST
 * Format:
 * {
 *   "statut": "success|error",
 *   "code": 200,
 *   "message": "Description du résultat",
 *   "data": { } ou [ ],
 *   "error": "Description de l'erreur",
 *   "count": nombre d'éléments (pour les listes)
 * }
 */
public class JsonResponse {
    private String statut;
    private int code;
    private String message;
    private Object data;
    private String error;
    private Integer count;

    public JsonResponse() {
    }

    public JsonResponse(String statut, int code, String message) {
        this.statut = statut;
        this.code = code;
        this.message = message;
    }

    public JsonResponse(String statut, int code, String message, Object data) {
        this.statut = statut;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Constructeur pour les erreurs
    public static JsonResponse error(int code, String message, String error) {
        JsonResponse response = new JsonResponse("error", code, message);
        response.setError(error);
        return response;
    }

    // Constructeur pour les succès sans données
    public static JsonResponse success(int code, String message) {
        return new JsonResponse("success", code, message);
    }

    // Constructeur pour les succès avec données
    public static JsonResponse success(int code, String message, Object data) {
        return new JsonResponse("success", code, message, data);
    }

    // Constructeur pour les listes
    public static JsonResponse successList(int code, String message, List<?> data) {
        JsonResponse response = new JsonResponse("success", code, message);
        response.setData(data);
        response.setCount(data != null ? data.size() : 0);
        return response;
    }

    // Getters et Setters
    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
