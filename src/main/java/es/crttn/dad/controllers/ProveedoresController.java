package es.crttn.dad.controllers;

import es.crttn.dad.App;
import es.crttn.dad.modelos.Producto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProveedoresController implements Initializable {

    public ProveedoresController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menus/ProveedoresView.fxml"));
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
    private TableColumn<?, ?> contactoColumn;

    @FXML
    private TableColumn<?, ?> direccionColumn;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> nombreColumn;

    @FXML
    private TableView<?> proveedoresTableView;

    @FXML
    private BorderPane root;

    @FXML
    void onAgregarAction(ActionEvent event) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Agregar Proveedor");
        dialog.setHeaderText("Ingrese los datos del nuevo proveedor");

        ButtonType addButtonType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nombreField = new TextField();
        TextField direccionField = new TextField();
        TextField contactoField = new TextField();

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Contacto:"), 0, 2);
        grid.add(contactoField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<Producto> result = dialog.showAndWait();



    }

    @FXML
    void onBackAction(ActionEvent event) {
        {
            App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
        }
    }

    @FXML
    void onEliminarAction(ActionEvent event) {

    }

    @FXML
    void onModificarAction(ActionEvent event) {

    }
}
