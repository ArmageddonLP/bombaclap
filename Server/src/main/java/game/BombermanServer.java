package game;

import config.Constants;
import helpers.DebugHelper;
import helpers.MapHelper;
import proxy.Field;

/**
 * BombermanServer-Singleton-Thread used as a lobby for the Bomberman game
 */
public class BombermanServer extends Thread {
    private static final Object connectionLock = new Object();
    private static final Object lobbyLock = new Object();
    private static final Object instanceLock = new Object();
    private static BombermanServer instance;
    private static GameState gameState;
    private final String[] connectedPlayers = new String[Constants.MAX_PLAYER_COUNT];
    private Integer activeConnections = 0;
    private Boolean gameRunning = false;
    private Boolean countdownStarted = false;
    private Long countdown = Constants.GAME_START_DELAY;

    /**
     * Private constructor to achieve Singleton-Pattern
     */
    private BombermanServer() {
    }

    /**
     * Returns the only existing BombermanServer-Instance or
     * creates an instance if it doesn't exist yet and starts the thread.
     *
     * @return unique instance of BombermanServer
     */
    public static BombermanServer getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new BombermanServer();
                gameState = GameState.getInstance();
                instance.setDaemon(true);
                instance.start();
            }
            return instance;
        }
    }

    /**
     * Returns whether the game is running.
     *
     * @return true if running, else false
     */
    public boolean isGameRunning() {
        synchronized (lobbyLock) {
            return gameRunning;
        }
    }

    /**
     * Returns whether the timer has been started.
     *
     * @return true if started, else false
     */
    public boolean getCountdownStarted() {
        synchronized (lobbyLock) {
            return countdownStarted;
        }
    }

    /**
     * Retrieves the current time until the game starts.
     *
     * @return long representing the milliseconds until the game starts
     */
    public long getCountdown() {
        synchronized (lobbyLock) {
            return countdown;
        }
    }

    /**
     * Returns the amount of active connections.
     *
     * @return int representing the amount of connected players
     */
    public int getActiveConnections() {
        synchronized (connectionLock) {
            return activeConnections;
        }
    }

    /**
     * Adds a new connection to the active connections of the bombermanServer.
     * This method finds the first free slot in the list of connected players.
     *
     * @param playerName playerName of the new connection as String
     * @return int representing the playerId designated to this connection,
     * returns -1 if there are no slots open for the new connection
     */
    public int addNewConnection(String playerName) {
        synchronized (connectionLock) {
            activeConnections++;
            for (int i = 0; i < connectedPlayers.length; i++) {
                if (connectedPlayers[i] == null) {
                    connectedPlayers[i] = playerName;
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Removes a formerly active connection according to the playerId given.
     *
     * @param playerId playerId of the formerly active connection that needs to be removed
     */
    public void removeActiveConnection(int playerId) {
        synchronized (connectionLock) {
            activeConnections--;
            connectedPlayers[playerId] = null;
        }
    }

    /**
     * Returns the list of players currently connected to the bombermanServer.
     *
     * @return Array of Strings, each String is either a playerName if the player is connected,
     * or null if the playerId is not yet taken
     */
    public String[] getConnectedPlayers() {
        synchronized (connectionLock) {
            return connectedPlayers;
        }
    }

    /**
     * Takes the startTime of the run loop and sleeps for a duration equal to the BOMBERMAN_SERVER_TICK_DURATION
     * minus the difference between startTime and now.
     *
     * @param start long representing start-time of the loop in milliseconds
     */
    private void resumeRun(long start) {
        long end = System.currentTimeMillis();
        try {
            long sleepTime = Constants.BOMBERMAN_SERVER_TICK_DURATION + start - end;
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
     * Takes the current map from gameState,
     * puts the provided playerNames into the map and
     * updates the gameState map.
     *
     * @param playerNames Array of Strings, each String is either a playerName if the player is connected,
     *                    or null if the playerId is not yet taken
     */
    private void updateMapWithPlayerNames(String[] playerNames) {
        Field[] map = gameState.retrieveCurrentMap();
        MapHelper.renamePlayers(map, playerNames);
        gameState.updateCurrentMap(map);
    }

    /**
     * Starts the countdown if it hasn't been started yet and enough players have joined.
     * Decreases the countdown by an amount equal to the BOMBERMAN_SERVER_TICK_DURATION.
     */
    private void updateCountdown() {
        if (!countdownStarted && getActiveConnections() == Constants.MAX_PLAYER_COUNT) {
            countdownStarted = true;
        }
        if (countdownStarted) {
            countdown -= Constants.BOMBERMAN_SERVER_TICK_DURATION;
        }
    }

    /**
     * Starts the game if the countdown has reached zero
     * and proceeds to reset the countdown.
     */
    private void startGameWhenReady() {
        if (countdownStarted && countdown <= 0L) {
            countdownStarted = false;
            gameState.startGame();
            countdown = Constants.GAME_START_DELAY;
        }
    }

    /**
     * BombermanServer-Thread run-function executed after Bomberman-Thread has been started.
     * Runs periodically until terminated and loops the following actions <br>
     * 1.Takes all playerNames of currently connected players <br>
     * 2.Asks the gameState if the game has started <br>
     * 2.1.If the game hasn't started the gameState is queried again <br>
     * 2.2.The map is updated with the playerNames <br>
     * 2.3.The countdown is updated <br>
     * 2.4.The game is started if the countdown hit zero <br>
     */
    @Override
    public void run() {
        while (true) {
            long start = System.currentTimeMillis();
            String[] playerNames;
            synchronized (connectionLock) {
                playerNames = connectedPlayers.clone();
            }
            synchronized (lobbyLock) {
                if (gameState != null) {
                    gameRunning = gameState.isRunning();
                } else {
                    gameRunning = false;
                }
                if (!gameRunning) {
                    gameState = GameState.getInstance();
                    updateMapWithPlayerNames(playerNames);
                    updateCountdown();
                    startGameWhenReady();
                }
            }
            resumeRun(start);
        }
    }
}

