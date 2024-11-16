import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulkStringRespTypeTest {
    @Test
    void nonNull() {
        var val = new BulkStringRespType("aValue");
        assertEquals("aValue", val.toString());
        assertEquals("$6\r\naValue\r\n", val.serialize());
    }

    @Test
    void nullValue() {
        var val = new BulkStringRespType(null);
        assertEquals("", val.toString());
        assertEquals("$-1\r\n", val.serialize());
    }

}
