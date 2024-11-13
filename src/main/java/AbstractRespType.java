import java.util.Objects;

public abstract class AbstractRespType implements RespType {
    private final String value;

    protected AbstractRespType(final String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractRespType that = (AbstractRespType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public String toStringSimple() {
        return value;
    }

    @Override
    public String toString() {
        return toStringSimple() + RespType.CRLF;
    }
}
