package views;

import config.Constants;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import proxy.Field;
import proxy.Player;
import proxy.enums.BlockType;
import proxy.enums.BombState;

import java.io.FileInputStream;

/**
 * View which holds all the Logic to render the different blocks.
 */
public class BlockView {
    private final ImageView[] players;
    private final Text[] playerNames;
    private final int id;
    private boolean isRendering = false;
    private StackPane stackPane;
    private ImageView block;
    private ImageView bomb;
    private Field field;

    public BlockView(int id) {
        players = new ImageView[4];
        playerNames = new Text[4];
        this.id = id;
    }

    /**
     * Set player image path and name
     * Also overlapping has been accounted for with offset
     *
     * @param imagePath
     * @param playerId
     * @param playerName
     * @param offset
     */
    private void setPlayer(String imagePath, int playerId, String playerName, int offset) {
        if (players[playerId] == null) {
            players[playerId] = new ImageView();
        }
        if (playerNames[playerId] == null) {
            playerNames[playerId] = new Text();
        }
        try {
            Image image = new Image(new FileInputStream(imagePath));
            ImageView playerImage = new ImageView();
            playerImage.setImage(image);
            playerImage.setTranslateX(offset);
            playerImage.setTranslateY(offset);
            players[playerId] = playerImage;
            Text namePlate = new Text(playerName);
            namePlate.setFont(Font.font("Verdana", FontWeight.BOLD, 8));
            namePlate.setTranslateX(offset);
            namePlate.setTranslateY(offset);
            playerNames[playerId] = namePlate;
        } catch (Exception e) {
            System.out.println("Failed to load texture. " + e.getMessage());
        }
    }

    /**
     * Setting Players from player array
     *
     * @param players
     */
    private void setPlayers(Player[] players) {
        int offset = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                setPlayer(Constants.nothing, i, "", offset);
            } else {
                switch (players[i].getColor()) {
                    case BLUE -> setPlayer(Constants.playerOne, i, players[i].getName(), offset);
                    case RED -> setPlayer(Constants.playerTwo, i, players[i].getName(), offset);
                    case GREEN -> setPlayer(Constants.playerThree, i, players[i].getName(), offset);
                    case YELLOW -> setPlayer(Constants.playerFour, i, players[i].getName(), offset);
                }
                offset += 3;
            }
        }
    }

    /**
     * Set Bomb image source
     *
     * @param imagePath
     */
    private void setBomb(String imagePath) {
        if (bomb == null) {
            bomb = new ImageView();
        }
        try {
            Image image = new Image(new FileInputStream(imagePath));
            bomb.setImage(image);
        } catch (Exception e) {
            System.out.println("Failed to load texture. " + e.getMessage());
        }

    }

    private void setBomb(BombState state) {
        switch (state) {
            case BLACK_STATE -> setBomb(Constants.bombPlacedBlack);
            case RED_STATE -> setBomb(Constants.bombPlacedRed);
            case EXPLODING_STATE -> setBomb(Constants.bombExplode);
            case FIRE_STATE -> setBomb(Constants.fireFromExplosion);
            case NO_BOMB -> setBomb(Constants.nothing);
        }
    }

    /**
     * Set Block with image source
     *
     * @param imagePath
     */
    private void setBlock(String imagePath) {
        if (block == null) {
            block = new ImageView();
        }
        try {
            Image image = new Image(new FileInputStream(imagePath));
            block.setImage(image);
        } catch (Exception e) {
            System.out.println("Failed to load texture. " + e.getMessage());
        }
    }

    /**
     * Sets block depending on BlockType
     *
     * @param type
     */
    private void setBlock(BlockType type) {
        switch (type) {
            case SOLID_WALL -> setBlock(Constants.solidBlock);
            case BRITTLE_WALL -> setBlock(Constants.brittleBlock);
            case GRASS -> setBlock(Constants.grassBlock);
            case EXPLODED_GRASS -> setBlock(Constants.grassBlockFire);
            case DIRT, EXPLODED_DIRT -> setBlock(Constants.sandBlock);
            case EXPLODED_WALL -> setBlock(Constants.destroyedWall);
        }
    }

    /**
     * Checks if the player array has been modified
     *
     * @param newPlayers
     * @return
     */
    private boolean hasPlayerDelta(Player[] newPlayers) {
        boolean hasDelta = false;
        for (int i = 0; i < newPlayers.length; i++) {
            boolean hasMovedIn = newPlayers[i] != null && this.field.getPlayers()[i] == null;
            boolean hasMovedOut = newPlayers[i] == null && this.field.getPlayers()[i] != null;
            boolean hasChangedName = hasChangedName(newPlayers[i], this.field.getPlayers()[i]);
            hasDelta = hasDelta || (hasMovedIn || hasMovedOut || hasChangedName);
        }
        return hasDelta;
    }

    private boolean hasChangedName(Player newPlayer, Player oldPlayer) {
        if (newPlayer == null || oldPlayer == null) {
            return false;
        }
        return !newPlayer.getName().equals(oldPlayer.getName());
    }

    /**
     * Checks if the field has been modified
     * If the field has changed, the image views get updated immediately
     *
     * @param newField
     * @return
     */
    private boolean hasDelta(Field newField) {
        if (this.field == null) {
            this.field = newField;
            setBlock(newField.getGround());
            setBomb(newField.getBombState());
            setPlayers(newField.getPlayers());
            return true;
        }
        boolean isDifferent = false;

        if (this.field.getGround() != newField.getGround()) {
            setBlock(newField.getGround());
            isDifferent = true;
        }
        if (this.field.getBombState() != newField.getBombState()) {
            setBomb(newField.getBombState());
            isDifferent = true;
        }
        if (hasPlayerDelta(newField.getPlayers())) {
            setPlayers(newField.getPlayers());
            isDifferent = true;
        }
        this.field = newField;
        return isDifferent;
    }

    public void setField(Field field) {
        if (hasDelta(field)) {
            this.field = field;
            render();
        }
    }

    /**
     * Set up the stackpane which contains the image views
     * Reference is needed to be able to update the view asynchronously
     *
     * @param stackPane
     */
    public void setStackPane(StackPane stackPane) {
        if (this.stackPane == null) {
            stackPane.setMaxHeight(Constants.blockSize);
            stackPane.setPrefHeight(Constants.blockSize);
            stackPane.setMaxWidth(Constants.blockSize);
            stackPane.setPrefWidth(Constants.blockSize);
            this.stackPane = stackPane;
            if (field != null) {
                render();
            }
        }
    }

    public void render() {
        if (isRendering || this.stackPane == null) {
            return;
        }
        isRendering = true;
        Platform.runLater(() -> {
            this.stackPane.getChildren().clear();
            if (this.block != null) {
                this.stackPane.getChildren().add(this.block);
            }
            if (this.bomb != null) {
                this.stackPane.getChildren().add(this.bomb);
            }
            for (ImageView player : this.players) {
                if (player != null) {
                    this.stackPane.getChildren().add(player);
                }
            }
            for (Text playerName : this.playerNames) {
                if (playerName != null) {
                    this.stackPane.setAlignment(Pos.TOP_CENTER);
                    this.stackPane.getChildren().add(playerName);
                }
            }
            isRendering = false;
        });

    }
}

