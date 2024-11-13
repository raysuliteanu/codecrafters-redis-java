public class SimpleStringRespType extends AbstractRespType {
    protected SimpleStringRespType(final String value) {
        super(value);
    }

    @Override
    public String toString() {
        return "+" + super.toString();
    }
}
