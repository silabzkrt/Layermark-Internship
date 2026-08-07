package com.hrmanproject.domain.engine.relation;

import com.hrmanproject.domain.engine.ddl.DynamicDdlEngine;
import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.validation.SqlIdentifierValidator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class RelationshipManagerService {

    private final DynamicDdlEngine ddlEngine;
    private final MetadataCatalogService catalogService;
    private final JdbcTemplate jdbcTemplate;

    public RelationshipManagerService(DynamicDdlEngine ddlEngine, MetadataCatalogService catalogService, JdbcTemplate jdbcTemplate) {
        this.ddlEngine = ddlEngine;
        this.catalogService = catalogService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void createManyToOneRelation(String sourceTable, String targetTable, String foreignKeyColumn) {
        SqlIdentifierValidator.validate(sourceTable);
        SqlIdentifierValidator.validate(targetTable);
        SqlIdentifierValidator.validate(foreignKeyColumn);

        TableMetadata targetMeta = catalogService.getTableMetadata(targetTable)
                .orElseThrow(() -> new IllegalArgumentException("Target table not found: " + targetTable));
        
        ColumnMetadata targetPk = getPrimaryKeyColumn(targetMeta);

        // 1. Add column to source table matching target PK type
        ColumnMetadata fkCol = ColumnMetadata.builder()
                .name(foreignKeyColumn)
                .dataType(targetPk.getDataType()) // Constraint: exact type match
                .nullable(true) // FKs usually can be null unless specified otherwise
                .primaryKey(false)
                .build();
        
        ddlEngine.addColumn(sourceTable, fkCol);

        // 2. Add Foreign Key constraint
        String constraintName = "fk_" + sourceTable + "_" + foreignKeyColumn;
        SqlIdentifierValidator.validate(constraintName);

        String fkSql = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE SET NULL",
                sourceTable, constraintName, foreignKeyColumn, targetTable, targetPk.getName());
        
        jdbcTemplate.execute(fkSql);
    }

    @Transactional
    public void createManyToManyRelation(String table1, String table2, String junctionTableName) {
        SqlIdentifierValidator.validate(table1);
        SqlIdentifierValidator.validate(table2);
        SqlIdentifierValidator.validate(junctionTableName);

        TableMetadata meta1 = catalogService.getTableMetadata(table1)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + table1));
        TableMetadata meta2 = catalogService.getTableMetadata(table2)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + table2));

        ColumnMetadata pk1 = getPrimaryKeyColumn(meta1);
        ColumnMetadata pk2 = getPrimaryKeyColumn(meta2);

        String fkCol1 = table1 + "_id";
        String fkCol2 = table2 + "_id";

        // Create junction table
        TableMetadata junctionMeta = TableMetadata.builder()
                .tableName(junctionTableName)
                .description("Junction table between " + table1 + " and " + table2)
                .columns(Arrays.asList(
                        ColumnMetadata.builder().name("id").dataType("BIGSERIAL").primaryKey(true).nullable(false).build(),
                        ColumnMetadata.builder().name(fkCol1).dataType(pk1.getDataType()).nullable(false).build(),
                        ColumnMetadata.builder().name(fkCol2).dataType(pk2.getDataType()).nullable(false).build()
                ))
                .build();

        ddlEngine.createTableIfNotExists(junctionMeta);

        // Add FK constraints
        String constraint1 = "fk_" + junctionTableName + "_" + fkCol1;
        String constraint2 = "fk_" + junctionTableName + "_" + fkCol2;

        String fkSql1 = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE",
                junctionTableName, constraint1, fkCol1, table1, pk1.getName());
        String fkSql2 = String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE",
                junctionTableName, constraint2, fkCol2, table2, pk2.getName());

        jdbcTemplate.execute(fkSql1);
        jdbcTemplate.execute(fkSql2);

        // Add unique constraint to prevent duplicate links
        String uniqueConstraint = "uk_" + junctionTableName + "_link";
        String uniqueSql = String.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s, %s)",
                junctionTableName, uniqueConstraint, fkCol1, fkCol2);
        jdbcTemplate.execute(uniqueSql);
    }

    private ColumnMetadata getPrimaryKeyColumn(TableMetadata tableMetadata) {
        return tableMetadata.getColumns().stream()
                .filter(ColumnMetadata::isPrimaryKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No primary key found for table: " + tableMetadata.getTableName()));
    }
}
