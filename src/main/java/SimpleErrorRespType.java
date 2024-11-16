public class SimpleErrorRespType implements RespType {
    private final String type;
    private final String value;

    protected SimpleErrorRespType(final String type, final String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + " " + value;
    }

    @Override
    public String serialize() {
        return "-" + this + RespType.CRLF;
    }
}
