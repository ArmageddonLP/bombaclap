import adapters.LoginActionTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import proxy.LoginAction;
import views.BlockView;
import views.BorderView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Client class which handles all networking.
 */
public class Client extends Thread {
    private final ArrayList<BlockView> blockViews;
    private final BorderView borderView;
    private final SimpleObjectProperty<KeyEvent> keyPressed;
    private final String host;
    private final String port;
    private final String username;
    private final StackPane timer;
    private int playerId = -1;

    public Client(ArrayList<BlockView> blockViews, BorderView borderView, SimpleObjectProperty<KeyEvent> keyPressed, String host, String port, String username, StackPane timer) {
        this.blockViews = blockViews;
        this.borderView = borderView;
        this.keyPressed = keyPressed;
        this.host = host;
        this.port = port;
        this.username = username;
        this.timer = timer;
    }

    /**
     * Sets up input and output handling for the client.
     */
    public void run() {
        try {
            while (true) {
                try (Socket socket = new Socket(host, Integer.parseInt(port));
                     BufferedReader serverIn = new BufferedReader(
                             new InputStreamReader(socket.getInputStream()));
                     PrintWriter serverOut = new PrintWriter(
                             socket.getOutputStream(), true)) {

                    setupConnection(serverIn, serverOut);

                    ClientIn clientIn = new ClientIn(serverIn, blockViews, borderView, playerId, timer);
                    clientIn.setDaemon(true);
                    clientIn.start();

                    HandlePlayerAction handlePlayerAction = new HandlePlayerAction(serverOut, playerId, keyPressed);
                    handlePlayerAction.setDaemon(true);
                    handlePlayerAction.start();

                    // Endless loop to keep garbage collection from collecting the classes
                    while (true) {
                    }

                } catch (Exception ignored) {
                    throw new Exception();
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Handshake when establishing connection with the server. Client sends LoginAction with the player name
     * and then retrieves the LoginAction from the server with the correct player id.
     *
     * @param serverIn
     * @param serverOut
     */
    private void setupConnection(BufferedReader serverIn, PrintWriter serverOut) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LoginAction.class, new LoginActionTypeAdapter());
        builder.setPrettyPrinting();
        Gson gson = builder.create();


        String name = this.username;
        LoginAction loginRequest = new LoginAction(name, playerId);
        serverOut.println(gson.toJson(loginRequest).replaceAll("\r*\n*", ""));
        boolean connectionAccepted = false;
        while (!connectionAccepted) {
            try {
                String input;
                if ((input = serverIn.readLine()) != null) {
                    LoginAction loginResponse = gson.fromJson(input, LoginAction.class);
                    if (loginResponse.getPlayerName().equals(name)) {
                        playerId = loginResponse.getPlayerId();
                        connectionAccepted = true;
                        System.out.println("Connection accepted.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Didn't get a login response: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Failed to log in to the server: " + e.getMessage());
            }
        }

    }
}
