package models.parameters;

public enum PreprocessType {
    ALL("all"),
    FIRST("first");

    private final String name;

    PreprocessType(String val) {
        name = val;
    }

    public boolean equals(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
