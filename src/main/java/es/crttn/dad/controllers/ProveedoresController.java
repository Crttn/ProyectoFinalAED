package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Producto;
import es.crttn.dad.modelos.Proveedor;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toHexString()));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        direccionColumn.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        contactoColumn.setCellValueFactory(new PropertyValueFactory<>("contacto"));

        proveedoresList = FXCollections.observableArrayList();
        proveedoresTableView.setItems(proveedoresList);

        showData();

    }


    private void showData() {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);
        List<Proveedor> proveedores = collection.find().into(new ArrayList<>());

        for (Proveedor p : proveedores) {
            System.out.println("ID: " + p.getId().toHexString());
            System.out.println("nombre: " + p.getNombre());
            System.out.println("Dirección: " + p.getDireccion());
            System.out.println("Contacto: " + p.getContacto());
            System.out.println("-----------------------------");
        }

        Platform.runLater(() -> {
            proveedoresList.setAll(proveedores);
        });
    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    private TableView<Proveedor> proveedoresTableView;

    @FXML
    private TableColumn<Proveedor, String> contactoColumn;

    @FXML
    private TableColumn<Proveedor, String> direccionColumn;

    @FXML
    private TableColumn<Proveedor, String> idColumn;

    @FXML
    private TableColumn<Proveedor, String> nombreColumn;

    @FXML
    private BorderPane root;

    @FXML
    void onAgregarAction(ActionEvent event) {
        Dialog<Proveedor> dialog = new Dialog<>();
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

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String nombre = nombreField.getText();
                    String direccion = direccionField.getText();
                    String contacto = contactoField.getText();

                    if (nombre.isEmpty() || direccion.isEmpty() || contacto.isEmpty()) {
                        // mostrarAlerta("Error", "Todos los campos son obligatorios.");
                        return null;
                    }

                    Proveedor nuevoProveedor = new Proveedor(nombre, direccion, contacto);
                    return nuevoProveedor;
                } catch (NumberFormatException e) {
                    return null;
                }


            }

            return null;
        });


        Optional<Proveedor> result = dialog.showAndWait();

        result.ifPresent(this::agregarProveedorABaseDeDatos);

    }

    private void agregarProveedorABaseDeDatos(Proveedor proveedor) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);

        try {
            collection.insertOne(proveedor);
            proveedoresList.add(proveedor); // Agregar a la lista observable
            proveedoresTableView.refresh();
        } catch (Exception e) {
            // mostrarAlerta("Error", "No se pudo agregar el producto.");
            e.printStackTrace();
        }
    }


    ObservableList proveedoresList;

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
