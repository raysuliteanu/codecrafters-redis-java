public enum Command {
    Ping("PING"),
    Echo("ECHO");

    private final String name;

    Command(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}