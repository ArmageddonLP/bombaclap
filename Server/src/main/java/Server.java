import com.google.gson.Gson;
import config.Constants;
import game.BombermanServer;
import game.GameLogic;
import game.GameState;
import proxy.Action;
import proxy.LoginAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Server-Thread used for each established connection between client and server
 */
public class Server extends Thread {
    private static GameState gameState;
    private final Socket client;
    private BombermanServer bombermanServer;
    private int playerId;
    private String playerName;
    private boolean connected;

    /**
     * Creates a new Server-Thread for the given client
     *
     * @param socket socket of connected client
     */
    public Server(final Socket socket) {
        client = socket;
        bombermanServer = BombermanServer.getInstance();
    }

    /**
     * Starts the bombermanServer and begins to periodically look for new clients
     * attempting to connect to the serverSocket with the predefined port.
     *
     * @param args not used
     */
    public static void main(final String[] args) {
        int port = Constants.PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        BombermanServer.getInstance();
        System.out.println("Server up! Port: " + port);
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                Server connection = new Server(client);
                connection.setDaemon(true);
                connection.start();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Server-Thread run-function executed after Server-Thread has been started.
     * Runs periodically as long as the client doesn't close their connection.
     */
    @Override
    public void run() {
        while (!this.client.isClosed()) {
            try (BufferedReader in =
                         new BufferedReader(
                                 new InputStreamReader(client.getInputStream()));
                 PrintWriter out =
                         new PrintWriter(
                                 this.client.getOutputStream(), true)) {

                while (!connected) {
                    connected = setupConnection(in, out);
                }
                gameState = GameState.getInstance();
                receiveActionsFromClientLoop(in);
                sendMapToClientLoop(out);
            } catch (IOException | InterruptedException e) {
                connected = false;
                bombermanServer.removeActiveConnection(playerId);
                String serverLogMessage = "Players " + bombermanServer.getActiveConnections() + "/" + Constants.MAX_PLAYER_COUNT +
                        " Names " + Arrays.toString(bombermanServer.getConnectedPlayers());
                System.out.println(serverLogMessage);
            }
        }
    }

    /**
     * Attempts to setup a connection between client and server after the socket has been accepted
     *
     * @param in  server-side BufferedReader used for client input
     * @param out server-side PrintWriter used for client output
     * @return true if connection has been established; false if connection limit has been reached
     */
    private boolean setupConnection(BufferedReader in, PrintWriter out) {
        Gson gson = new Gson();
        boolean connectionAccepted = false;
        while (!connectionAccepted) {
            try {
                String input;
                if ((input = in.readLine()) != null) {
                    LoginAction loginAction = gson.fromJson(input, LoginAction.class);
                    bombermanServer = BombermanServer.getInstance();
                    playerName = loginAction.getPlayerName();
                    if (playerName.length() > 9) {
                        playerName = playerName.substring(0, 8);
                    } else if (playerName.length() == 0) {
                        playerName = Constants.DEFAULT_PLAYER_NAME;
                    }
                    playerId = bombermanServer.addNewConnection(playerName);
                    if (playerId != -1) {
                        loginAction.setPlayerId(playerId);
                        String serverLogMessage = "Players " + bombermanServer.getActiveConnections() + "/" + Constants.MAX_PLAYER_COUNT +
                                " Names " + Arrays.toString(bombermanServer.getConnectedPlayers());
                        System.out.println(serverLogMessage);
                        out.println(gson.toJson(loginAction));
                        connectionAccepted = true;
                    } else {
                        throw new Exception("Player limit reached");
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection refused! " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Periodically checks for new client input and passes parsable actions to gameLogic
     *
     * @param in server-side BufferedReader used for client input
     */
    private void receiveActionsFromClientLoop(BufferedReader in) {
        Runnable inputRunnable = () -> {
            Gson gsonInput = new Gson();
            GameLogic gameLogic = GameLogic.getInstance();
            while (connected) {
                try {
                    String input;
                    if ((input = in.readLine()) != null) {
                        Action action = gsonInput.fromJson(input, Action.class);
                        if (action != null && playerId < Constants.MAX_PLAYER_COUNT) {
                            action.setPlayerId(playerId);
                            if (bombermanServer.isGameRunning()) {
                                gameLogic.addPlayerAction(action);
                            }
                        }
                    }
                } catch (Exception e) {
                    connected = false;
                    bombermanServer.removeActiveConnection(playerId);
                    String serverLogMessage = "Players " + bombermanServer.getActiveConnections() + "/" + Constants.MAX_PLAYER_COUNT +
                            " Names " + Arrays.toString(bombermanServer.getConnectedPlayers());
                    System.out.println(serverLogMessage);
                }
            }
        };
        Thread thread = new Thread(inputRunnable);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Periodically sends the current map to client output
     *
     * @param out server-side PrintWriter used for client output
     * @throws InterruptedException thrown if client disconnects
     */
    private void sendMapToClientLoop(PrintWriter out) throws InterruptedException {
        Gson gson = new Gson();
        while (connected) {
            if (bombermanServer.getCountdownStarted()) {
                out.println(bombermanServer.getCountdown());
            }
            String json = gson.toJson(gameState.retrieveCurrentMap());
            out.println(json);
            Thread.sleep(Constants.SERVER_TICK_DURATION);
        }
    }
}
