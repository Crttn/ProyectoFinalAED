package es.crttn.dad.modelos;

import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;

public class Proveedor {
    @BsonId
    private ObjectId id;

    private String nombre;
    private String direccion;

    @BsonProperty("contacto")
    private String contacto;  // Ahora solo es un número de teléfono (String)

    @BsonProperty("productos_suministrados")
    private List<ObjectId> productosSuministrados;

    // Constructor vacío para MongoDB
    public Proveedor() {}

    public Proveedor(String nombre, String direccion, String contacto, List<ObjectId> productosSuministrados) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.contacto = contacto;
        this.productosSuministrados = productosSuministrados;
    }

    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public List<ObjectId> getProductosSuministrados() { return productosSuministrados; }
    public void setProductosSuministrados(List<ObjectId> productosSuministrados) { this.productosSuministrados = productosSuministrados; }
}
