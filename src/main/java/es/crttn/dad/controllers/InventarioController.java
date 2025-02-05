package es.crttn.dad.controllers;

import es.crttn.dad.App;
import es.crttn.dad.modelos.Movimiento;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import java.text.SimpleDateFormat;


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

        //Combobox
        tipocomboBox.getItems().addAll("Venta", "Compra", "Ajuste");

        // Configurar las columnas de la tabla
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductoId().toString()));
        tipoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        cantidadColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCantidad()));
        detallesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDetalles()));

        // Configurar el formato de fecha deseado
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        fechaColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(dateFormat.format(cellData.getValue().getFecha()))
        );

    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    void onAddAction(ActionEvent event) {

        String idProducto = idProductoTextfield.getText();
        String tipo = tipocomboBox.getValue();
        int cantidad = Integer.parseInt(cantidadTetxfield.getText());
        Date fecha = java.sql.Date.valueOf(fechaTextfield.getValue());
        String detalles = detallesTextfield.getText();

        Movimiento nuevoMovimiento = new Movimiento(new ObjectId(), new ObjectId(), tipo, cantidad, null, null, fecha, detalles);

        gestionmovimientosTable.getItems().add(nuevoMovimiento);

        idProductoTextfield.clear();
        tipocomboBox.setValue(null);
        cantidadTetxfield.clear();
        fechaTextfield.setValue(null);
        detallesTextfield.clear();
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
        gestionmovimientosTable.getItems().remove(selectedMovimiento);
    }

    @FXML
    void onFindAction(ActionEvent event) {
        // Crear un cuadro de diálogo de entrada
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar por Fecha");
        dialog.setHeaderText("Buscar movimientos por fecha");
        dialog.setContentText("Por favor, ingrese la fecha (yyyy-mm-dd):");

        // Obtener el resultado del diálogo
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(fechaStr -> {
            try {
                // Validar y parsear la fecha ingresada
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                Date fecha = dateFormat.parse(fechaStr);

                // Filtrar los movimientos por la fecha ingresada
                ObservableList<Movimiento> filteredMovimientos = gestionmovimientosTable.getItems().filtered(movimiento -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(movimiento.getFecha()).equals(fechaStr);
                });

                // Actualizar la tabla con los movimientos filtrados
                gestionmovimientosTable.setItems(filteredMovimientos);
            } catch (Exception e) {
                mostrarAlerta("Error", "Formato de fecha inválido. Use yyyy-mm-dd.");
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void onUpdateAction(ActionEvent event) {
        Movimiento selectedMovimiento = gestionmovimientosTable.getSelectionModel().getSelectedItem();

        if (selectedMovimiento == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No hay selección");
            alert.setHeaderText("No has seleccinado ningún movimiento");
            alert.setContentText("Por favor, selecciona un movimiento de la tabla.");
            alert.showAndWait();
            return;
        }

        // Create a custom dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modificar Movimiento");
        alert.setHeaderText("Modifica los datos del movimiento");

        // Create a GridPane for input fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Input fields
        TextField idProductoField = new TextField(selectedMovimiento.getProductoId().toString());
        ComboBox<String> tipoComboBox = new ComboBox<>();
        tipoComboBox.getItems().addAll("Venta", "Compra", "Ajuste");
        tipoComboBox.setValue(selectedMovimiento.getTipo());
        TextField cantidadField = new TextField(String.valueOf(selectedMovimiento.getCantidad()));
        DatePicker fechaPicker = new DatePicker(new java.sql.Date(selectedMovimiento.getFecha().getTime()).toLocalDate());
        TextField detallesField = new TextField(selectedMovimiento.getDetalles());

        // Add fields to the GridPane
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

        // Show the dialog and wait for user response
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Update the selected movimiento data
                selectedMovimiento.setProductoId(new ObjectId(idProductoField.getText()));
                selectedMovimiento.setTipo(tipoComboBox.getValue());
                selectedMovimiento.setCantidad(Integer.parseInt(cantidadField.getText()));
                selectedMovimiento.setFecha(new Date(java.sql.Date.valueOf(fechaPicker.getValue()).getTime()));
                selectedMovimiento.setDetalles(detallesField.getText());

                // Refresh the table to show the changes
                gestionmovimientosTable.refresh();
            }
        });
    }

    @FXML
    void onBackAction(ActionEvent event) {
        App.getRc().getRoot().setCenter(App.getRc().getMc().getRoot());
    }

}
