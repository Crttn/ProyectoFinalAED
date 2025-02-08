package es.crttn.dad;

import es.crttn.dad.controllers.InventarioController;
import es.crttn.dad.controllers.ProductosController;
import es.crttn.dad.controllers.StockController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static RootController rc;
    private DatabaseConector dbc;

    @Override
    public void start(Stage primaryStage) throws Exception {

        rc = new RootController();
        dbc = new DatabaseConector();

        StockController stockController = new StockController();
        InventarioController inventarioController= new InventarioController(stockController);
        ProductosController productosController = new ProductosController(inventarioController);

        primaryStage.setTitle("Gestión de Inventario");
        primaryStage.setScene(new Scene(rc.getRoot(), 800, 600));
        primaryStage.show();
    }

    // Cerrar la conexión con la base de datos
    @Override
    public void stop() throws Exception {
        super.stop();
        dbc.closeConnection();
    }

    public static RootController getRc() {
        return rc;
    }
}
