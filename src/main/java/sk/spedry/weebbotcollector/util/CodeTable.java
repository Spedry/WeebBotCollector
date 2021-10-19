package sk.spedry.weebbotcollector.util;

import lombok.Getter;

public class CodeTable {
    @Getter
    private final Integer id;
    @Getter
    private final String name;
    @Getter
    private String value;

    public CodeTable(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public CodeTable(int id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return getName();
    }
}
