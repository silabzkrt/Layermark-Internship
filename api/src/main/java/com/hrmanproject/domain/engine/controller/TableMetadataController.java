package com.hrmanproject.domain.engine.controller;

import com.hrmanproject.domain.engine.ddl.DynamicDdlEngine;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.schema.JsonSchemaGenerator;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import com.hrmanproject.domain.engine.validation.MetadataValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tables")
public class TableMetadataController {

    private final DynamicDdlEngine ddlEngine;
    private final MetadataCatalogService catalogService;
    private final JsonSchemaGenerator jsonSchemaGenerator;
    private final MetadataValidator metadataValidator;
    private final SafeSqlCompiler sqlCompiler;
    private final JdbcTemplate jdbcTemplate;

    public TableMetadataController(DynamicDdlEngine ddlEngine,
                                   MetadataCatalogService catalogService,
                                   JsonSchemaGenerator jsonSchemaGenerator,
                                   MetadataValidator metadataValidator,
                                   SafeSqlCompiler sqlCompiler,
                                   JdbcTemplate jdbcTemplate) {
        this.ddlEngine = ddlEngine;
        this.catalogService = catalogService;
        this.jsonSchemaGenerator = jsonSchemaGenerator;
        this.metadataValidator = metadataValidator;
        this.sqlCompiler = sqlCompiler;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/{name}")
    public ResponseEntity<TableMetadata> createTable(@PathVariable String name, @RequestBody TableMetadata tableMetadata) {
        tableMetadata.setTableName(name);
        ddlEngine.createTableIfNotExists(tableMetadata);
        return ResponseEntity.status(HttpStatus.CREATED).body(tableMetadata);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTable(@PathVariable String name) {
        ddlEngine.dropTable(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Collection<TableMetadata>> getAllTables() {
        return ResponseEntity.ok(catalogService.getAllTables());
    }

    @GetMapping("/{name}")
    public ResponseEntity<TableMetadata> getTableMetadata(@PathVariable String name) {
        Optional<TableMetadata> meta = catalogService.getTableMetadata(name);
        return meta.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{name}/schema")
    public ResponseEntity<Map<String, Object>> getJsonSchema(@PathVariable String name) {
        return catalogService.getTableMetadata(name)
                .map(jsonSchemaGenerator::generateJsonSchema)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{name}/records")
    public ResponseEntity<Map<String, Object>> insertRecord(@PathVariable String name, @RequestBody Map<String, Object> recordData) {
        // 1. Boundary validation
        metadataValidator.validate(name, recordData);

        // 2. Safe Dynamic DML execution
        List<String> columns = new ArrayList<>(recordData.keySet());
        String insertSql = sqlCompiler.buildInsert(name, columns);
        Object[] values = columns.stream().map(recordData::get).toArray();

        jdbcTemplate.update(insertSql, values);
        return ResponseEntity.status(HttpStatus.CREATED).body(recordData);
    }
}
