import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayRespType implements RespType {
    private final List<RespType> contents = new ArrayList<>();

    public ArrayRespType(final List<RespType> value) {
        contents.addAll(value);
    }

    public List<RespType> getContents() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public String serialize() {
        return "*"
                + contents.size()
                + CRLF
                + contents.stream().map(RespType::serialize).collect(Collectors.joining());
    }
}
