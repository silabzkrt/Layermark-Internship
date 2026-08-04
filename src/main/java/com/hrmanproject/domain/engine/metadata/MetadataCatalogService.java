package com.hrmanproject.domain.engine.metadata;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetadataCatalogService {

    private static final Logger log = LoggerFactory.getLogger(MetadataCatalogService.class);

    private final Map<String, TableMetadata> catalog = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public MetadataCatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void loadCatalogFromDatabase() {
        try {
            String tableSql = "SELECT table_name, description FROM sys_table_metadata";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(tableSql);

            for (Map<String, Object> tRow : tables) {
                String tableName = (String) tRow.get("table_name");
                String description = (String) tRow.get("description");

                String colSql = "SELECT column_name, data_type, is_primary_key, is_nullable, is_unique, default_value, regex_pattern, validation_type " +
                        "FROM sys_column_metadata WHERE table_name = ?";
                List<ColumnMetadata> columns = jdbcTemplate.query(colSql, (rs, rowNum) -> ColumnMetadata.builder()
                        .name(rs.getString("column_name"))
                        .dataType(rs.getString("data_type"))
                        .primaryKey(rs.getBoolean("is_primary_key"))
                        .nullable(rs.getBoolean("is_nullable"))
                        .unique(rs.getBoolean("is_unique"))
                        .defaultValue(rs.getString("default_value"))
                        .regexPattern(rs.getString("regex_pattern"))
                        .validationType(rs.getString("validation_type"))
                        .build(), tableName);

                TableMetadata tableMeta = TableMetadata.builder()
                        .tableName(tableName)
                        .description(description)
                        .columns(new ArrayList<>(columns))
                        .build();

                catalog.put(tableName.toLowerCase(), tableMeta);
            }
            log.info("Loaded {} tables from persistent metadata catalog.", catalog.size());
        } catch (Exception e) {
            log.warn("System metadata tables not available yet or empty during boot: {}", e.getMessage());
        }
    }

    public void registerTable(TableMetadata metadata) {
        if (metadata == null || metadata.getTableName() == null || metadata.getTableName().trim().isEmpty()) {
            throw new IllegalArgumentException("Table metadata and table name must not be empty");
        }
        catalog.put(metadata.getTableName().toLowerCase(), metadata);

        try {
            String insertTableSql = "INSERT INTO sys_table_metadata (table_name, description) VALUES (?, ?) " +
                    "ON CONFLICT (table_name) DO UPDATE SET description = EXCLUDED.description";
            jdbcTemplate.update(insertTableSql, metadata.getTableName(), metadata.getDescription());

            for (ColumnMetadata col : metadata.getColumns()) {
                String insertColSql = "INSERT INTO sys_column_metadata " +
                        "(table_name, column_name, data_type, is_primary_key, is_nullable, is_unique, default_value, regex_pattern, validation_type) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (table_name, column_name) DO UPDATE SET " +
                        "data_type = EXCLUDED.data_type, is_primary_key = EXCLUDED.is_primary_key, " +
                        "is_nullable = EXCLUDED.is_nullable, is_unique = EXCLUDED.is_unique, " +
                        "default_value = EXCLUDED.default_value, regex_pattern = EXCLUDED.regex_pattern, " +
                        "validation_type = EXCLUDED.validation_type";
                jdbcTemplate.update(insertColSql,
                        metadata.getTableName(),
                        col.getName(),
                        col.getDataType(),
                        col.isPrimaryKey(),
                        col.isNullable(),
                        col.isUnique(),
                        col.getDefaultValue(),
                        col.getRegexPattern(),
                        col.getValidationType()
                );
            }
        } catch (Exception e) {
            log.warn("Could not persist table metadata to database: {}", e.getMessage());
        }
    }

    public void deleteTableMetadata(String tableName) {
        if (tableName == null) return;
        catalog.remove(tableName.toLowerCase());
        try {
            jdbcTemplate.update("DELETE FROM sys_table_metadata WHERE table_name = ?", tableName);
        } catch (Exception e) {
            log.warn("Could not delete metadata from database: {}", e.getMessage());
        }
    }

    public Optional<TableMetadata> getTableMetadata(String tableName) {
        if (tableName == null) return Optional.empty();
        return Optional.ofNullable(catalog.get(tableName.toLowerCase()));
    }

    public Collection<TableMetadata> getAllTables() {
        return catalog.values();
    }

    public boolean isTableRegistered(String tableName) {
        if (tableName == null) return false;
        return catalog.containsKey(tableName.toLowerCase());
    }
}
