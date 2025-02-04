package es.crttn.dad.controllers;

import es.crttn.dad.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InventarioController implements Initializable {

    @FXML
    private BorderPane root;

    public InventarioController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menus/InventarioView.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }
}
