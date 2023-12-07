import adapters.ActionTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyEvent;
import proxy.Action;
import proxy.enums.PlayerDirection;

import java.io.PrintWriter;

/**
 * Handles the actions which the user can take and interacts with the server.
 */
public class HandlePlayerAction extends Thread {
    private final int playerId;
    private final PrintWriter serverOut;
    private final SimpleObjectProperty<KeyEvent> keyPressed;
    private Action playerAction;

    public HandlePlayerAction(PrintWriter serverOut, int playerId, SimpleObjectProperty<KeyEvent> keyPressed) {
        this.serverOut = serverOut;
        this.playerId = playerId;
        this.keyPressed = keyPressed;
        this.playerAction = new Action(playerId, PlayerDirection.NO_DIRECTION, false);
        addKeyListener();
    }

    public void setPlayerDirection(PlayerDirection playerDirection) {
        this.playerAction.setPlayerDirection(playerDirection);
    }

    public void setBombPlanted(boolean bombPlanted) {
        this.playerAction.setBombPlanted(bombPlanted);
    }

    /**
     * Handles key input from the user. Sets the values in the action which get sent to the server.
     */
    private void addKeyListener() {
        this.keyPressed.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            keyPressed.setValue(null);
            switch (newValue.getCode()) {
                case W, UP -> setPlayerDirection(PlayerDirection.UP);
                case S, DOWN -> setPlayerDirection(PlayerDirection.DOWN);
                case A, LEFT -> setPlayerDirection(PlayerDirection.LEFT);
                case D, RIGHT -> setPlayerDirection(PlayerDirection.RIGHT);
                default -> setPlayerDirection(PlayerDirection.NO_DIRECTION);
            }
            switch (newValue.getCode()) {
                case SPACE -> setBombPlanted(true);
                default -> setBombPlanted(false);
            }
        });
    }

    /**
     * Periodically sends actions to the server. These actions can be empty if the user has not taken any action.
     */
    @Override
    public void run() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Action.class, new ActionTypeAdapter());
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        while (true) {
            try {
                serverOut.println(gson.toJson(this.playerAction).replaceAll("\r*\n*", ""));
                this.playerAction = new Action(playerId, PlayerDirection.NO_DIRECTION, false);
                sleep(50);

            } catch (InterruptedException e) {
                System.out.println("Failed to send action " + e.getMessage());
            }
        }
    }
}
