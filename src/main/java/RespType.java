public interface RespType {
    String CRLF = "\r\n";

    /** format as required for sending over the wire */
    String serialize();
}
