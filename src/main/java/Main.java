import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {
    private final Map<RespType, RespType> db = new HashMap<>();

    public static void main(String[] args) {
        new Main().serve();
    }

    private void serve() {
        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                Socket clientSocket = serverSocket.accept();

                Thread.ofVirtual().start(() -> handleConnection(clientSocket));
            }
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    private void handleConnection(final Socket clientSocket) {
        try (var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            final var outputStream = clientSocket.getOutputStream();
            String input;
            while ((input = reader.readLine()) != null) {
                RespType respType = null;
                if (input.startsWith("*")) {
                    respType = handleArrayRespType(input, reader);
                }

                switch (respType) {
                    case ArrayRespType a -> {
                        final var contents = a.getContents();
                        var command = contents.getFirst();
                        switch (command) {
                            case BulkStringRespType b -> {
                                final var commandName = b.toStringSimple();
                                switch (commandName) {
                                    case "PING" -> handleResponse(new SimpleStringRespType("PONG"), outputStream);
                                    case "ECHO" -> handleResponse(contents.get(1), outputStream);
                                    case "SET" -> {
                                        var key = contents.get(1);
                                        var value = contents.get(2);
                                        db.put(key, value);
                                        handleResponse(new SimpleStringRespType("OK"), outputStream);
                                    }
                                    case "GET" -> {
                                        var key = contents.get(1);
                                        var value = db.get(key);
                                        handleResponse(value, outputStream);
                                    }
                                }
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + command);
                        }
                    }
                    case null, default -> throw new IllegalStateException("Unexpected value: " + respType);
                }
            }
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
            catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
            }
        }
    }

    private static RespType handleArrayRespType(String input, final BufferedReader reader) throws IOException {
        var totalLineCount = Integer.parseInt(input.substring(1));

        List<RespType> contents = new ArrayList<>();
        var count = 0;
        while (count++ < totalLineCount) {
            input = reader.readLine();
            if (input.startsWith("$")) {
                var respType = handleBulkStringRespType(input, reader);
                contents.add(respType);
            }
            else if (input.startsWith("+")) {
                var respType = handleSimpleStringRespType(input);
                contents.add(respType);
            }
            else if (input.startsWith("*")) {
                var respType = handleArrayRespType(input, reader);
                contents.add(respType);
            }
        }

        return new ArrayRespType(contents);
    }

    private static RespType handleSimpleStringRespType(final String input) {
        return new SimpleStringRespType(input.substring(1));
    }

    private static RespType handleBulkStringRespType(String input, final BufferedReader reader) throws IOException {
        var len = Integer.parseInt(input.substring(1));
        input = reader.readLine();
        assert input.length() == len;

        return new BulkStringRespType(input);
    }

    private static void handleResponse(final RespType respType, final OutputStream outputStream) {
        try {
            outputStream.write(Objects.requireNonNull(respType.toString()).getBytes());
            outputStream.flush();
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
