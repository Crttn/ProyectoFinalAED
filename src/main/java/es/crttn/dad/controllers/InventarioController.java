package es.crttn.dad.controllers;

import es.crttn.dad.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InventarioController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private Button actualizarButton;

    @FXML
    private Button añadirButton;

    @FXML
    private Button buscarButton;

    @FXML
    private TableColumn<?, ?> cantidadColumn;

    @FXML
    private TextField cantidadTetxfield;

    @FXML
    private TextField detallesTextfield;

    @FXML
    private Button eliminarButton;

    @FXML
    private TableColumn<?, ?> fechaColumn;

    @FXML
    private DatePicker fechaTextfield;

    @FXML
    private TableView<?> gestionmovimientosTable;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TextField idProductoTextfield;

    @FXML
    private TableColumn<?, ?> tipoColumn;

    @FXML
    private ComboBox<?> tipocomboBox;

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
