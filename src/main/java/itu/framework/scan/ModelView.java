package itu.framework.scan;

public class ModelView {

    String view;

    public ModelView() {
    }

    public ModelView(String view) {
        this.setView(view);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        if(view.endsWith(".jsp")) {
            this.view = view;
        } else {
            throw new IllegalArgumentException("La vue doit se terminer par .jsp");
        }
    }
}
