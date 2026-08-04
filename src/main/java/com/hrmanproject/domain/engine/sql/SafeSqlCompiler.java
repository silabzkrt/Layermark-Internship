package com.hrmanproject.domain.engine.sql;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SafeSqlCompiler {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    public String buildSelectById(String tableName, String idColumn) {
        validateIdentifier(tableName);
        validateIdentifier(idColumn);
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
    }

    public String buildSelectAll(String tableName) {
        validateIdentifier(tableName);
        return String.format("SELECT * FROM %s", tableName);
    }

    public String buildSelectByColumn(String tableName, String column) {
        validateIdentifier(tableName);
        validateIdentifier(column);
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, column);
    }

    public String buildExistsByColumn(String tableName, String column) {
        validateIdentifier(tableName);
        validateIdentifier(column);
        return String.format("SELECT COUNT(1) FROM %s WHERE %s = ?", tableName, column);
    }

    public String buildInsert(String tableName, List<String> columns) {
        validateIdentifier(tableName);
        columns.forEach(this::validateIdentifier);

        String cols = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, cols, placeholders);
    }

    public String buildUpdateById(String tableName, String idColumn, List<String> updateColumns) {
        validateIdentifier(tableName);
        validateIdentifier(idColumn);
        updateColumns.forEach(this::validateIdentifier);

        String setClause = updateColumns.stream()
                .map(c -> c + " = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, idColumn);
    }

    public String buildDeleteById(String tableName, String idColumn) {
        validateIdentifier(tableName);
        validateIdentifier(idColumn);
        return String.format("DELETE FROM %s WHERE %s = ?", tableName, idColumn);
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }
}
