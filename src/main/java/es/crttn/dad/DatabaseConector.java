package es.crttn.dad;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConector {
    private static DatabaseConector instance;  // Singleton
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Constructor privado para evitar instancias externas
    DatabaseConector() {
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("inventario");
            System.out.println("✅ Conectado a MongoDB correctamente.");
        } catch (Exception e) {
            System.err.println("❌ Error al conectar a MongoDB: " + e.getMessage());
        }
    }

    // Método para obtener la instancia única de la conexión
    public static synchronized DatabaseConector getInstance() {
        if (instance == null) {
            instance = new DatabaseConector();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    // Método para cerrar la conexión cuando se deja de usar
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 Conexión a MongoDB cerrada.");
        }
    }
}
