package com.hrmanproject.domain.engine.schema;

import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JsonSchemaGenerator {

    public Map<String, Object> generateJsonSchema(TableMetadata tableMetadata) {
        if (tableMetadata == null) {
            throw new IllegalArgumentException("Table metadata cannot be null");
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("title", tableMetadata.getTableName());
        if (tableMetadata.getDescription() != null) {
            schema.put("description", tableMetadata.getDescription());
        }
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ColumnMetadata col : tableMetadata.getColumns()) {
            Map<String, Object> prop = new LinkedHashMap<>();
            String jsonType = mapSqlToJsonType(col.getDataType());
            prop.put("type", jsonType);

            if ("EMAIL".equalsIgnoreCase(col.getValidationType())) {
                prop.put("format", "email");
            } else if ("PHONE".equalsIgnoreCase(col.getValidationType())) {
                prop.put("pattern", "^\\+?[0-9]{10,15}$");
            } else if (col.getRegexPattern() != null && !col.getRegexPattern().trim().isEmpty()) {
                prop.put("pattern", col.getRegexPattern());
            }

            if (col.getDefaultValue() != null) {
                prop.put("default", col.getDefaultValue());
            }

            properties.put(col.getName(), prop);

            if (!col.isNullable() && !col.isPrimaryKey() && col.getDefaultValue() == null) {
                required.add(col.getName());
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }

        return schema;
    }

    private String mapSqlToJsonType(String sqlDataType) {
        if (sqlDataType == null) return "string";
        String upper = sqlDataType.toUpperCase();
        if (upper.contains("INT") || upper.contains("SERIAL")) {
            return "integer";
        }
        if (upper.contains("DOUBLE") || upper.contains("FLOAT") || upper.contains("NUMERIC") || upper.contains("DECIMAL")) {
            return "number";
        }
        if (upper.contains("BOOL")) {
            return "boolean";
        }
        return "string";
    }
}
