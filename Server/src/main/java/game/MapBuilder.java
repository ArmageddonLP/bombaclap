package game;

import config.Constants;
import helpers.MapHelper;
import proxy.Field;
import proxy.Player;
import proxy.enums.BlockType;
import proxy.enums.BombState;
import proxy.enums.PlayerDirection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * MapBuilder-Builder used to build a map for gameState
 */
public class MapBuilder {
    private final Field[] map;

    /**
     * Creates a MapBuilder with a blank map of length equal to MAP_SIZE
     */
    public MapBuilder() {
        map = new Field[Constants.MAP_SIZE];
    }

    /**
     * Fills the map with fields.
     * Fields are created with default values for ground and bombState,
     * and they are passed their coordinates in the map.
     *
     * @return MapBuilder this
     */
    public MapBuilder generateMap() {
        for (int i = 0; i < map.length; i++) {
            Field field = new Field();
            field.setGround(BlockType.GRASS);
            field.setBombState(BombState.NO_BOMB);
            field.setX(i % Constants.MAP_WIDTH);
            field.setY(i / Constants.MAP_WIDTH);
            map[i] = field;
        }
        return this;
    }

    /**
     * Fills the startingPositions in the map with players.
     * Players are created with default values for playerId, color and direction.
     *
     * @param playerNames Array of Strings representing playerNames
     * @return MapBuilder this
     */
    public MapBuilder generatePlayers(String[] playerNames) {
        map[Constants.STARTPOSITION_PLAYER_ONE].getPlayers()[Constants.PLAYER_ID_PLAYER_ONE] =
                new Player(playerNames[Constants.PLAYER_ID_PLAYER_ONE], Constants.COLOR_PLAYER_ONE, PlayerDirection.NO_DIRECTION);
        map[Constants.STARTPOSITION_PLAYER_TWO].getPlayers()[Constants.PLAYER_ID_PLAYER_TWO] =
                new Player(playerNames[Constants.PLAYER_ID_PLAYER_TWO], Constants.COLOR_PLAYER_TWO, PlayerDirection.NO_DIRECTION);
        map[Constants.STARTPOSITION_PLAYER_THREE].getPlayers()[Constants.PLAYER_ID_PLAYER_THREE] =
                new Player(playerNames[Constants.PLAYER_ID_PLAYER_THREE], Constants.COLOR_PLAYER_THREE, PlayerDirection.NO_DIRECTION);
        map[Constants.STARTPOSITION_PLAYER_FOUR].getPlayers()[Constants.PLAYER_ID_PLAYER_FOUR] =
                new Player(playerNames[Constants.PLAYER_ID_PLAYER_FOUR], Constants.COLOR_PLAYER_FOUR, PlayerDirection.NO_DIRECTION);
        return this;
    }

    /**
     * Fills the map with blocks according to data read from the csv-file in the given fileLocation.
     *
     * @param fileLocation String location of a csv-file of a maze
     * @return MapBuilder this
     */
    public MapBuilder generateMaze(String fileLocation) {
        ArrayList<Integer> blockCodes = MapHelper.readMap(fileLocation);
        for (int i = 0; i < map.length; i++) {
            switch (blockCodes.get(i)) {
                case 0 -> map[i].setGround(BlockType.SOLID_WALL);
                case 1 -> map[i].setGround(BlockType.BRITTLE_WALL);
                case 2 -> map[i].setGround(BlockType.GRASS);
            }
        }
        return this;
    }

    /**
     * Fills the map with blocks according to data read from a randomly selected csv-file in the given folderLocation.
     *
     * @param folderLocation String location of csv-files of mazes
     * @return MapBuilder this
     */
    public MapBuilder generateRandomMaze(String folderLocation) {
        Random random = new Random();
        File dir = new File(folderLocation);
        File[] files = dir.listFiles();
        if (files == null) {
            return this;
        }
        File file = files[random.nextInt(files.length)];
        if (file == null) {
            return this;
        }
        ArrayList<Integer> blockCodes = MapHelper.readMap(file.getAbsolutePath());
        for (int i = 0; i < map.length; i++) {
            switch (blockCodes.get(i)) {
                case 0 -> map[i].setGround(BlockType.SOLID_WALL);
                case 1 -> map[i].setGround(BlockType.BRITTLE_WALL);
                case 2 -> map[i].setGround(BlockType.GRASS);
            }
        }
        return this;
    }

    /**
     * Returns the map currently stored in this mapBuilder and resets the stored map
     *
     * @return Field[] as map
     */
    public Field[] retrieveMap() {
        Field[] currentMap = Arrays.stream(map).toArray(Field[]::new);
        Arrays.fill(map, null);
        return currentMap;
    }
}
