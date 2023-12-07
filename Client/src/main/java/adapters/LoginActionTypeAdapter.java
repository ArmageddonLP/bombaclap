package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import proxy.LoginAction;

import java.io.IOException;

/**
 * Adapter to parse Json. Somehow needed by Gson in the frontend.
 */
public class LoginActionTypeAdapter extends TypeAdapter<LoginAction> {

    /**
     * Implementation to parse LoginAction to JSON
     *
     * @param out
     * @param loginAction
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, LoginAction loginAction) throws IOException {
        out.beginObject();
        out.name("playerName");
        out.value(loginAction.getPlayerName());
        out.name("playerId");
        out.value(loginAction.getPlayerId());
        out.endObject();
    }

    /**
     * Implementation to read the Json and parse it to LoginAction
     *
     * @param in
     * @return
     * @throws IOException
     */
    @Override
    public LoginAction read(JsonReader in) throws IOException {
        LoginAction la = new LoginAction();
        in.beginObject();
        while (in.hasNext() && !in.peek().equals(JsonToken.END_OBJECT)) {
            switch (in.nextName()) {
                case "playerName" -> la.setPlayerName(in.nextString());
                case "playerId" -> la.setPlayerId(in.nextInt());
                default -> in.skipValue();
            }
        }
        in.endObject();
        return la;
    }
}
