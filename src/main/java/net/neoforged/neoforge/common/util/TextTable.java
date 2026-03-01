package net.neoforged.neoforge.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub: Text table builder.
 */
public class TextTable {
    private final List<Column> columns = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public TextTable(List<Column> columns) {
        this.columns.addAll(columns);
    }

    public void add(Object... data) {
        List<String> row = new ArrayList<>();
        for (Object d : data) {
            row.add(String.valueOf(d));
        }
        rows.add(row);
    }

    @Override
    public String toString() {
        return TablePrinter.toPrettyTable(
            columns.stream().map(c -> c.header).toList(),
            rows
        );
    }

    public static class Column {
        final String header;
        public Column(String header) {
            this.header = header;
        }
        public static Column of(String header) {
            return new Column(header);
        }
    }
}
