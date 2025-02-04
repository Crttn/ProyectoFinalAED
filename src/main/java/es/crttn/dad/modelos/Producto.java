package es.crttn.dad.modelos;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Date;

public class Producto {
    @BsonId
    private ObjectId id;

    private String nombre;
    private String categoria;
    private String descripcion;
    private double precio;

    @BsonProperty("stock_actual")
    private int stockActual;

    @BsonProperty("proveedor_id")
    private ObjectId proveedorId;

    @BsonProperty("fecha_creacion")
    private Date fechaCreacion;

    // Constructor vacío para MongoDB
    public Producto() {}

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public ObjectId getProveedorId() { return proveedorId; }
    public void setProveedorId(ObjectId proveedorId) { this.proveedorId = proveedorId; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

