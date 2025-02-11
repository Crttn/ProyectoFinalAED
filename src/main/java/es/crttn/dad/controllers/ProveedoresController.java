package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Producto;
import es.crttn.dad.modelos.Proveedor;
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
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProveedoresController implements Initializable {
    private ProductosController productosController;
    private StockController stockController;

    // Obtener la base de datos
    MongoDatabase db = DatabaseConector.getInstance().getDatabase();

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

        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);
        List<Proveedor> proveedores = collection.find().into(new ArrayList<>());

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

    //establecer productoscontroller
    public void setProductosController(ProductosController productosController) {
        this.productosController = productosController;
    }

    //establecer stock
    public void setStockController(StockController stockController) {
        this.stockController = stockController;
    }

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
        // Obtener el proveedor seleccionado de la tabla.
        Proveedor selectedProveedor = proveedoresTableView.getSelectionModel().getSelectedItem();

        if (selectedProveedor == null) {
            mostrarAlerta("Error", "Debes seleccionar un proveedor para eliminar.");
            return;
        }

        // Crear una alerta de confirmación.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Si elimina este proveedor tambien desaparecerán los datos asociados a este.");
        alert.setContentText("¿Está seguro de continuar?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteProveedorFromDatabase(selectedProveedor);

        }
    }

    private void deleteProveedorFromDatabase(Proveedor proveedor) {
        try {
            MongoCollection<Proveedor> proveedoresCollection = db.getCollection("proveedores", Proveedor.class);
            MongoCollection<Producto> productosCollection = db.getCollection("productos", Producto.class);
            MongoCollection<Stock> stockCollection = db.getCollection("stock", Stock.class); // Asegúrate de tener importado el modelo Stock

            // Primero, obtener la lista de productos asociados al proveedor
            List<Producto> productosAsociados = productosCollection.find(Filters.eq("proveedor_id", proveedor.getId()))
                    .into(new ArrayList<>());
            List<ObjectId> productosIds = new ArrayList<>();
            for (Producto p : productosAsociados) {
                productosIds.add(p.getId());
            }

            // Eliminar el proveedor
            proveedoresCollection.deleteOne(Filters.eq("_id", proveedor.getId()));

            // Eliminar los productos asociados al proveedor
            productosCollection.deleteMany(Filters.eq("proveedor_id", proveedor.getId()));

            // Si existen productos asociados, eliminar el stock correspondiente
            if (!productosIds.isEmpty()) {
                stockCollection.deleteMany(Filters.in("producto_id", productosIds));
            }

            // Remover el proveedor de la lista observable en la vista
            proveedoresTableView.getItems().remove(proveedor);

            productosController.refreshProducts();
            stockController.refreshStock();




            // Mostrar alerta de éxito.
            mostrarAlerta("Éxito", "El proveedor, sus productos y el stock asociado han sido eliminados correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo eliminar el proveedor: " + e.getMessage());
        }
    }

    @FXML
    void onModificarAction(ActionEvent event) {
        // Obtener el proveedor seleccionado de la tabla.
        Proveedor selectedProveedor = proveedoresTableView.getSelectionModel().getSelectedItem();

        if (selectedProveedor == null) {
            mostrarAlerta("Error", "Debes seleccionar un proveedor para editar.");
            return;
        }

        // Crear un cuadro de diálogo para editar el proveedor.
        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle("Editar Proveedor");
        dialog.setHeaderText("Modifica los datos del proveedor");

        // Botón de actualizar.
        ButtonType updateButtonType = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Crear el formulario de edición.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nombreField = new TextField(selectedProveedor.getNombre());
        TextField direccionField = new TextField(selectedProveedor.getDireccion());
        TextField contactoField = new TextField(selectedProveedor.getContacto());

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Contacto:"), 0, 2);
        grid.add(contactoField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convertir los datos del formulario en el proveedor modificado.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                selectedProveedor.setNombre(nombreField.getText());
                selectedProveedor.setDireccion(direccionField.getText());
                selectedProveedor.setContacto(contactoField.getText());
                return selectedProveedor;
            }
            return null;
        });

        // Mostrar el diálogo y, si se confirma, actualizar en la base de datos.
        Optional<Proveedor> result = dialog.showAndWait();
        result.ifPresent(this::updateProveedorInDatabase);
    }


    private void updateProveedorInDatabase(Proveedor proveedor) {
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);

        try {
            // Actualizar el documento usando el ObjectId.
            collection.updateOne(Filters.eq("_id", proveedor.getId()),
                    Updates.combine(
                            Updates.set("nombre", proveedor.getNombre()),
                            Updates.set("direccion", proveedor.getDireccion()),
                            Updates.set("contacto", proveedor.getContacto())
                    ));
            proveedoresTableView.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo actualizar el proveedor.");
        }
    }



    @FXML
    void onBuscarAction(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Producto");
        dialog.setHeaderText("Ingrese el nombre del proveedor");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::searchProveedor); // Buscar si se ingresó un nombre
    }

    private void searchProveedor(String nombre) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Proveedor> collection = db.getCollection("proveedores", Proveedor.class);

        try {
            if (nombre.isEmpty()) {
                // Si el nombre está vacío, restauramos todos los proveedores.
                List<Proveedor> proveedores = collection.find().into(new ArrayList<>());
                proveedoresList.setAll(proveedores);
                proveedoresTableView.refresh();
                return;
            }

            // Buscar proveedores cuyo nombre contenga el término ingresado.
            List<Proveedor> proveedorList = collection.find(Filters.regex("nombre", ".*" + nombre + ".*", "i"))
                    .into(new ArrayList<>());

            // Actualizar la lista observable y refrescar la tabla
            proveedoresList.clear();
            proveedoresList.addAll(proveedorList);
            proveedoresTableView.refresh();

            if (proveedoresList.isEmpty()) {
                mostrarAlerta("Información", "No se encontraron proveedores con el nombre que contiene: " + nombre);
            }

        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo realizar la búsqueda.");
            e.printStackTrace();
        }
    }

    @FXML
    void onMostrarTodosAction(ActionEvent event) {
        showData();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
