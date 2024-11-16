import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayRespTypeTest {

    @Test
    void testToString() {
        var arrayType = new ArrayRespType(List.of(new SimpleStringRespType("hello"), new BulkStringRespType("world")));
        assertEquals("*2\r\n+hello\r\n$5\r\nworld\r\n", arrayType.serialize());
    }
}
