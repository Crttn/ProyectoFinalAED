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

public class MenuController implements Initializable {

    ProductosController pc;
    StockController sc;
    InventarioController ic;

    @FXML
    private BorderPane root;

    public MenuController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuView.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        pc = new ProductosController();
        sc = new StockController();
        ic = new InventarioController();
    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    void onProductosAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(pc.getRoot());
    }

    @FXML
    void onStockAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(sc.getRoot());
    }

    @FXML
    void onInventarioAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(ic.getRoot());
    }
}