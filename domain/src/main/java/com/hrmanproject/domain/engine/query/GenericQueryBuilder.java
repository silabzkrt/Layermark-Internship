package com.hrmanproject.domain.engine.query;

import com.hrmanproject.domain.engine.validation.SqlIdentifierValidator;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericQueryBuilder {

    private static final Pattern PARAM_PATTERN = Pattern.compile("^([a-zA-Z0-9_]+)(_(eq|gt|lt|gte|lte|like))?$");

    public static class QueryResult {
        private final String sql;
        private final Object[] args;

        public QueryResult(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getArgs() {
            return args;
        }
    }

    /**
     * Builds a safe dynamic SQL search query with Keyset Pagination.
     */
    public static QueryResult buildSearchQuery(String tableName, Map<String, String> params, int limit) {
        SqlIdentifierValidator.validate(tableName);
        
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        List<Object> args = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String cursor = null;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if ("limit".equalsIgnoreCase(key)) {
                continue; // limit is passed as method arg or handled separately
            }
            if ("cursor".equalsIgnoreCase(key)) {
                cursor = value;
                continue;
            }

            Matcher matcher = PARAM_PATTERN.matcher(key);
            if (matcher.matches()) {
                String column = matcher.group(1);
                String operatorSuffix = matcher.group(3);
                
                SqlIdentifierValidator.validate(column);

                if (operatorSuffix == null || operatorSuffix.equalsIgnoreCase("eq")) {
                    conditions.add(column + " = ?");
                    args.add(value);
                } else if (operatorSuffix.equalsIgnoreCase("gt")) {
                    conditions.add(column + " > ?");
                    args.add(parseNumber(value));
                } else if (operatorSuffix.equalsIgnoreCase("lt")) {
                    conditions.add(column + " < ?");
                    args.add(parseNumber(value));
                } else if (operatorSuffix.equalsIgnoreCase("gte")) {
                    conditions.add(column + " >= ?");
                    args.add(parseNumber(value));
                } else if (operatorSuffix.equalsIgnoreCase("lte")) {
                    conditions.add(column + " <= ?");
                    args.add(parseNumber(value));
                } else if (operatorSuffix.equalsIgnoreCase("like")) {
                    conditions.add(column + " LIKE ?");
                    args.add("%" + value + "%");
                }
            }
        }

        // Keyset Pagination logic (Assuming primary key is 'id' and we sort by id DESC)
        if (StringUtils.hasText(cursor)) {
            conditions.add("id < ?"); // Fetch items older than cursor
            args.add(Long.parseLong(cursor));
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Order by ID DESC for keyset pagination consistency
        sql.append(" ORDER BY id DESC ");

        if (limit > 0) {
            sql.append(" LIMIT ?");
            args.add(limit);
        }

        return new QueryResult(sql.toString(), args.toArray());
    }

    private static Object parseNumber(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                return value; // fallback to string if parsing fails
            }
        }
    }
}
