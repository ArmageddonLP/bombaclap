package adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import proxy.Field;
import proxy.Player;
import proxy.enums.BlockType;
import proxy.enums.BombState;
import proxy.enums.PlayerColor;
import proxy.enums.PlayerDirection;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Adapter to parse Json. Somehow needed by Gson in the frontend.
 */
public class FieldTypeAdapter extends TypeAdapter<Field[]> {

    /**
     * Implementation to convert the Field[] to a json for Gson
     * Not implemented because it is not needed.
     *
     * @param out
     * @param value
     * @throws IOException
     */
    @Override
    public void write(JsonWriter out, Field[] value) throws IOException {
    }

    /**
     * Implementation to read the Json and parse it to a Field[]
     *
     * @param in
     * @return
     * @throws IOException
     */
    @Override
    public Field[] read(JsonReader in) throws IOException {
        if (!in.peek().equals(JsonToken.BEGIN_ARRAY)) {
            return new Field[0];
        }
        ArrayList<Field> map = new ArrayList<>();
        in.beginArray();
        while (in.hasNext() && !in.peek().equals(JsonToken.END_ARRAY)) {
            Field field = new Field();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "players" -> {
                        parsePlayers(in, field);
                    }
                    case "ground" -> {
                        switch (in.nextString()) {
                            case "DIRT" -> field.setGround(BlockType.DIRT);
                            case "BRITTLE_WALL" -> field.setGround(BlockType.BRITTLE_WALL);
                            case "SOLID_WALL" -> field.setGround(BlockType.SOLID_WALL);
                            case "EXPLODED_GRASS" -> field.setGround(BlockType.EXPLODED_GRASS);
                            case "EXPLODED_DIRT" -> field.setGround(BlockType.EXPLODED_DIRT);
                            case "EXPLODED_WALL" -> field.setGround(BlockType.EXPLODED_WALL);
                            default -> field.setGround(BlockType.GRASS);
                        }
                    }
                    case "bombState" -> {
                        switch (in.nextString()) {
                            case "FIRE_STATE" -> field.setBombState(BombState.FIRE_STATE);
                            case "RED_STATE" -> field.setBombState(BombState.RED_STATE);
                            case "BLACK_STATE" -> field.setBombState(BombState.BLACK_STATE);
                            case "EXPLODING_STATE" -> field.setBombState(BombState.EXPLODING_STATE);
                            default -> field.setBombState(BombState.NO_BOMB);
                        }
                    }
                    case "x" -> field.setX(in.nextInt());
                    case "y" -> field.setY(in.nextInt());
                    default -> in.skipValue();
                }
            }
            in.endObject();
            map.add(field);
        }
        in.endArray();
        return map.toArray(new Field[0]);
    }

    /**
     * Parses the Player[] in the Json
     *
     * @param in
     * @param field
     * @throws IOException
     */
    private void parsePlayers(JsonReader in, Field field) throws IOException {
        ArrayList<Player> players = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
            if (in.peek().equals(JsonToken.NULL)) {
                players.add(null);
                in.skipValue();
                continue;
            }
            in.beginObject();
            Player player = new Player();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "name" -> player.setName(in.nextString());
                    case "playerDirection" -> {
                        switch (in.nextString()) {
                            case "UP" -> player.setDirection(PlayerDirection.UP);
                            case "DOWN" -> player.setDirection(PlayerDirection.DOWN);
                            case "LEFT" -> player.setDirection(PlayerDirection.LEFT);
                            case "RIGHT" -> player.setDirection(PlayerDirection.RIGHT);
                            default -> player.setDirection(PlayerDirection.NO_DIRECTION);
                        }
                    }
                    case "color" -> {
                        switch (in.nextString()) {
                            case "BLUE" -> player.setColor(PlayerColor.BLUE);
                            case "RED" -> player.setColor(PlayerColor.RED);
                            case "GREEN" -> player.setColor(PlayerColor.GREEN);
                            default -> player.setColor(PlayerColor.YELLOW);
                        }
                    }
                    default -> in.skipValue();
                }
            }
            players.add(player);
            in.endObject();
        }
        in.endArray();
        field.setPlayers(players.toArray(new Player[0]));
    }
}
