package itu.framework.web;

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

    public HashMap<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }
}
