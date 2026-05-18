package ScanHub.BE.enums;

public enum LogAction {
    ALL(""),
    CREATE("created"),
    UPDATE("updated"),
    DELETE("deleted"),
    EXPORT("exported"),
    LOGIN("logged in"),
    ERROR("ERROR: ");

    private final String verb;

    LogAction(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }
}