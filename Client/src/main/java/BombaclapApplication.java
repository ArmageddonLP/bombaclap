import config.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import views.BlockView;
import views.BorderView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * Main application for JavaFx
 */
public class BombaclapApplication extends Application {
    private static final ArrayList<BlockView> blockViews = new ArrayList<>();
    private static final SimpleObjectProperty<KeyEvent> keyPressed = new SimpleObjectProperty<>();
    private static final BorderView borderView = new BorderView();
    private File[] musicList;
    private int currentSongIndex = 0;
    private Thread musicThread = null;
    private boolean isChangingMusic = false;

    private String host;
    private String username;
    private String port;
    private MediaPlayer mediaPlayer;
    private Stage stage;

    public static void launchBombaclapApplication(String[] args) {
        for (int i = 0; i < Constants.blocksPerLine * Constants.blocksPerLine; i++) {
            blockViews.add(new BlockView(i));
        }
        launch();
    }

    /**
     * Setting up layout for bomberman
     *
     * @return HBox Root component, which holds the bomberman layout.
     */
    public HBox setupLayout() {
        HBox root = new HBox();
        VBox vBox = new VBox();
        HBox hBox = new HBox();

        root.getChildren().add(borderView.left);
        vBox.getChildren().add(borderView.top);


        for (int i = 0; i < blockViews.size(); i++) {
            StackPane stackPane = new StackPane();
            blockViews.get(i).setStackPane(stackPane);
            hBox.getChildren().add(stackPane);
            if ((i + 1) % Constants.blocksPerLine == 0) {
                vBox.getChildren().add(hBox);
                hBox = new HBox();
            }
        }
        vBox.getChildren().add(borderView.bottom);
        root.getChildren().add(vBox);
        root.getChildren().add(borderView.right);

        return root;
    }

    private StackPane setupTimer() {
        Text timerText = new Text("Waiting for players");
        timerText.setId(Constants.timerTextId);
        timerText.setFont(new Font(30));
        timerText.setStyle("-fx-text-fill: black");
        HBox timerBack = new HBox();
        timerBack.setMaxHeight(100);
        timerBack.setMaxWidth(500);
        timerBack.setStyle("-fx-background-color: lightgrey; -fx-opacity: 75%");
        timerBack.setId(Constants.timerBackId);
        StackPane timer = new StackPane(timerBack, timerText);
        timer.setAlignment(Pos.CENTER);
        return timer;
    }

    /**
     * Starting JavaFx application. Also doing some initializing for the bomberman client
     *
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        HBox rootBox = setupLayout();
        StackPane root = new StackPane(rootBox);
        Scene scene = new Scene(root, Constants.mapWidth, Constants.mapWidth);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressed::setValue);

        requestConnectionData();

        StackPane timer = setupTimer();
        root.getChildren().add(timer);

        // Start client thread
        Client client = new Client(blockViews, borderView, keyPressed, this.host, this.port, this.username, timer);
        client.setDaemon(true);
        client.start();

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Bombaclap");
        stage.show();

        this.stage = stage;
        setUpMusic();
    }

    private void setUpMusic() {
        loadMusicList();
        startMusicPlayback();
        addChangeMusicListener();
    }

    /**
     * Starts music for intense gaming action
     */
    private void startMusicPlayback() {
        File musicFile = musicList[currentSongIndex];
        if (musicFile == null) return;
        String uri = "file:/" + musicFile.getAbsolutePath().replaceAll("\\\\", "/");
        uri = uri.replace(" ", "%20");
        changeMusic(uri, musicFile.getName());
    }

    /**
     * Initial loading of music list
     */
    private void loadMusicList() {
        File musicDir = new File(Constants.musicFolder);
        this.musicList = musicDir.listFiles();
    }

    /**
     * Changes the music to a new song
     *
     * @param uri
     */
    private void changeMusic(String uri, String fileName) {
        Runnable mediaRunnable = () -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            mediaPlayer = null;
            Media media;
            try {
                media = new Media(uri);
            } catch (Exception e) {
                System.out.println("Failed to load media: " + e.getMessage());
                isChangingMusic = false;
                return;
            }

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.15);
            mediaPlayer.setOnEndOfMedia(this::nextSong);
            mediaPlayer.play();

            setMusicTitle(fileName);
            isChangingMusic = false;
        };

        isChangingMusic = true;
        musicThread = new Thread(mediaRunnable);
        musicThread.setDaemon(true);
        musicThread.start();
    }

    private void setMusicTitle(String fileName) {
        String newTitle = stage.getTitle().split(" - ")[0] + " - Currently playing: " + fileName.replace(".mp3", "");
        Platform.runLater(() -> stage.setTitle(newTitle));
    }

    /**
     * Changes the music to a new song
     *
     * @param index
     */
    private void changeMusic(int index) {
        currentSongIndex = index;
        File musicFile = musicList[index];
        if (musicFile == null) return;
        String uri = "file:/" + musicFile.getAbsolutePath().replaceAll("\\\\", "/");
        uri = uri.replace(" ", "%20");
        changeMusic(uri, musicFile.getName());
    }

    /**
     * Changes music to next song
     */
    private void nextSong() {
        if (currentSongIndex < musicList.length - 1) {
            changeMusic(currentSongIndex + 1);
            return;
        }
        changeMusic(0);
    }

    /**
     * Changes music to previous song
     */
    private void previousSong() {
        if (currentSongIndex == 0) {
            changeMusic(musicList.length - 1);
            return;
        }
        changeMusic(currentSongIndex - 1);
    }

    /**
     * Change music to a random song
     */
    private void randomSong() {
        Random random = new Random();
        int randomSongIndex;
        while ((randomSongIndex = random.nextInt(musicList.length)) == currentSongIndex) {
        }
        changeMusic(randomSongIndex);
    }

    /**
     * Handles keyboard input to change music
     */
    private void addChangeMusicListener() {
        keyPressed.addListener((observable, oldValue, newValue) -> {
            if (isChangingMusic) return;
            if (newValue == null) return;
            switch (newValue.getCode()) {
                case NUMPAD4 -> previousSong();
                case NUMPAD5 -> randomSong();
                case NUMPAD6 -> nextSong();
            }
        });
    }

    /**
     * Configure connection data to establish connection to server
     */
    private void requestConnectionData() {
        TextInputDialog ipDialog = new TextInputDialog("127.0.0.1");
        ipDialog.setTitle("Connection Details");
        ipDialog.setContentText("IP: ");
        Optional<String> host;
        do {
            host = ipDialog.showAndWait();
        } while (host.isEmpty());
        this.host = host.get();
        TextInputDialog portDialog = new TextInputDialog("8765");
        portDialog.setTitle("Connection Details");
        portDialog.setContentText("Port: ");
        Optional<String> port;
        do {
            port = portDialog.showAndWait();
        } while (port.isEmpty());
        this.port = port.get();
        TextInputDialog usernameDialog = new TextInputDialog("Player");
        usernameDialog.setTitle("Connection Details");
        usernameDialog.setContentText("Username(0-8 Zeichen): ");
        Optional<String> username;
        do {
            username = usernameDialog.showAndWait();
        } while (username.isEmpty());
        this.username = username.get();
    }
}

