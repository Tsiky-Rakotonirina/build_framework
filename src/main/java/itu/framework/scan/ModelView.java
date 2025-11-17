package itu.framework.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelView {

    String view;

    // Remplace la simple map par une liste de HashMap
    private List<HashMap<String, Object>> modelList;

    public ModelView() {
        modelList = new ArrayList<>();
    }

    public ModelView(String view) {
        modelList = new ArrayList<>();
        this.setView(view);
    }

    /** Retourne la liste des maps de données (peut être vide) */
    public List<HashMap<String, Object>> getModelList() {
        return modelList;
    }

    /** Retourne la première map si elle existe, sinon null */
    public HashMap<String, Object> getFirstModel() {
        return modelList.isEmpty() ? null : modelList.get(0);
    }

    public String getView() {
        return view;
    }

    /** Ajoute une map complète au model */
    public void addModel(HashMap<String, Object> map) {
        if (map != null) modelList.add(map);
    }

    /** Ajoute un couple clé/valeur au premier map (crée le map s'il n'existe pas) */
    public void addModelEntry(String key, Object value) {
        if (modelList.isEmpty()) modelList.add(new HashMap<>());
        modelList.get(0).put(key, value);
    }

    public void setView(String view) {
        if (view.endsWith(".jsp")) {
            this.view = view;
        } else {
            throw new IllegalArgumentException("La vue doit se terminer par .jsp");
        }
    }
}
