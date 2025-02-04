package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Producto;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ProductosController implements Initializable {

    @FXML
    private TableView<Producto> productosTableView;

    @FXML
    private TableColumn<Producto, String> idColumn;

    @FXML
    private TableColumn<Producto, String> idProveedorColumn;

    @FXML
    private TableColumn<Producto, String> nombreColumn;

    @FXML
    private TableColumn<Producto, String> categoriaColumn;

    @FXML
    private TableColumn<Producto, Double> precioColumn;

    @FXML
    private BorderPane root;

    ObservableList productosList;

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

        // Convierte el tipo ObjectID a strign para poder manejarlo
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toHexString()));
        idProveedorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProveedorId().toHexString()));

        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        precioColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());

        productosList = FXCollections.observableArrayList();
        productosTableView.setItems(productosList);

        showData();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void showData() {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Producto> collection = db.getCollection("productos", Producto.class);

        List<Producto> productos = collection.find().into(new ArrayList<>()); // Obtener los datos

        Platform.runLater(() -> {
            productosList.setAll(productos); // Actualizar la lista observable en la UI
        });
    }

    @FXML
    void onAgregarAction(ActionEvent event) {

    }

    @FXML
    void onModificarAction(ActionEvent event) {

    }

    @FXML
    void onEliminarAction(ActionEvent event) {

    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }
}
