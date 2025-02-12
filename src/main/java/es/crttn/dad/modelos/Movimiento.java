package es.crttn.dad.modelos;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Movimiento {
    @BsonId
    private ObjectId id;

    private Producto producto;

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

    public Movimiento(ObjectId id, Producto producto, String tipo, int cantidad, ObjectId proveedorId, ObjectId compradorId, Date fecha, String detalles) {
        this.id = id;
        this.producto = producto;
        this.productoId = producto.getId();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.proveedorId = proveedorId;
        this.compradorId = compradorId;
        this.fecha = fecha;
        this.detalles = detalles;
    }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) {
        this.producto = producto;
        this.productoId = producto.getId();
    }

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
