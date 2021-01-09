package controller;

import model.JSONParser;
import model.Model;
import view.View;

public class Controller {
    private View view;
    private Model model;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        JSONParser parser = new JSONParser();
        parser.parse(getClass().getResource("/planverkehr.json").getPath());
    }
}
