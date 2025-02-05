package es.crttn.dad.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import es.crttn.dad.App;
import es.crttn.dad.DatabaseConector;
import es.crttn.dad.modelos.Movimiento;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private TableColumn<Movimiento, Integer> cantidadColumn;

    @FXML
    private TextField cantidadTetxfield;

    @FXML
    private TextField detallesTextfield;

    @FXML
    private Button eliminarButton;

    @FXML
    private TableColumn<Movimiento, String> fechaColumn;

    @FXML
    private DatePicker fechaTextfield;

    @FXML
    private TableView<Movimiento> gestionmovimientosTable;

    @FXML
    private TableColumn<Movimiento, String> idColumn;

    @FXML
    private TextField idProductoTextfield;

    @FXML
    private TableColumn<Movimiento, String> tipoColumn;

    @FXML
    private TableColumn<Movimiento, String> detallesColumn;

    @FXML
    private ComboBox<String> tipocomboBox;

    private ObservableList<Movimiento> movimientosList;

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
        tipocomboBox.getItems().addAll("Venta", "Compra", "Ajuste");

        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductoId().toString()));
        tipoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        cantidadColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCantidad()));
        detallesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDetalles()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fechaColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(dateFormat.format(cellData.getValue().getFecha())));

        movimientosList = FXCollections.observableArrayList();
        gestionmovimientosTable.setItems(movimientosList);

        showData();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void showData() {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Movimiento> collection = db.getCollection("movimientos", Movimiento.class);

        List<Movimiento> movimientos = collection.find().into(new ArrayList<>());

        Platform.runLater(() -> {
            movimientosList.setAll(movimientos);
        });
    }

    @FXML
    void onAddAction(ActionEvent event) {
        String idProducto = idProductoTextfield.getText();
        String tipo = tipocomboBox.getValue();
        int cantidad = Integer.parseInt(cantidadTetxfield.getText());
        Date fecha = java.sql.Date.valueOf(fechaTextfield.getValue());
        String detalles = detallesTextfield.getText();

        Movimiento nuevoMovimiento = new Movimiento(new ObjectId(), new ObjectId(idProducto), tipo, cantidad, null, null, fecha, detalles);

        agregarMovimientoABaseDeDatos(nuevoMovimiento);

        idProductoTextfield.clear();
        tipocomboBox.setValue(null);
        cantidadTetxfield.clear();
        fechaTextfield.setValue(null);
        detallesTextfield.clear();
    }

    private void agregarMovimientoABaseDeDatos(Movimiento movimiento) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Movimiento> collection = db.getCollection("movimientos", Movimiento.class);

        try {
            collection.insertOne(movimiento);
            movimientosList.add(movimiento);
            gestionmovimientosTable.refresh();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo agregar el movimiento.");
            e.printStackTrace();
        }
    }

    @FXML
    void onDeleteAction(ActionEvent event) {
        Movimiento selectedMovimiento = gestionmovimientosTable.getSelectionModel().getSelectedItem();

        if (selectedMovimiento == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No hay selección");
            alert.setHeaderText("No hay ningún movimiento seleccionado");
            alert.setContentText("Por favor, selecciona un movimiento de la tabla.");
            alert.showAndWait();
            return;
        }

        eliminarMovimientoDeBaseDeDatos(selectedMovimiento);
    }

    private void eliminarMovimientoDeBaseDeDatos(Movimiento movimiento) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Movimiento> collection = db.getCollection("movimientos", Movimiento.class);

        try {
            collection.deleteOne(Filters.eq("_id", movimiento.getId()));
            movimientosList.remove(movimiento);
            gestionmovimientosTable.refresh();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo eliminar el movimiento.");
            e.printStackTrace();
        }
    }

    @FXML
    void onFindAction(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar por Fecha");
        dialog.setHeaderText("Buscar movimientos por fecha");
        dialog.setContentText("Por favor, ingrese la fecha (yyyy-mm-dd):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fechaStr -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                Date fecha = dateFormat.parse(fechaStr);

                buscarMovimientosPorFecha(fecha);
            } catch (Exception e) {
                mostrarAlerta("Error", "Formato de fecha inválido. Use yyyy-mm-dd.");
            }
        });
    }

    private void buscarMovimientosPorFecha(Date fecha) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Movimiento> collection = db.getCollection("movimientos", Movimiento.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Movimiento> movimientos = collection.find(Filters.eq("fecha", sdf.format(fecha))).into(new ArrayList<>());

        Platform.runLater(() -> {
            movimientosList.setAll(movimientos);
            gestionmovimientosTable.refresh();
        });
    }

    @FXML
    void onUpdateAction(ActionEvent event) {
        Movimiento selectedMovimiento = gestionmovimientosTable.getSelectionModel().getSelectedItem();

        if (selectedMovimiento == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No hay selección");
            alert.setHeaderText("No has seleccionado ningún movimiento");
            alert.setContentText("Por favor, selecciona un movimiento de la tabla.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modificar Movimiento");
        alert.setHeaderText("Modifica los datos del movimiento");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField idProductoField = new TextField(selectedMovimiento.getProductoId().toString());
        ComboBox<String> tipoComboBox = new ComboBox<>();
        tipoComboBox.getItems().addAll("Venta", "Compra", "Ajuste");
        tipoComboBox.setValue(selectedMovimiento.getTipo());
        TextField cantidadField = new TextField(String.valueOf(selectedMovimiento.getCantidad()));
        DatePicker fechaPicker = new DatePicker(new java.sql.Date(selectedMovimiento.getFecha().getTime()).toLocalDate());
        TextField detallesField = new TextField(selectedMovimiento.getDetalles());

        grid.add(new Label("ID Producto:"), 0, 0);
        grid.add(idProductoField, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(tipoComboBox, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(cantidadField, 1, 2);
        grid.add(new Label("Fecha:"), 0, 3);
        grid.add(fechaPicker, 1, 3);
        grid.add(new Label("Detalles:"), 0, 4);
        grid.add(detallesField, 1, 4);

        alert.getDialogPane().setContent(grid);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                selectedMovimiento.setProductoId(new ObjectId(idProductoField.getText()));
                selectedMovimiento.setTipo(tipoComboBox.getValue());
                selectedMovimiento.setCantidad(Integer.parseInt(cantidadField.getText()));
                selectedMovimiento.setFecha(new Date(java.sql.Date.valueOf(fechaPicker.getValue()).getTime()));
                selectedMovimiento.setDetalles(detallesField.getText());

                actualizarMovimientoEnBaseDeDatos(selectedMovimiento);
            }
        });
    }

    private void actualizarMovimientoEnBaseDeDatos(Movimiento movimiento) {
        MongoDatabase db = DatabaseConector.getInstance().getDatabase();
        MongoCollection<Movimiento> collection = db.getCollection("movimientos", Movimiento.class);

        try {
            collection.updateOne(Filters.eq("_id", movimiento.getId()),
                    Updates.combine(
                            Updates.set("producto_id", movimiento.getProductoId()),
                            Updates.set("tipo", movimiento.getTipo()),
                            Updates.set("cantidad", movimiento.getCantidad()),
                            Updates.set("fecha", movimiento.getFecha()),
                            Updates.set("detalles", movimiento.getDetalles())
                    ));
            gestionmovimientosTable.refresh();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo actualizar el movimiento.");
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