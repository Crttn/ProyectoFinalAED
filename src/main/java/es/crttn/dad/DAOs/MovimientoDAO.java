package es.crttn.dad.DAOs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import es.crttn.dad.modelos.Movimiento;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MovimientoDAO {
    private MongoCollection<Movimiento> collection;

    public MovimientoDAO(MongoDatabase database) {
        this.collection = database.getCollection("movimientos", Movimiento.class);
    }

    // Insertar un nuevo movimiento
    public void insertarMovimiento(Movimiento movimiento) {
        collection.insertOne(movimiento);
        System.out.println("Movimiento insertado correctamente.");
    }

    // Obtener todos los movimientos
    public List<Movimiento> obtenerTodosMovimientos() {
        return collection.find().into(new ArrayList<>());
    }

    // Buscar un movimiento por su ID
    public Movimiento obtenerMovimientoPorId(ObjectId id) {
        return collection.find(eq("_id", id)).first();
    }

    // Actualizar un movimiento existente
    public void actualizarMovimiento(ObjectId id, Movimiento movimientoActualizado) {
        collection.replaceOne(eq("_id", id), movimientoActualizado);
        System.out.println("Movimiento actualizado correctamente.");
    }

    // Eliminar un movimiento
    public void eliminarMovimiento(ObjectId id) {
        collection.deleteOne(eq("_id", id));
        System.out.println("Movimiento eliminado correctamente.");
    }

}
