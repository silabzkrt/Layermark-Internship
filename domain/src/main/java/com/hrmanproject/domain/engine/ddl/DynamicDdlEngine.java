package com.hrmanproject.domain.engine.ddl;

import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.validation.SqlIdentifierValidator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class DynamicDdlEngine {

    private final JdbcTemplate jdbcTemplate;
    private final MetadataCatalogService catalogService;

    public DynamicDdlEngine(JdbcTemplate jdbcTemplate, MetadataCatalogService catalogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogService = catalogService;
    }

    public void createTableIfNotExists(TableMetadata tableMetadata) {
        SqlIdentifierValidator.validate(tableMetadata.getTableName());
        if (tableMetadata.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Cannot create table without columns: " + tableMetadata.getTableName());
        }

        String columnsSql = tableMetadata.getColumns().stream()
                .map(this::buildColumnDefinition)
                .collect(Collectors.joining(", "));

        String ddl = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableMetadata.getTableName(), columnsSql);
        jdbcTemplate.execute(ddl);

        catalogService.registerTable(tableMetadata);
    }

    public void addColumn(String tableName, ColumnMetadata column) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(column.getName());

        String ddl = String.format("ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s;", tableName, buildColumnDefinition(column));
        jdbcTemplate.execute(ddl);
    }

    public void dropTable(String tableName) {
        SqlIdentifierValidator.validate(tableName);
        String ddl = String.format("DROP TABLE IF EXISTS %s CASCADE;", tableName);
        jdbcTemplate.execute(ddl);
        catalogService.deleteTableMetadata(tableName);
    }

    private String buildColumnDefinition(ColumnMetadata col) {
        SqlIdentifierValidator.validate(col.getName());
        StringBuilder sb = new StringBuilder();
        sb.append(col.getName()).append(" ").append(col.getDataType());
        if (col.isPrimaryKey()) {
            sb.append(" PRIMARY KEY");
        } else if (!col.isNullable()) {
            sb.append(" NOT NULL");
        }
        if (col.isUnique() && !col.isPrimaryKey()) {
            sb.append(" UNIQUE");
        }
        if (col.getDefaultValue() != null) {
            sb.append(" DEFAULT ").append(col.getDefaultValue());
        }
        return sb.toString();
    }

}
