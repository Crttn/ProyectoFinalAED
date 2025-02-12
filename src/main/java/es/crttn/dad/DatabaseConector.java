package es.crttn.dad;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

public class DatabaseConector {

    private static DatabaseConector instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    DatabaseConector() {
        // Permite manejar automaticamente Objetos como documentos BSON
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        // Configura MongoDB con el codec para soportar Objetos
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .build();

        this.mongoClient = MongoClients.create("mongodb://localhost:27017"); // Conexión a MongoDB
        this.database = mongoClient.getDatabase("inventario").withCodecRegistry(codecRegistry); // Aplicar codec
    }

    // Asegura que solo exista una única instancia de DatabaseConector
    public static DatabaseConector getInstance() {
        if (instance == null) {
            instance = new DatabaseConector();
        }
        return instance;
    }

    // Permite obtener la referencia a la base de datos
    public MongoDatabase getDatabase() {
        return database;
    }

    public void closeConnection() {
        mongoClient.close();
    }
}
