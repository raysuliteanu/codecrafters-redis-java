public enum ProtocolType {
    Array('*'),
    Boolean('#'),
    Integer(':'),
    Double(','),
    SimpleString('+'),
    SimpleError('-'),
    BulkString('$');

    private final char discriminator;

    ProtocolType(final char c) {
        this.discriminator = c;
    }


    @Override
    public String toString() {
        return String.valueOf(discriminator);
    }
}
