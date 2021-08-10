package csemodels.parameters;

public enum ComputationType {
    LOOP("loop"),
    HAIRPIN("hairpin"),
    HOMOLOGY("homology");

    private final String name;

    ComputationType(String val) {
        name = val;
    }

    public boolean equals(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
