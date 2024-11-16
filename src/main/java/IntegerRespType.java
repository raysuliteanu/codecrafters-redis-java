public class IntegerRespType implements RespType {
    private final int value;

    protected IntegerRespType(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String serialize() {
        return ":" + value + RespType.CRLF;
    }
}
