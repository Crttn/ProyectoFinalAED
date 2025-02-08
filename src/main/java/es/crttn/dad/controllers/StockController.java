package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Producto;
import es.crttn.dad.modelos.Stock;
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
import javafx.util.StringConverter;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private TableView<Stock> stockTableView;

    @FXML
    private TableColumn<Stock, String> nombreProductoColumn;

    @FXML
    private TableColumn<Stock, String> IdProductoColumn;

    @FXML
    private TableColumn<Stock, Integer> cantidadColumn;

    // Declarar la lista con el tipo correcto
    private ObservableList<Stock> stockList;

    // Mapa para almacenar la asociación: id de producto -> nombre
    private Map<ObjectId, String> productNameMap;

    public StockController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menus/StockView.fxml"));
            // Si ya está definido fx:controller en el FXML, elimina la siguiente línea:
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productNameMap = new HashMap<>();

        // Configurar la columna que muestra el nombre del producto
        nombreProductoColumn.setCellValueFactory(cellData -> {
            ObjectId prodId = cellData.getValue().getProductoId();
            String prodName = productNameMap.get(prodId);
            return new SimpleStringProperty(prodName != null ? prodName : "N/A");
        });

        // Configurar la columna del ID del producto
        IdProductoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductoId().toHexString()));

        // Configurar la columna de cantidad disponible
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));

        stockList = FXCollections.observableArrayList();
        stockTableView.setItems(stockList);

        // Mostrar los datos y actualizar
        showData();
    }


    private void loadProductNameMap() {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        // Asegúrate de que el nombre de la colección es correcto (por ejemplo, "productos")
        MongoCollection<Producto> productoCollection = db.getCollection("productos", Producto.class);
        List<Producto> productList = productoCollection.find().into(new ArrayList<>());
        productNameMap.clear();
        for (Producto p : productList) {
            productNameMap.put(p.getId(), p.getNombre());
        }
    }


    private void showData() {
        loadProductNameMap();

        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Stock> collection = db.getCollection("stock", Stock.class);
        List<Stock> stock = collection.find().into(new ArrayList<>());

        Platform.runLater(() -> {
            stockList.setAll(stock);
        });
    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }

    @FXML
    void onAgregarAction(ActionEvent event) {
        Dialog<Stock> dialog = new Dialog<>();
        dialog.setTitle("Agregar Stock");
        dialog.setHeaderText("Ingrese los datos del nuevo stock");

        ButtonType addButtonType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<Producto> productoComboBox = new ComboBox<>();
        productoComboBox.setPromptText("Seleccione un producto");

        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Producto> productoCollection = db.getCollection("productos", Producto.class);
        List<Producto> productos = productoCollection.find().into(new ArrayList<>());
        productoComboBox.setItems(FXCollections.observableArrayList(productos));

        productoComboBox.setConverter(new StringConverter<Producto>() {
            @Override
            public String toString(Producto producto) {
                return producto != null ? producto.getNombre() : "";
            }
            @Override
            public Producto fromString(String string) {
                return null; // No se utiliza
            }
        });

        TextField cantidadField = new TextField();
        cantidadField.setPromptText("Cantidad Disponible");

        grid.add(new Label("Producto:"), 0, 0);
        grid.add(productoComboBox, 1, 0);
        grid.add(new Label("Cantidad Disponible:"), 0, 1);
        grid.add(cantidadField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Producto selectedProducto = productoComboBox.getSelectionModel().getSelectedItem();
                String cantidadStr = cantidadField.getText();

                if (selectedProducto == null || cantidadStr.isEmpty()) {
                    return null;
                }
                try {
                    int cantidadDisponible = Integer.parseInt(cantidadStr);
                    Stock nuevoStock = new Stock(selectedProducto.getId(), cantidadDisponible);
                    System.out.println("Creado nuevo Stock: " + nuevoStock);
                    return nuevoStock;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Stock> result = dialog.showAndWait();
        result.ifPresent(this::agregarStockABaseDeDatos);
    }

    private void agregarStockABaseDeDatos(Stock stock) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Stock> collection = db.getCollection("stock", Stock.class);
        collection.insertOne(stock);
        showData();
    }

    @FXML
    void onBuscarAction(ActionEvent event) {
    }

    @FXML
    void onEliminarAction(ActionEvent event) {
    }

    @FXML
    void onModificarAction(ActionEvent event) {
    }
}
