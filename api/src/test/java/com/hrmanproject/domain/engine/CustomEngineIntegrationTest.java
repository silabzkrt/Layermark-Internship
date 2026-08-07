package com.hrmanproject.domain.engine;

import com.hrmanproject.common.AbstractPostgresIntegrationTest;
import com.hrmanproject.domain.engine.ddl.DynamicDdlEngine;
import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomEngineIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private DynamicDdlEngine ddlEngine;

    @Autowired
    private MetadataCatalogService catalogService;

    @Autowired
    private SafeSqlCompiler sqlCompiler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateDynamicTableAndExecuteSafeQueries() {
        TableMetadata customTable = TableMetadata.builder()
                .tableName("custom_assets")
                .columns(List.of(
                        ColumnMetadata.builder().name("id").dataType("BIGSERIAL").primaryKey(true).build(),
                        ColumnMetadata.builder().name("asset_code").dataType("VARCHAR(100)").nullable(false).unique(true).build(),
                        ColumnMetadata.builder().name("asset_value").dataType("DOUBLE PRECISION").nullable(false).defaultValue("0.0").build()
                ))
                .build();

        ddlEngine.createTableIfNotExists(customTable);

        assertThat(catalogService.isTableRegistered("custom_assets")).isTrue();

        String insertSql = sqlCompiler.buildInsert("custom_assets", List.of("asset_code", "asset_value"));
        jdbcTemplate.update(insertSql, "AST-101", 15000.50);

        String selectSql = sqlCompiler.buildSelectByColumn("custom_assets", "asset_code");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, "AST-101");

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("asset_code")).isEqualTo("AST-101");
        assertThat(((Number) rows.get(0).get("asset_value")).doubleValue()).isEqualTo(15000.50);
    }
}
