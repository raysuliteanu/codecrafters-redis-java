import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayRespType extends AbstractRespType {
    private final List<RespType> contents = new ArrayList<>();

    public ArrayRespType(final List<RespType> value) {
        super("");
        contents.addAll(value);
    }

    public List<RespType> getContents() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public String toString() {
        return "*"
                + contents.size()
                + CRLF
                + contents.stream().map(Object::toString).collect(Collectors.joining());
    }
}
