package game;

import config.Constants;
import helpers.DebugHelper;
import models.ActionToken;
import models.PlayerInfo;
import proxy.Action;
import proxy.Field;
import proxy.Player;
import proxy.enums.BlockType;
import proxy.enums.BombState;
import proxy.enums.PlayerColor;
import proxy.enums.PlayerDirection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * GameLogic-Singleton used to execute a game-tick
 */
public class GameLogic {
    private static final ArrayList<Action> PLAYER_ACTIONS = new ArrayList<>();
    private static final Object instanceLock = new Object();
    private static final Integer[] scoreboard = new Integer[Constants.MAX_PLAYER_COUNT];
    private static GameLogic instance;
    private static GameState gameState;
    private final ActionToken[] actionTokens = new ActionToken[Constants.MAX_PLAYER_COUNT];

    /**
     * Private constructor to achieve Singleton-Pattern
     */
    private GameLogic() {
    }

    /**
     * Returns the only existing GameLogic-Instance or
     * creates an instance if it doesn't exist yet.
     *
     * @return unique instance of GameLogic
     */
    public static GameLogic getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new GameLogic();
                gameState = GameState.getInstance();
                PLAYER_ACTIONS.clear();
                Arrays.fill(scoreboard, -1);
                instance.fillActionTokens();
            }
            return instance;
        }
    }

    /**
     * Performs a game-tick with the following steps: <br>
     * 1.Retrieves the map of the game, the list of playerActions and the current playerInfos <br>
     * 2.Reduces all playerToken cooldowns by the duration of one GAME_LOGIC_TICK_DURATION <br>
     * 3.Evaluates all playerActions in the retrieved list of playerActions <br>
     * 4.Updates all bombStates in the retrieved map <br>
     * 5.Evaluates all dead players <br>
     * 6.Updates the gameState map with the new map <br>
     * 7.Retrieves the playerInfos after all updates and counts the amount of players still alive <br>
     * 8.Writes the new scores to the scoreboard <br>
     * 9.Resumes to sleep for the remaining time in the tick <br>
     * 10.Returns if the game has ended.
     *
     * @return true if the game is still running, else false
     */
    public Boolean tick() {
        long start = System.currentTimeMillis();
        Field[] map = gameState.retrieveCurrentMap();
        ArrayList<Action> playerActions = retrieveRecentPlayerActions();
        PlayerInfo[] playerInfos = findPlayerInfos(map);
        for (ActionToken actionToken : actionTokens) {
            actionToken.reduceCooldowns(Constants.GAME_LOGIC_TICK_DURATION);
        }
        for (Action action : playerActions) {
            attemptActionPlaceBomb(action, map, playerInfos);
            attemptActionMoveDirection(action, map, playerInfos);
        }
        updateBombs(map);
        updatePlayers(map);
        gameState.updateCurrentMap(map);
        playerInfos = findPlayerInfos(map);
        int playerCount = 0;
        for (PlayerInfo playerInfo : playerInfos) {
            if (playerInfo != null) {
                playerCount++;
            }
        }
        writeScores(playerInfos, playerCount);
        if (Constants.DEBUG_MODE) {
            DebugHelper.printMapHumanReadable(map);
        }
        resumeTick(start);
        return playerCount > 1;
    }

    /**
     * Adds a new playerAction to the list of playerActions
     * to be evaluated by the gameLogic
     *
     * @param playerAction Action representing the intended actions of a player
     */
    public void addPlayerAction(Action playerAction) {
        synchronized (PLAYER_ACTIONS) {
            PLAYER_ACTIONS.add(playerAction);
        }
    }

    /**
     * Takes the playerActions added to the list of playerActions
     * and clears the list.
     *
     * @return ArrayList of Actions representing the playerActions sent to the gameLogic
     * during the last tick
     */
    private ArrayList<Action> retrieveRecentPlayerActions() {
        ArrayList<Action> copyOfPlayerActions = new ArrayList<>();
        synchronized (PLAYER_ACTIONS) {
            if (PLAYER_ACTIONS.size() > 0) {
                copyOfPlayerActions = new ArrayList<>(PLAYER_ACTIONS);
                PLAYER_ACTIONS.clear();
            }
        }
        return copyOfPlayerActions;
    }

    /**
     * Takes the start-time of the tick and sleeps for a duration equal to
     * GAME_LOGIC_TICK_DURATION minus the difference between start-time and now.
     *
     * @param start long representing start-time of the tick in milliseconds
     */
    private void resumeTick(long start) {
        long end = System.currentTimeMillis();
        try {
            long sleepTime = Constants.GAME_LOGIC_TICK_DURATION + start - end;
            if (sleepTime < 0) {
                sleepTime = 0;
            }
            if (Constants.DEBUG_MODE) {
                DebugHelper.printTickStats(start, end, sleepTime);
            }
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the actionTokens
     */
    private void fillActionTokens() {
        for (int i = 0; i < actionTokens.length; i++) {
            actionTokens[i] = new ActionToken();
        }
    }

    /**
     * Attempts to place a bomb at the player's location if the following conditions are met: <br>
     * 1.The player is alive <br>
     * 2.The player intends to place a bomb <br>
     * 3.The current location allows for a bomb to be placed <br>
     * 4.The player's bomb placement is off cooldown.
     *
     * @param action      Action represents the player's intended actions
     * @param map         Field[] represents the game map
     * @param playerInfos PlayerInfo[] representing all information of all players
     */
    private void attemptActionPlaceBomb(Action action, Field[] map, PlayerInfo[] playerInfos) {
        int actionPlayerId = action.getPlayerId();
        PlayerInfo playerInfo = playerInfos[actionPlayerId];
        if (playerInfo != null &&
                action.getBombPlanted() &&
                map[playerInfo.getIndex()].getBombState() == BombState.NO_BOMB &&
                actionTokens[actionPlayerId].placeBomb()) {
            map[playerInfo.getIndex()].setBombState(BombState.BLACK_STATE);
            map[playerInfo.getIndex()].setBombTimer(0);
        }
    }

    /**
     * Attempts to move the player if the following conditions are met: <br>
     * 1.The player is alive <br>
     * 2.The player intends to move <br>
     * 3.The movement is legal/valid <br>
     * 4.The player's movement is off cooldown.
     *
     * @param action      Action represents the player's intended actions
     * @param map         Field[] represents the game map
     * @param playerInfos PlayerInfo[] representing all information of all players
     */
    private void attemptActionMoveDirection(Action action, Field[] map, PlayerInfo[] playerInfos) {
        int actionPlayerId = action.getPlayerId();
        PlayerInfo playerInfo = playerInfos[actionPlayerId];
        if (playerInfo != null &&
                action.getPlayerDirection() != PlayerDirection.NO_DIRECTION &&
                playerDirectionValid(map, action, playerInfo) &&
                actionTokens[actionPlayerId].move()) {
            Player player = new Player(playerInfo.getName(), playerInfo.getColor(), playerInfo.getDirection());
            map[playerInfo.getIndex()].getPlayers()[actionPlayerId] = null;
            switch (action.getPlayerDirection()) {
                case DOWN -> map[playerInfo.getX() + (playerInfo.getY() + 1) * Constants.MAP_WIDTH]
                        .getPlayers()[action.getPlayerId()] = player;
                case UP -> map[playerInfo.getX() + (playerInfo.getY() - 1) * Constants.MAP_WIDTH]
                        .getPlayers()[action.getPlayerId()] = player;
                case LEFT -> map[(playerInfo.getX() - 1) + playerInfo.getY() * Constants.MAP_WIDTH]
                        .getPlayers()[action.getPlayerId()] = player;
                case RIGHT -> map[(playerInfo.getX() + 1) + playerInfo.getY() * Constants.MAP_WIDTH]
                        .getPlayers()[action.getPlayerId()] = player;
            }
        }
    }

    /**
     * Evaluates if the movement requested by the given action is valid in the given map
     * for the given playerInfo of a player.
     *
     * @param map        Field[] represents the game map
     * @param action     Action represents the player's intended actions
     * @param playerInfo PlayerInfo represents all information of the selected player
     * @return true if the movement is valid, else false
     */
    private boolean playerDirectionValid(Field[] map, Action action, PlayerInfo playerInfo) {
        boolean validMove = false;
        switch (action.getPlayerDirection()) {
            case RIGHT -> {
                if (playerInfo.getX() + 1 < Constants.MAP_WIDTH) {
                    int newIndex = (playerInfo.getX() + 1) + playerInfo.getY() * Constants.MAP_WIDTH;
                    boolean noWall = map[newIndex].getGround() != BlockType.SOLID_WALL && map[newIndex].getGround() != BlockType.BRITTLE_WALL;
                    boolean noBomb = map[newIndex].getBombState() == BombState.NO_BOMB || map[newIndex].getBombState() == BombState.FIRE_STATE;
                    validMove = noWall && noBomb;
                }
            }
            case LEFT -> {
                if (playerInfo.getX() - 1 >= 0) {
                    int newIndex = (playerInfo.getX() - 1) + playerInfo.getY() * Constants.MAP_WIDTH;
                    boolean noWall = map[newIndex].getGround() != BlockType.SOLID_WALL && map[newIndex].getGround() != BlockType.BRITTLE_WALL;
                    boolean noBomb = map[newIndex].getBombState() == BombState.NO_BOMB || map[newIndex].getBombState() == BombState.FIRE_STATE;
                    validMove = noWall && noBomb;
                }
            }
            case DOWN -> {
                if (playerInfo.getY() + 1 < Constants.MAP_WIDTH) {
                    int newIndex = playerInfo.getX() + (playerInfo.getY() + 1) * Constants.MAP_WIDTH;
                    boolean noWall = map[newIndex].getGround() != BlockType.SOLID_WALL && map[newIndex].getGround() != BlockType.BRITTLE_WALL;
                    boolean noBomb = map[newIndex].getBombState() == BombState.NO_BOMB || map[newIndex].getBombState() == BombState.FIRE_STATE;
                    validMove = noWall && noBomb;
                }
            }
            case UP -> {
                if (playerInfo.getY() - 1 >= 0) {
                    int newIndex = playerInfo.getX() + (playerInfo.getY() - 1) * Constants.MAP_WIDTH;
                    boolean noWall = map[newIndex].getGround() != BlockType.SOLID_WALL && map[newIndex].getGround() != BlockType.BRITTLE_WALL;
                    boolean noBomb = map[newIndex].getBombState() == BombState.NO_BOMB || map[newIndex].getBombState() == BombState.FIRE_STATE;
                    validMove = noWall && noBomb;
                }
            }
        }
        return validMove;
    }

    /**
     * Searches for all information on each player in the given map.
     *
     * @param map Field[] representing the game map
     * @return PlayerInfo[] representing the playerInfo of each player
     */
    private PlayerInfo[] findPlayerInfos(Field[] map) {
        PlayerInfo[] playerInfos = new PlayerInfo[Constants.MAX_PLAYER_COUNT];
        for (int i = 0; i < map.length; i++) {
            Player[] players = map[i].getPlayers();
            for (int j = 0; j < players.length; j++) {
                if (players[j] != null) {
                    int x = i % Constants.MAP_WIDTH;
                    int y = i / Constants.MAP_WIDTH;
                    String name = players[j].getName();
                    PlayerColor color = players[j].getColor();
                    PlayerDirection direction = players[j].getDirection();
                    playerInfos[j] = new PlayerInfo(j, x, y, i, name, color, direction);
                }
            }
        }
        return playerInfos;
    }

    /**
     * Increases the bombTimer of every bomb in the map
     * and changes their bombStates according to the bombTimer.
     *
     * @param map Field[] representing the game map
     */
    private void updateBombs(Field[] map) {
        for (Field field : map) {
            long bombTimer = field.getBombTimer();
            if (field.getBombState() != BombState.NO_BOMB) {
                bombTimer += Constants.GAME_LOGIC_TICK_DURATION;
            }
            if (bombTimer == Constants.BOMB_STATE_COOLDOWN) {
                field.setBombState(BombState.RED_STATE);
            }
            if (bombTimer == Constants.BOMB_STATE_COOLDOWN * 2) {
                field.setBombState(BombState.EXPLODING_STATE);
            }
            if (bombTimer == Constants.BOMB_STATE_COOLDOWN * 3) {
                this.triggerBomb(map, field, bombTimer);
            }
            if (bombTimer >= Constants.BOMB_STATE_COOLDOWN * 4) {
                field.setBombState(BombState.NO_BOMB);
                if (field.getGround() == BlockType.EXPLODED_WALL) {
                    field.setGround(BlockType.EXPLODED_DIRT);
                } else {
                    field.setGround(BlockType.EXPLODED_GRASS);
                }
                bombTimer = 0;
            }
            field.setBombTimer(bombTimer);
        }
    }

    /**
     * Triggers the bomb in the given field and spreads the bombExplosion
     * to the next two fields in each direction.
     *
     * @param map       Field[] representing the game map
     * @param field     Field representing the field where the bomb needs to be triggered
     * @param bombTimer long bombTimer of the location
     */
    private void triggerBomb(Field[] map, Field field, long bombTimer) {
        field.setGround(BlockType.EXPLODED_GRASS);
        field.setBombState(BombState.FIRE_STATE);
        field.setBombTimer(bombTimer);
        int x = field.getX();
        int y = field.getY();
        if (x + 2 < Constants.MAP_WIDTH) {
            int indexInbetween = x + 1 + y * Constants.MAP_WIDTH;
            int indexHere = x + 2 + y * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere, indexInbetween);
        }
        if (x + 1 < Constants.MAP_WIDTH) {
            int indexHere = x + 1 + y * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere);
        }
        if (x - 2 >= 0) {
            int indexInbetween = x - 1 + y * Constants.MAP_WIDTH;
            int indexHere = x - 2 + y * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere, indexInbetween);
        }
        if (x - 1 >= 0) {
            int indexHere = x - 1 + y * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere);
        }
        if (y + 2 < Constants.MAP_WIDTH) {
            int indexInbetween = x + (y + 1) * Constants.MAP_WIDTH;
            int indexHere = x + (y + 2) * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere, indexInbetween);
        }
        if (y + 1 < Constants.MAP_WIDTH) {
            int indexHere = x + (y + 1) * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere);
        }
        if (y - 2 >= 0) {
            int indexInbetween = x + (y - 1) * Constants.MAP_WIDTH;
            int indexHere = x + (y - 2) * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere, indexInbetween);
        }
        if (y - 1 >= 0) {
            int indexHere = x + (y - 1) * Constants.MAP_WIDTH;
            spreadFire(map, bombTimer, indexHere);
        }
    }

    /**
     * Spreads the fire of a bombExplosion to the field at the given indexHere if possible
     * and if the field at the given indexInbetween allows it.
     * If the fire hits another bomb, it triggers that bomb.
     *
     * @param map            Field[] representing the game map
     * @param bombTimer      long timer of the exploded bomb
     * @param indexHere      int index of the field in the map
     * @param indexInbetween int index of the field between the bomb and indexHere
     */
    private void spreadFire(Field[] map, long bombTimer, int indexHere, int indexInbetween) {
        boolean noWallInbetween = map[indexInbetween].getGround() != BlockType.SOLID_WALL && map[indexInbetween].getGround() != BlockType.BRITTLE_WALL;
        boolean noBombInbetween = map[indexInbetween].getBombState() == BombState.NO_BOMB || map[indexInbetween].getBombState() == BombState.FIRE_STATE;
        boolean noSolidWallHere = map[indexHere].getGround() != BlockType.SOLID_WALL;
        boolean noBombHere = map[indexHere].getBombState() == BombState.NO_BOMB || map[indexHere].getBombState() == BombState.FIRE_STATE;
        if (noWallInbetween && noBombInbetween && noSolidWallHere && noBombHere) {
            if (map[indexHere].getGround() == BlockType.BRITTLE_WALL) {
                map[indexHere].setGround(BlockType.EXPLODED_WALL);
            } else {
                map[indexHere].setGround(BlockType.EXPLODED_GRASS);
            }
            map[indexHere].setBombState(BombState.FIRE_STATE);
            map[indexHere].setBombTimer(bombTimer);
        } else if (noWallInbetween && noBombInbetween && !noBombHere) {
            this.triggerBomb(map, map[indexHere], bombTimer);
        }
    }

    /**
     * Spreads the fire of a bombExplosion to the field at the given indexHere if possible.
     * If the fire hits another bomb, it triggers that bomb.
     *
     * @param map       Field[] representing the game map
     * @param bombTimer long timer of the exploded bomb
     * @param indexHere int index of the field in the map
     */
    private void spreadFire(Field[] map, long bombTimer, int indexHere) {
        boolean noSolidWallHere = map[indexHere].getGround() != BlockType.SOLID_WALL;
        boolean noBombHere = map[indexHere].getBombState() == BombState.NO_BOMB || map[indexHere].getBombState() == BombState.FIRE_STATE;
        if (noSolidWallHere && noBombHere) {
            if (map[indexHere].getGround() == BlockType.BRITTLE_WALL) {
                map[indexHere].setGround(BlockType.EXPLODED_WALL);
            } else {
                map[indexHere].setGround(BlockType.EXPLODED_GRASS);
            }
            map[indexHere].setBombState(BombState.FIRE_STATE);
            map[indexHere].setBombTimer(bombTimer);
        } else if (!noBombHere) {
            this.triggerBomb(map, map[indexHere], bombTimer);
        }
    }

    /**
     * Looks for all players in the map and kills them if they are standing in fire.
     *
     * @param map Field[] representing the game map
     */
    private void updatePlayers(Field[] map) {
        for (Field field : map) {
            if (field.getBombState() == BombState.FIRE_STATE) {
                Arrays.fill(field.getPlayers(), null);
            }
        }
    }

    /**
     * Writes the scores to scoreboard according to playerInfo and playerCount.
     * Scores for a player are either written if they died or if they won the game.
     *
     * @param playerInfos PlayerInfo[] of all players in the game
     * @param playerCount int amount of players still alive
     */
    private void writeScores(PlayerInfo[] playerInfos, int playerCount) {
        for (int i = 0; i < playerInfos.length; i++) {
            if (playerInfos[i] == null && scoreboard[i] == -1) {
                scoreboard[i] = 3 - playerCount;
            } else if (playerInfos[i] != null && scoreboard[i] == -1 && playerCount == 1) {
                scoreboard[i] = 3;
            }
        }
    }

    /**
     * Returns the current scoreboard.
     *
     * @return Integer[] representing scores for each player
     */
    public Integer[] getScoreboard() {
        return scoreboard;
    }

    /**
     * Resets the gameLogic
     */
    public void reset() {
        instance = null;
    }
}
