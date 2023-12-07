package views;

import config.Constants;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Class to add a border in the color of the player (or black if dead) to the map
 */
public class BorderView {
    public final HBox top;
    public final HBox bottom;
    public final VBox left;
    public final VBox right;
    private Color color;
    private boolean isRendering = false;

    public BorderView() {
        top = new HBox();
        top.setMinHeight(Constants.borderWidth);
        bottom = new HBox();
        bottom.setMinHeight(Constants.borderWidth);
        left = new VBox();
        left.setMinWidth(Constants.borderWidth);
        right = new VBox();
        right.setMinWidth(Constants.borderWidth);
        color = Color.BLACK;
    }

    /**
     * Sets PlayerColor parsed from player id
     *
     * @param playerId
     */
    public void setPlayerId(int playerId) {
        switch (playerId) {
            case 0 -> color = Color.BLUE;
            case 1 -> color = Color.RED;
            case 2 -> color = Color.GREEN;
            case 3 -> color = Color.YELLOW;
            default -> color = Color.BLACK;
        }
        render();
    }

    /**
     * Renders map border
     */
    private void render() {
        if (isRendering) {
            return;
        }
        isRendering = true;
        Platform.runLater(() -> {
            top.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            left.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            right.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
            bottom.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
        });
        isRendering = false;
    }

}
