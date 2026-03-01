package net.neoforged.neoforge.common.util;

import java.util.List;

/**
 * Stub: Utility for printing formatted tables.
 */
public class TablePrinter {
    private TablePrinter() {}

    public static String toPrettyTable(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        // Minimal stub implementation
        if (headers != null) sb.append(String.join(" | ", headers)).append('\n');
        if (rows != null) {
            for (List<String> row : rows) {
                sb.append(String.join(" | ", row)).append('\n');
            }
        }
        return sb.toString();
    }
}
