package game;

import config.Constants;
import helpers.HighscoreHelper;
import proxy.Field;

import java.util.ArrayList;

/**
 * GameState-Singleton-Thread used as single-source-of-truth
 */
public class GameState extends Thread {
    private static final Object mapLock = new Object();
    private static final Object instanceLock = new Object();
    private static Field[] map;
    private static GameState instance;
    private static GameLogic gameLogic;
    private static BombermanServer bombermanServer;
    private static Boolean running = false;

    /**
     * Private constructor to achieve Singleton-Pattern
     */
    private GameState() {
    }

    /**
     * Returns the only existing GameState-Instance or
     * creates an instance if it doesn't exist yet and starts the thread.
     *
     * @return unique instance of GameState
     */
    public static GameState getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new GameState();
                gameLogic = GameLogic.getInstance();
                bombermanServer = BombermanServer.getInstance();
                instance.setDaemon(true);
                map = new MapBuilder()
                        .generateMap()
                        .generatePlayers(new String[]{
                                Constants.NAME_PLAYER_ONE,
                                Constants.NAME_PLAYER_TWO,
                                Constants.NAME_PLAYER_THREE,
                                Constants.NAME_PLAYER_FOUR})
                        .generateRandomMaze(Constants.FOLDER_LOCATION_MAZES)
                        .retrieveMap();
            }
            return instance;
        }
    }

    /**
     * Returns the current map of the gameState.
     *
     * @return Field[] map of the game
     */
    public Field[] retrieveCurrentMap() {
        synchronized (mapLock) {
            return map;
        }
    }

    /**
     * Updates the current map with the given newMap
     *
     * @param newMap Field[] new map of the game
     */
    public void updateCurrentMap(Field[] newMap) {
        synchronized (mapLock) {
            map = newMap;
        }
    }

    /**
     * Starts the game
     */
    public void startGame() {
        running = true;
        instance.setDaemon(true);
        instance.start();
    }

    /**
     * Resets the game
     */
    private void resetGame() {
        gameLogic.reset();
        instance = null;
    }

    /**
     * Returns if the game is running.
     *
     * @return true if running, else false
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Appends the most recent scores to both scoreboard-csv-files
     */
    private void persistScoreboard() {
        Integer[] scores = gameLogic.getScoreboard();
        String[] names = bombermanServer.getConnectedPlayers();
        String[] namedScores = {
                scores[0].toString(),
                scores[1].toString(),
                scores[2].toString(),
                scores[3].toString()};

        ArrayList<Integer[]> scoreboardsByColor = HighscoreHelper.readScoreboardByColor();
        scoreboardsByColor.add(scores);
        HighscoreHelper.writeScoreboardByColor(scoreboardsByColor);

        ArrayList<String[]> scoreboardsByName = HighscoreHelper.readScoreboardByName();
        scoreboardsByName.add(names);
        scoreboardsByName.add(namedScores);
        HighscoreHelper.writeScoreboardByName(scoreboardsByName);
    }

    /**
     * GameState-Thread run-function executed after GameState-Thread has been started.
     * Runs periodically while the game is running executing gameLogic-ticks on a loop.
     * If the game stops running, the scores are saved and the gameState is reset.
     */
    @Override
    public void run() {
        while (running) {
            running = gameLogic.tick();
        }
        persistScoreboard();
        resetGame();
    }
}
