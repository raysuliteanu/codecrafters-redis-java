import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
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

    private static void handleConnection(final Socket clientSocket) {
        try (var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            final var outputStream = clientSocket.getOutputStream();
            String input;
            while ((input = reader.readLine()) != null) {
                var totalLineCount = 0;
                if (input.startsWith("*")) {
                    totalLineCount = Integer.parseInt(input.substring(1));
                }

                input = reader.readLine();
                if (input.startsWith("$")) {
                    var len = Integer.parseInt(input.substring(1));
                    input = reader.readLine();
                    assert input.length() == len;
                    if (input.equals("PING")) {
                        handleResponse("PONG", ResponseType.SimpleString, outputStream);
                    }
                    else if (input.equals("ECHO")) {
                        input = reader.readLine();
                        len = Integer.parseInt(input.substring(1));
                        input = reader.readLine();
                        assert input.length() == len;
                        handleResponse(input, ResponseType.BulkString, outputStream);
                    }
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

    private static void handleResponse(final String response, final ResponseType responseType, final OutputStream outputStream) {
        try {
            String s = switch (responseType) {
                case SimpleString -> formatSimpleString(response);
                case BulkString -> formatBulkString(response);
            };

            outputStream.write(s.getBytes());
            outputStream.flush();
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    private static String formatSimpleString(final String response) {
        return "+" + response + "\r\n";
    }

    private static String formatBulkString(final String response) {
        var len = response.length();
        return "$" + len + "\r\n" + response + "\r\n";
    }
}
