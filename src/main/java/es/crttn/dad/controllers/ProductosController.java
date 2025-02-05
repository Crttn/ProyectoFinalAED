package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Producto;
import es.crttn.dad.modelos.Proveedor;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
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
import java.util.*;

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

    public void refreshProducts() {
        showData();
    }

    @FXML
    void onAgregarAction(ActionEvent event) {
        // Crear un cuadro de diálogo personalizado
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Agregar Producto");
        dialog.setHeaderText("Ingrese los datos del nuevo producto");

        // Botones OK y Cancelar
        ButtonType addButtonType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Campos de entrada
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nombreField = new TextField();
        TextField categoriaField = new TextField();
        TextField precioField = new TextField();

        // ComboBox para seleccionar un proveedor
        ComboBox<String> proveedorComboBox = new ComboBox<>();
        Map<String, ObjectId> proveedorMap = new HashMap<>();

        // Cargar proveedores
        List<Proveedor> proveedores = cargarProveedores();
        for (Proveedor proveedor : proveedores) {
            proveedorComboBox.getItems().add(proveedor.getNombre());
            proveedorMap.put(proveedor.getNombre(), proveedor.getId());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Categoría:"), 0, 1);
        grid.add(categoriaField, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(precioField, 1, 2);
        grid.add(new Label("Proveedor:"), 0, 3);
        grid.add(proveedorComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir los datos del formulario en un producto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String nombre = nombreField.getText();
                    String categoria = categoriaField.getText();
                    double precio = Double.parseDouble(precioField.getText());
                    String proveedorNombre = proveedorComboBox.getValue();

                    if (nombre.isEmpty() || categoria.isEmpty() || proveedorNombre == null) {
                        mostrarAlerta("Error", "Todos los campos son obligatorios.");
                        return null;
                    }

                    ObjectId proveedorId = proveedorMap.get(proveedorNombre);
                    Producto nuevoProducto = new Producto(nombre, categoria, proveedorId, precio);
                    return nuevoProducto;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El precio debe ser un número válido.");
                    return null;
                }
            }
            return null;
        });

        Optional<Producto> result = dialog.showAndWait();

        result.ifPresent(this::agregarProductoABaseDeDatos);
    }


    private void agregarProductoABaseDeDatos(Producto producto) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Producto> collection = db.getCollection("productos", Producto.class);

        try {
            collection.insertOne(producto);
            productosList.add(producto); // Agregar a la lista observable
            productosTableView.refresh();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo agregar el producto.");
            e.printStackTrace();
        }
    }

    @FXML
    void onBuscarAction(ActionEvent event) {
        // Crear un cuadro de diálogo para ingresar el nombre
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Producto");
        dialog.setHeaderText("Ingrese el nombre del producto a buscar");
        dialog.setContentText("Nombre:");

        // Capturar la entrada del usuario
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(this::searchProducto); // Buscar si se ingresó un nombre
    }

    private void searchProducto(String nombre) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Producto> collection = db.getCollection("productos", Producto.class);

        try {
            // Buscar productos que coincidan exactamente con el nombre ingresado
            List<Producto> productoList = collection.find(Filters.eq("nombre", nombre))
                    .into(new ArrayList<>());

            // Actualizar la lista observable y refrescar la tabla
            productosList.clear();
            productosList.addAll(productoList);
            productosTableView.refresh();

            if (productosList.isEmpty()) {
                mostrarAlerta("Información", "No se encontraron productos con el nombre: " + nombre);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo realizar la búsqueda.");
            e.printStackTrace();
        }
    }


    @FXML
    void onModificarAction(ActionEvent event) {
        // Obtener el producto seleccionado
        Producto selectedProducto = productosTableView.getSelectionModel().getSelectedItem();

        if (selectedProducto == null) {
            mostrarAlerta("Error", "Debes seleccionar un producto para editar.");
            return;
        }

        // Crear un cuadro de diálogo personalizado
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto");
        dialog.setHeaderText("Modifica los datos del producto");

        // Botones OK y Cancelar
        ButtonType updateButtonType = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Campos de entrada
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nombreField = new TextField(selectedProducto.getNombre());
        TextField categoriaField = new TextField(selectedProducto.getCategoria());
        TextField precioField = new TextField(String.valueOf(selectedProducto.getPrecio()));

        // Selección de proveedor (ComboBox con nombres)
        ComboBox<String> proveedorComboBox = new ComboBox<>();
        Map<String, ObjectId> proveedorMap = new HashMap<>();

        // Cargar proveedores y mapearlos por nombre
        List<Proveedor> proveedores = cargarProveedores();
        for (Proveedor proveedor : proveedores) {
            proveedorComboBox.getItems().add(proveedor.getNombre());
            proveedorMap.put(proveedor.getNombre(), proveedor.getId());
        }

        // Establecer el proveedor actual en el ComboBox
        String proveedorActual = buscarNombreProveedor(selectedProducto.getProveedorId());
        proveedorComboBox.setValue(proveedorActual);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Categoría:"), 0, 1);
        grid.add(categoriaField, 1, 1);
        grid.add(new Label("Precio:"), 0, 2);
        grid.add(precioField, 1, 2);
        grid.add(new Label("Proveedor:"), 0, 3);
        grid.add(proveedorComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir los datos del formulario en un producto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    selectedProducto.setNombre(nombreField.getText());
                    selectedProducto.setCategoria(categoriaField.getText());
                    selectedProducto.setPrecio(Double.parseDouble(precioField.getText()));

                    String proveedorSeleccionado = proveedorComboBox.getValue();
                    selectedProducto.setProveedorId(proveedorMap.get(proveedorSeleccionado)); // Guardar ID

                    return selectedProducto;
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El precio debe ser un número válido.");
                    return null;
                }
            }
            return null;
        });

        Optional<Producto> result = dialog.showAndWait();

        result.ifPresent(this::updateProductoInDatabase);
    }


    private void updateProductoInDatabase(Producto producto) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Producto> collection = db.getCollection("productos", Producto.class);

        try {
            // Actualizar el documento en la base de datos
            collection.updateOne(Filters.eq("_id", producto.getId()),
                    Updates.combine(
                            Updates.set("nombre", producto.getNombre()),
                            Updates.set("categoria", producto.getCategoria()),
                            Updates.set("precio", producto.getPrecio()),
                            Updates.set("proveedor_id", producto.getProveedorId()) // Actualizar proveedor
                    ));

            // Refrescar la tabla
            productosTableView.refresh();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el producto.");
            e.printStackTrace();
        }
    }

    private List<Proveedor> cargarProveedores() {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);
        return collection.find().into(new ArrayList<>());
    }

    private String buscarNombreProveedor(ObjectId proveedorId) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);

        Proveedor proveedor = collection.find(Filters.eq("_id", proveedorId)).first();
        return (proveedor != null) ? proveedor.getNombre() : "Desconocido";
    }

    @FXML
    void onEliminarAction(ActionEvent event) {
        // Obtener el producto seleccionado
        Producto selectedUser = productosTableView.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            mostrarAlerta("Error", "Debes seleccionar un producto para eliminar.");
            return;
        }

        // Crear una alerta de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro de que quieres eliminar este producto?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteUserFromDatabase(selectedUser);
        }
    }

    public void deleteUserFromDatabase(Producto producto) {
        try {
            MongoDatabase db = DatabaseConector.getInstance().getDatabase();
            MongoCollection<Producto> collection = db.getCollection("productos", Producto.class);

            // Eliminar el producto usando su ObjectId
            collection.deleteOne(Filters.eq("_id", producto.getId()));

            // Eliminarlo también de la lista observable
            productosTableView.getItems().remove(producto);

            mostrarAlerta("Éxito", "El producto ha sido eliminado correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo eliminar el producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }


}
