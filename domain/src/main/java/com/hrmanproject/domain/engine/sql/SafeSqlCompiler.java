package com.hrmanproject.domain.engine.sql;

import com.hrmanproject.domain.engine.validation.SqlIdentifierValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SafeSqlCompiler {

    public String buildSelectById(String tableName, String idColumn) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(idColumn);
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
    }

    public String buildSelectAll(String tableName) {
        SqlIdentifierValidator.validate(tableName);
        return String.format("SELECT * FROM %s", tableName);
    }

    public String buildSelectByColumn(String tableName, String column) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(column);
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, column);
    }

    public String buildExistsByColumn(String tableName, String column) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(column);
        return String.format("SELECT COUNT(1) FROM %s WHERE %s = ?", tableName, column);
    }

    public String buildInsert(String tableName, List<String> columns) {
        SqlIdentifierValidator.validate(tableName);
        columns.forEach(SqlIdentifierValidator::validate);

        String cols = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, cols, placeholders);
    }

    public String buildUpdateById(String tableName, String idColumn, List<String> updateColumns) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(idColumn);
        updateColumns.forEach(SqlIdentifierValidator::validate);

        String setClause = updateColumns.stream()
                .map(c -> c + " = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, idColumn);
    }

    public String buildDeleteById(String tableName, String idColumn) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(idColumn);
        return String.format("DELETE FROM %s WHERE %s = ?", tableName, idColumn);
    }

}
