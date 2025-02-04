package es.crttn.dad.modelos;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class Stock {
    @BsonId
    private ObjectId id;

    @BsonProperty("producto_id")
    private ObjectId productoId; // Referencia al producto

    @BsonProperty("cantidad_disponible")
    private int cantidadDisponible;

    // Constructor vacío para MongoDB
    public Stock() {}

    public Stock(ObjectId productoId, int cantidadDisponible) {
        this.productoId = productoId;
        this.cantidadDisponible = cantidadDisponible;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getProductoId() { return productoId; }
    public void setProductoId(ObjectId productoId) { this.productoId = productoId; }

    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }
}
