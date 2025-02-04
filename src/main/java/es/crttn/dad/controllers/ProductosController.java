package es.crttn.dad.controllers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ProductosController implements Initializable {

    @FXML
    private BorderPane root;

    public ProductosController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menus/ProductosView.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        MongoDatabase database = DatabaseConector.getInstance().getDatabase();

        System.out.println("✅ Conexión exitosa a la base de datos: " + database.getName());
    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }
}
