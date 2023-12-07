import config.Constants;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import proxy.Field;
import views.BlockView;
import views.BorderView;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Handles all input from the server to the client. This includes the map and timer.
 */
public class ClientIn extends Thread {
    private final BufferedReader serverIn;
    private final ArrayList<BlockView> blockViews;
    private final BorderView borderView;
    private final int playerId;
    private final StackPane timerStack;
    private Text timerText;
    private HBox timerBack;


    public ClientIn(BufferedReader serverIn, ArrayList<BlockView> blockViews, BorderView borderView, int playerId, StackPane timer) {
        this.serverIn = serverIn;
        this.blockViews = blockViews;
        this.borderView = borderView;
        this.playerId = playerId;
        this.timerStack = timer;
        for (Node node : timer.getChildren()) {
            if (node.getId() == Constants.timerTextId) {
                timerText = (Text) node;
            } else if (node.getId() == Constants.timerBackId) {
                timerBack = (HBox) node;
            }
        }
    }


    /**
     * Counts down the game start
     *
     * @param timer
     */
    public void setTimer(Long timer) {
        if (timerStack.getChildren().size() == 0) {
            Platform.runLater(() -> {
                timerStack.getChildren().add(timerBack);
                timerStack.getChildren().add(timerText);
            });
        }
        if (timer <= 100) {
            Platform.runLater(() -> timerStack.getChildren().clear());
            return;
        }
        Platform.runLater(() -> timerText.setText("Game starting in " + (timer / 1000 + 1) + " second(s)"));
    }

    /**
     * Gets messages from the server and parses them with the MessageParser.
     * Schedules update for UI and Timer.
     */
    public void run() {
        try {
            while (true) {
                String message = serverIn.readLine();
                Field[] map = MessageParser.parseMessage(message);
                Long timer = MessageParser.parseTimerMessage(message);
                if (timer != null) {
                    setTimer(timer);
                }
                if (map == null) {
                    continue;
                }

                borderView.setPlayerId(playerIdIfAlive(playerId, map));
                for (int i = 0; i < map.length; i++) {
                    if (blockViews.size() < i + 1) {
                        blockViews.add(new BlockView(i));
                    }

                    blockViews.get(i).setField(map[i]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Checks if player is still alive. Used for rendering the border
     *
     * @param playerId
     * @param map
     * @return the player id, if he's alive. -1 if the player is dead
     */
    private int playerIdIfAlive(int playerId, Field[] map) {
        if (playerId > 3) {
            return -1;
        }
        for (Field field : map) {
            if (field.getPlayers()[playerId] != null) {
                return playerId;
            }
        }
        return -1;
    }
}
