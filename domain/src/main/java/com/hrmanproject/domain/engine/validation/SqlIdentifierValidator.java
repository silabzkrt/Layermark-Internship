package com.hrmanproject.domain.engine.validation;

import java.util.regex.Pattern;

public class SqlIdentifierValidator {

    // Allows only alphanumeric characters and underscores, starting with a letter or underscore.
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    // Reserved SQL keywords that shouldn't be used as table or column names
    private static final String[] RESERVED_KEYWORDS = {
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TABLE", 
            "FROM", "WHERE", "AND", "OR", "USER", "GROUP", "ORDER", "BY", "INDEX", "VIEW"
    };

    /**
     * Validates an SQL identifier (table name, column name) to prevent SQL injection.
     * @param identifier The string to validate.
     * @throws IllegalArgumentException if the identifier is invalid.
     */
    public static void validate(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty.");
        }

        String trimmed = identifier.trim();

        if (trimmed.length() > 63) { // PostgreSQL max identifier length is 63 bytes by default
            throw new IllegalArgumentException("Identifier cannot exceed 63 characters: " + trimmed);
        }

        if (!IDENTIFIER_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid identifier format. Only alphanumeric characters and underscores are allowed, starting with a letter or underscore: " + trimmed);
        }

        for (String keyword : RESERVED_KEYWORDS) {
            if (trimmed.equalsIgnoreCase(keyword)) {
                throw new IllegalArgumentException("Identifier cannot be a reserved SQL keyword: " + trimmed);
            }
        }
    }
}
