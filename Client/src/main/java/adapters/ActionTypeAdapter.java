package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import proxy.Action;
import proxy.enums.PlayerDirection;

import java.io.IOException;

/**
 * Adapter to parse Json. Somehow needed by Gson in the frontend.
 */
public class ActionTypeAdapter extends TypeAdapter<Action> {

    /**
     * Implementation to parse Action to JSON
     *
     * @param out
     * @param action
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, Action action) throws IOException {
        out.beginObject();
        out.name("playerId");
        out.value(action.getPlayerId());
        out.name("playerDirection");
        switch (action.getPlayerDirection()) {
            case UP -> out.value(PlayerDirection.UP.name());
            case DOWN -> out.value(PlayerDirection.DOWN.name());
            case RIGHT -> out.value(PlayerDirection.RIGHT.name());
            case LEFT -> out.value(PlayerDirection.LEFT.name());
            default -> out.value(PlayerDirection.NO_DIRECTION.name());
        }
        out.name("bombPlanted");
        out.value(action.getBombPlanted());
        out.endObject();
    }

    /**
     * * Implementation to convert the Json to Action
     * * Not implemented because it is not needed.
     *
     * @param in
     * @return
     * @throws IOException
     */
    @Override
    public Action read(JsonReader in) throws IOException {
        return null;
    }
}
