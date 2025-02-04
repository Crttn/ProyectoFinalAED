package es.crttn.dad.modelos;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Movimiento {
    @BsonId
    private ObjectId id;

    @BsonProperty("producto_id")
    private ObjectId productoId;

    private String tipo;  // "venta", "compra", "ajuste", etc.

    private int cantidad;

    @BsonProperty("proveedor_id")
    private ObjectId proveedorId; // Se usa solo si el movimiento es "compra"

    @BsonProperty("comprador_id")
    private ObjectId compradorId; // Se usa solo si el movimiento es "venta"

    private Date fecha;
    private String detalles;

    public Movimiento() {}

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getProductoId() { return productoId; }
    public void setProductoId(ObjectId productoId) { this.productoId = productoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public ObjectId getProveedorId() { return proveedorId; }
    public void setProveedorId(ObjectId proveedorId) { this.proveedorId = proveedorId; }

    public ObjectId getCompradorId() { return compradorId; }
    public void setCompradorId(ObjectId compradorId) { this.compradorId = compradorId; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
}
