package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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

    public void actualizarStock(ObjectId productoId, int cantidad) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Stock> collection = db.getCollection("stock", Stock.class);

        Stock stock = collection.find(Filters.eq("producto_id", productoId)).first();
        if (stock != null) {
            int nuevaCantidad = stock.getCantidadDisponible() - cantidad;
            collection.updateOne(Filters.eq("producto_id", productoId), Updates.set("cantidad_disponible", nuevaCantidad));
            showData();
        }
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

    public void refreshStock() { showData(); }


    @FXML
    void onBuscarAction(ActionEvent event) {
        // Obtener el texto de búsqueda
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Stock");
        dialog.setHeaderText("Ingrese el nombre del producto para buscar");
        dialog.setContentText("Nombre del Producto:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchText -> {
            if (searchText != null && !searchText.isEmpty()) {
                // Filtrar los resultados de stock según el nombre del producto
                filterStockByProductName(searchText);
            }
        });
    }

    private void filterStockByProductName(String searchText) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Stock> stockCollection = db.getCollection("stock", Stock.class);
        List<Stock> allStock = stockCollection.find().into(new ArrayList<>());

        // Filtrar los productos que contengan el texto de búsqueda en su nombre
        List<Stock> filteredStock = new ArrayList<>();
        for (Stock stock : allStock) {
            // Obtener el nombre del producto correspondiente al stock
            String productName = productNameMap.get(stock.getProductoId());
            if (productName != null && productName.toLowerCase().contains(searchText.toLowerCase())) {
                filteredStock.add(stock);
            }
        }

        // Actualizar la vista con los resultados filtrados
        Platform.runLater(() -> {
            stockList.setAll(filteredStock);
        });
    }

    @FXML
    void onEliminarAction(ActionEvent event) {
        // Obtener el stock seleccionado en la tabla
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();

        // Si no se seleccionó ningún stock, mostrar un mensaje de error
        if (selectedStock == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seleccionado");
            alert.setHeaderText("No se ha seleccionado ningún stock");
            alert.setContentText("Por favor, seleccione un stock para eliminar.");
            alert.showAndWait();
            return;
        }

        // Mostrar un cuadro de confirmación antes de eliminar
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmación de eliminación");
        confirmAlert.setHeaderText("¿Está seguro de que desea eliminar este stock?");
        confirmAlert.setContentText("El stock de este producto será eliminado permanentemente.");

        // Si el usuario confirma, eliminar el stock
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Eliminar de la base de datos
            MongoDatabase db = DatabaseConector.getInstance().getDatabase();
            MongoCollection<Stock> stockCollection = db.getCollection("stock", Stock.class);

            // Eliminar el stock por su ID
            stockCollection.deleteOne(new org.bson.Document("_id", selectedStock.getId()));

            // Actualizar la vista (refrescar la tabla)
            showData();

            // Mostrar un mensaje de éxito
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Stock Eliminado");
            successAlert.setHeaderText("El stock se ha eliminado correctamente.");
            successAlert.showAndWait();
        }
    }


    @FXML
    void onModificarAction(ActionEvent event) {
        // Obtener el stock seleccionado en la tabla
        Stock selectedStock = stockTableView.getSelectionModel().getSelectedItem();

        // Si no se seleccionó ningún stock, mostrar un mensaje de error
        if (selectedStock == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Seleccionado");
            alert.setHeaderText("No se ha seleccionado ningún stock");
            alert.setContentText("Por favor, seleccione un stock para modificar.");
            alert.showAndWait();
            return;
        }

        // Crear un cuadro de diálogo para modificar la cantidad disponible
        Dialog<Stock> dialog = new Dialog<>();
        dialog.setTitle("Modificar Stock");
        dialog.setHeaderText("Ingrese la nueva cantidad para el stock del producto");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Campo de texto para ingresar la nueva cantidad
        TextField cantidadField = new TextField();
        cantidadField.setText(String.valueOf(selectedStock.getCantidadDisponible())); // Prellenar con la cantidad actual
        cantidadField.setPromptText("Cantidad Disponible");

        grid.add(new Label("Cantidad Disponible:"), 0, 0);
        grid.add(cantidadField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String cantidadStr = cantidadField.getText();

                if (cantidadStr.isEmpty()) {
                    return null;
                }
                try {
                    int nuevaCantidad = Integer.parseInt(cantidadStr);
                    selectedStock.setCantidadDisponible(nuevaCantidad);
                    return selectedStock;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Stock> result = dialog.showAndWait();
        result.ifPresent(this::modificarStockEnBaseDeDatos);
    }

    private void modificarStockEnBaseDeDatos(Stock stock) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Stock> collection = db.getCollection("stock", Stock.class);

        // Actualizar el stock en la base de datos por su ID
        collection.replaceOne(new org.bson.Document("_id", stock.getId()), stock);

        // Actualizar la vista (refrescar la tabla)
        showData();

        // Mostrar un mensaje de éxito
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Stock Modificado");
        successAlert.setHeaderText("El stock ha sido actualizado correctamente.");
        successAlert.showAndWait();
    }
}
