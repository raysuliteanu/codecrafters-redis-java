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
    record TtlValue(RespType value, long ttl) {
        public TtlValue(final RespType value) {
            this(value, -1);
        }

        boolean notExpired() {
            return ttl == -1 || System.currentTimeMillis() <= ttl;
        }
    }

    final Map<RespType, TtlValue> db = new HashMap<>();

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
                                final var commandName = b.toString();
                                switch (commandName) {
                                    case "PING" -> handleResponse(new SimpleStringRespType("PONG"), outputStream);
                                    case "ECHO" -> handleResponse(contents.get(1), outputStream);
                                    case "SET" -> handleSetCommand(contents, outputStream);
                                    case "GET" -> handleGetCommand(contents, outputStream);
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

    void handleSetCommand(final List<RespType> contents, final OutputStream outputStream) {
        assert contents.size() >= 3 : "too few args to SET";
        var key = contents.get(1);
        var respValue = contents.get(2);

        var ttl = -1L;
        var setOnlyNotExist = false;
        var setOnlyExists = false;
        var getValue = false;
        var keyTtl = false;
        if (contents.size() > 3) {
            for (int i = 3; i < contents.size(); ) {
                var opt = contents.get(i++).toString().toUpperCase();
                switch (opt) {
                    // TODO: the PXAT and EXAT are different from PX and EX but for now just handle the same
                    // See https://redis.io/docs/latest/commands/set/
                    case "PX", "PXAT" -> ttl = System.currentTimeMillis() + Long.parseLong(contents.get(i++).toString());
                    case "EX", "EXAT" -> ttl = System.currentTimeMillis() + Long.parseLong(contents.get(i++).toString()) * 1000;
                    case "XX" -> setOnlyExists = true;
                    case "NX" -> setOnlyNotExist = true;
                    case "KEEPTTL" -> keyTtl = true;
                    case "GET" -> getValue = true;
                    default ->
                            throw new IllegalStateException("Unexpected value: " + opt);
                }
            }
        }

        final var curVal = saveValue(key, respValue, ttl, setOnlyExists, setOnlyNotExist);

        RespType resp;
        if (curVal != null) {
            resp = getRespType(getValue, curVal.value());
        }
        else {
            resp = getRespType(getValue, null);
        }

        handleResponse(resp, outputStream);
    }

    TtlValue saveValue(final RespType key, final RespType value, final long ttl, final boolean setOnlyExists, final boolean setOnlyNotExist) {
        var ttlValue = new TtlValue(value, ttl);

        final var curVal = db.get(key);

        if (curVal != null && setOnlyExists) {
            db.put(key, ttlValue);
        }
        else if (curVal == null && setOnlyNotExist) {
            db.put(key, ttlValue);
        }
        else {
            db.put(key, ttlValue);
        }

        return curVal;
    }

    void handleGetCommand(final List<RespType> contents, final OutputStream outputStream) {
        var key = contents.get(1);
        var ttlValue = db.get(key);
        if (ttlValue != null) {
            if (ttlValue.notExpired()) {
                handleResponse(ttlValue.value, outputStream);
            }
            else {
                handleResponse(new BulkStringRespType(null), outputStream);
            }
        }
        else {
            var err = new SimpleErrorRespType("MISSING", "no value for " + key);
            handleResponse(err, outputStream);
        }
    }

    private static RespType getRespType(final boolean getValue, final RespType curVal) {
        RespType resp;
        if (getValue) {
            if (curVal instanceof SimpleStringRespType) {
                resp = new BulkStringRespType(curVal.toString());
            }
            else {
                resp = new SimpleErrorRespType("WRONGTYPE", "Operation against a key holding the wrong kind of value");
            }
        }
        else {
            resp = new SimpleStringRespType("OK");
        }

        return resp;
    }

    RespType handleArrayRespType(String input, final BufferedReader reader) throws IOException {
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
            else if (input.startsWith(":")) {
                var respType = handleIntegerRespType(input);
                contents.add(respType);
            }
        }

        return new ArrayRespType(contents);
    }

    private static RespType handleIntegerRespType(final String input) {
        final var i = Integer.parseInt(input.substring(1));
        return null;
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
            outputStream.write(Objects.requireNonNull(respType.serialize()).getBytes());
            outputStream.flush();
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}
