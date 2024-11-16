import java.util.Objects;

public class SimpleStringRespType implements RespType {
    private final String value;

    protected SimpleStringRespType(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        final SimpleStringRespType that = (SimpleStringRespType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String serialize() {
        return "+" + value + RespType.CRLF;
    }
}
