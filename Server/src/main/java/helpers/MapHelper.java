package helpers;

import config.Constants;
import proxy.Field;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * MapHelper provides static methods used for changes to map outside of gameLogic
 */
public class MapHelper {

    /**
     * Reads a csv-file from the given fileLocation and transforms it into a list Integers
     *
     * @param fileLocation path to the csv-file as String
     * @return ArrayList of Integers representing a BlockType
     */
    public static ArrayList<Integer> readMap(String fileLocation) {
        ArrayList<Integer> blockCodes = new ArrayList<>();
        ArrayList<String[]> lines = new ArrayList<>();
        if (new File(fileLocation).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line.split(Constants.CSV_SEPARATOR));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String[] line : lines) {
                for (String code : line) {
                    try {
                        blockCodes.add(Integer.parseInt(code));
                    } catch (NumberFormatException e) {
                        blockCodes.add(Constants.DEFAULT_BLOCK_TYPE_CODE);
                    }

                }
            }
        }
        return blockCodes;
    }

    /**
     * Takes an amount of playerNames equal to Constants.MAX_PLAYER_AMOUNT
     * from the given playerNames-Array and adds those playerNames to the startPositions
     * of the players
     *
     * @param map         Field-Array of the game representing the map
     * @param playerNames String-Array of names for each player
     */
    public static void renamePlayers(Field[] map, String[] playerNames) {
        if (playerNames.length == Constants.MAX_PLAYER_COUNT) {
            if (playerNames[Constants.PLAYER_ID_PLAYER_ONE] != null &&
                    map[Constants.STARTPOSITION_PLAYER_ONE].getPlayers()[Constants.PLAYER_ID_PLAYER_ONE] != null) {
                map[Constants.STARTPOSITION_PLAYER_ONE]
                        .getPlayers()[Constants.PLAYER_ID_PLAYER_ONE]
                        .setName(playerNames[Constants.PLAYER_ID_PLAYER_ONE]);
            }
            if (playerNames[Constants.PLAYER_ID_PLAYER_TWO] != null &&
                    map[Constants.STARTPOSITION_PLAYER_TWO].getPlayers()[Constants.PLAYER_ID_PLAYER_TWO] != null) {
                map[Constants.STARTPOSITION_PLAYER_TWO]
                        .getPlayers()[Constants.PLAYER_ID_PLAYER_TWO]
                        .setName(playerNames[Constants.PLAYER_ID_PLAYER_TWO]);
            }
            if (playerNames[Constants.PLAYER_ID_PLAYER_THREE] != null &&
                    map[Constants.STARTPOSITION_PLAYER_THREE].getPlayers()[Constants.PLAYER_ID_PLAYER_THREE] != null) {
                map[Constants.STARTPOSITION_PLAYER_THREE]
                        .getPlayers()[Constants.PLAYER_ID_PLAYER_THREE]
                        .setName(playerNames[Constants.PLAYER_ID_PLAYER_THREE]);
            }
            if (playerNames[Constants.PLAYER_ID_PLAYER_FOUR] != null &&
                    map[Constants.STARTPOSITION_PLAYER_FOUR].getPlayers()[Constants.PLAYER_ID_PLAYER_FOUR] != null) {
                map[Constants.STARTPOSITION_PLAYER_FOUR]
                        .getPlayers()[Constants.PLAYER_ID_PLAYER_FOUR]
                        .setName(playerNames[Constants.PLAYER_ID_PLAYER_FOUR]);
            }
        }
    }
}
