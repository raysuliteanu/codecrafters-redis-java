public class BulkStringRespType extends AbstractRespType {
    protected BulkStringRespType(final String value) {
        super(value);
    }

    @Override
    public String toString() {
        var s = super.toString();
        var len = s.length() - RespType.CRLF.length();
        return "$" + len + RespType.CRLF + s;
    }
}
