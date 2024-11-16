import java.util.Objects;

public class BulkStringRespType implements RespType {
    private final String value;

    protected BulkStringRespType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value != null ? value : "";
    }

    @Override
    public String serialize() {
        String formatted = "$" + (value != null ? value.length() : -1) + RespType.CRLF;

        if (value != null) {
            formatted += value + RespType.CRLF;
        }

        return formatted;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        final BulkStringRespType that = (BulkStringRespType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
