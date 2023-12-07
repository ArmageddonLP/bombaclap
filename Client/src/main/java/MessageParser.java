import adapters.FieldTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import proxy.Field;

/**
 * Handles parsing of messages from backend.
 */
public class MessageParser {

    /**
     * Parses the map into a internal object to allow operations with the objects
     *
     * @param message Server message which contains the map
     * @return The parsed map as a Field[]
     */
    public static Field[] parseMessage(String message) {
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Field[].class, new FieldTypeAdapter());
            builder.setPrettyPrinting();
            Gson gson = builder.create();
            return gson.fromJson(message, Field[].class);
        } catch (Exception e) {
//            System.out.println("Couldn't parse message: " + e.getMessage());
            return null;
        }
    }

    public static Long parseTimerMessage(String message) {
        try {
            Long timer = Long.parseLong(message);
            return timer;
        } catch (Exception e) {
//            System.out.println("Couldn't parse message: " + e.getMessage());
            return null;
        }
    }
}
