import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        Socket clientSocket = null;
        int port = 6379;
        try (var serverSocket = new ServerSocket(port)) {
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();

            try (var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String input;
                while ((input = reader.readLine()) != null) {
                    if (input.equals("PING")) {
                        clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
                        clientSocket.getOutputStream().flush();
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
}
