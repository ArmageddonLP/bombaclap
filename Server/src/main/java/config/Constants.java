package config;

import proxy.enums.PlayerColor;

/**
 * Constants used across the Server to provide ease of access and guarantee of unity
 */
public class Constants {
    public static final Boolean DEBUG_MODE = false;
    //region Server
    public static final Integer PORT = 8765;
    //endregion
    //region Tick-Durations and Cooldowns
    /**
     * Base tick duration for all processes
     */
    public static final Long TICK_DURATION = 15L;
    public static final Long SERVER_TICK_DURATION = TICK_DURATION * 3;
    public static final Long BOMBERMAN_SERVER_TICK_DURATION = TICK_DURATION;
    public static final Long GAME_LOGIC_TICK_DURATION = TICK_DURATION;
    public static final Long PLAYER_MOVEMENT_COOLDOWN = GAME_LOGIC_TICK_DURATION * 5;
    public static final Long PLAYER_BOMB_COOLDOWN = GAME_LOGIC_TICK_DURATION * 30;
    public static final Long BOMB_STATE_COOLDOWN = GAME_LOGIC_TICK_DURATION * 50;
    //endregion
    //region Map
    /**
     * Amount of rows and lines in a map
     */
    public static final Integer MAP_WIDTH = 15;
    /**
     * Amount of fields in a map
     */
    public static final Integer MAP_SIZE = MAP_WIDTH * MAP_WIDTH;
    public static final Integer STARTPOSITION_PLAYER_FOUR = MAP_SIZE - 1;
    //endregion
    //region Game
    public static final Long GAME_START_DELAY = 5000L;
    public static final Integer MAX_PLAYER_COUNT = 4;
    public static final String DEFAULT_PLAYER_NAME = "Bombaclap";
    //endregion
    //region PlayerOne
    public static final Integer PLAYER_ID_PLAYER_ONE = 0;
    public static final Integer STARTPOSITION_PLAYER_ONE = 0;
    public static final PlayerColor COLOR_PLAYER_ONE = PlayerColor.BLUE;
    public static final String NAME_PLAYER_ONE = "Player 1";
    //endregion
    //region PlayerTwo
    public static final Integer PLAYER_ID_PLAYER_TWO = 1;
    public static final Integer STARTPOSITION_PLAYER_TWO = MAP_WIDTH - 1;
    public static final PlayerColor COLOR_PLAYER_TWO = PlayerColor.RED;
    public static final String NAME_PLAYER_TWO = "Player 2";
    //endregion
    //region PlayerThree
    public static final Integer PLAYER_ID_PLAYER_THREE = 2;
    public static final Integer STARTPOSITION_PLAYER_THREE = MAP_WIDTH * (MAP_WIDTH - 1);
    public static final PlayerColor COLOR_PLAYER_THREE = PlayerColor.GREEN;
    public static final String NAME_PLAYER_THREE = "Player 3";
    //endregion
    //region PlayerFour
    public static final Integer PLAYER_ID_PLAYER_FOUR = 3;
    public static final PlayerColor COLOR_PLAYER_FOUR = PlayerColor.YELLOW;
    public static final String NAME_PLAYER_FOUR = "Player 4";
    //endregion
    //region CSV
    public static final String CSV_SEPARATOR = ";";
    public static final String FOLDER_LOCATION_MAZES = "assets/maze";
    public static final String FILE_LOCATION_SCOREBOARD_COLOR = "scoreboardByColor.csv";
    public static final String FILE_LOCATION_SCOREBOARD_NAME = "scoreboardByName.csv";
    public static final Integer DEFAULT_BLOCK_TYPE_CODE = 3;
    //endregion
}
