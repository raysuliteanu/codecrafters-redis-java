import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MainTest {
    @Test
    void parseArray() throws IOException {
        String input =  "*3";
        BufferedReader reader = new BufferedReader(new StringReader("$3\r\nSET\r\n$10\r\nstrawberry\r\n$5\r\napple\r\n"));
        final var main = new Main();
        final var respType = main.handleArrayRespType(input, reader);
        assertInstanceOf(ArrayRespType.class, respType);
        var actual = (ArrayRespType) respType;
        final var contents = actual.getContents();
        assertEquals(3, contents.size());
        var cmd = contents.getFirst();
        assertInstanceOf(BulkStringRespType.class, cmd);
        assertEquals("SET", cmd.toString());
        var key = contents.get(1);
        assertInstanceOf(BulkStringRespType.class, key);
        assertEquals("strawberry", key.toString());
        var val = contents.get(2);
        assertInstanceOf(BulkStringRespType.class, val);
        assertEquals("apple", val.toString());
    }

    @Test
    void setWithOptions() {
        final var main = new Main();
        final List<RespType> contents = new ArrayList<>(5);
        contents.add(new BulkStringRespType("SET"));
        contents.add(new BulkStringRespType("aKey"));
        contents.add(new BulkStringRespType("aVal"));
        contents.add(new BulkStringRespType("px"));
        contents.add(new BulkStringRespType("100"));
        final var os = new ByteArrayOutputStream();
        main.handleSetCommand(contents, os);

        assertEquals("+OK\r\n", os.toString());
    }
}
