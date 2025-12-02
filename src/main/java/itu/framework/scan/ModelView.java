package itu.framework.scan;

import java.util.HashMap;

public class ModelView {

    String view;
    private HashMap<String, Object> data;

    public ModelView() {
        data = new HashMap<>();
    }

    public ModelView(String view) {
        data = new HashMap<>();
        this.setView(view);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        if (view.endsWith(".jsp")) {
            this.view = view;
        } else {
            throw new IllegalArgumentException("La vue doit se terminer par .jsp");
        }
    }

    /** Retourne le HashMap contenant les données */
    public HashMap<String, Object> getData() {
        return data;
    }

    /** Ajoute une donnée au model */
    public void addData(String key, Object value) {
        data.put(key, value);
    }

    /** Récupère une valeur du model */
    public Object getData(String key) {
        return data.get(key);
    }
}
