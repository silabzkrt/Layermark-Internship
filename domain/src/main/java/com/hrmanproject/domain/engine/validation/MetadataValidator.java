package com.hrmanproject.domain.engine.validation;

import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MetadataValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    private final MetadataCatalogService catalogService;
    private final JdbcTemplate jdbcTemplate;
    private final SafeSqlCompiler sqlCompiler;

    public MetadataValidator(MetadataCatalogService catalogService,
                             JdbcTemplate jdbcTemplate,
                             SafeSqlCompiler sqlCompiler) {
        this.catalogService = catalogService;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlCompiler = sqlCompiler;
    }

    public void validate(String tableName, Map<String, Object> recordData) {
        TableMetadata tableMetadata = catalogService.getTableMetadata(tableName)
                .orElseThrow(() -> new IllegalArgumentException("Table metadata not registered: " + tableName));

        Map<String, String> errors = new HashMap<>();

        for (ColumnMetadata col : tableMetadata.getColumns()) {
            String colName = col.getName();
            Object val = recordData != null ? recordData.get(colName) : null;

            // 1. Nullable check
            if (!col.isNullable() && !col.isPrimaryKey() && col.getDefaultValue() == null) {
                if (val == null || (val instanceof String && ((String) val).trim().isEmpty())) {
                    errors.put(colName, "This field is required and cannot be null");
                    continue;
                }
            }

            // 2. Regex / Validation Type check
            if (val instanceof String && !((String) val).trim().isEmpty()) {
                String strVal = ((String) val).trim();

                if ("EMAIL".equalsIgnoreCase(col.getValidationType())) {
                    if (!EMAIL_PATTERN.matcher(strVal).matches()) {
                        errors.put(colName, "Invalid EMAIL format");
                    }
                } else if ("PHONE".equalsIgnoreCase(col.getValidationType())) {
                    if (!PHONE_PATTERN.matcher(strVal).matches()) {
                        errors.put(colName, "Invalid PHONE format");
                    }
                } else if (col.getRegexPattern() != null && !col.getRegexPattern().trim().isEmpty()) {
                    if (!Pattern.compile(col.getRegexPattern()).matcher(strVal).matches()) {
                        errors.put(colName, "Value does not match required pattern: " + col.getRegexPattern());
                    }
                }
            }

            // 3. Unique check (if value is present and no error on it yet)
            if (col.isUnique() && !col.isPrimaryKey() && val != null && !errors.containsKey(colName)) {
                try {
                    String sql = sqlCompiler.buildExistsByColumn(tableName, colName);
                    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, val);
                    if (count != null && count > 0) {
                        errors.put(colName, "Value already exists in database: " + val);
                    }
                } catch (Exception e) {
                    // Table might not physically exist yet or query failed; skip runtime unique check
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new MetadataValidationException("Boundary validation failed for table: " + tableName, errors);
        }
    }
}
