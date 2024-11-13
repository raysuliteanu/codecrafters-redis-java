/*
Grammar

Expression   := Type
Type         := Array | SimpleString | BulkString | ...
Array        := '*' Len CRLF Expression
SimpleString := '+' ASCII CRLF
BulkString   := '$' Len CRLF ASCII(Len) CRLF
CRLF         := '\r' \n'
 */

public class RespParser {
    /*

            char c;
            while ((c = (char) reader.read()) > 0) {
                var respType = switch (c) {
                    case '*' -> {
                        var cnt = Integer.parseInt(String.valueOf((char) reader.read()));
                        expectEol(reader);
                        var size = 0;
                        while (size < cnt) {
                            var len = Integer.parseInt(String.valueOf((char) reader.read()));
                            expectEol(reader);
                            char[] buf = new char[len];
                            var read = reader.read(buf);
                            assert read == len;

                            ++size;
                        }
                    }
                    case '+' -> null;
                    case '$' -> null;
                    default -> throw new IllegalStateException("Unexpected value: " + c);
                };
            }

     */
}
