package com.hrmanproject.domain.engine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrmanproject.domain.engine.ddl.DynamicDdlEngine;
import com.hrmanproject.domain.engine.metadata.ColumnMetadata;
import com.hrmanproject.domain.engine.metadata.MetadataCatalogService;
import com.hrmanproject.domain.engine.metadata.TableMetadata;
import com.hrmanproject.domain.engine.schema.JsonSchemaGenerator;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import com.hrmanproject.domain.engine.validation.MetadataValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TableMetadataController.class)
public class TableMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DynamicDdlEngine ddlEngine;

    @MockBean
    private MetadataCatalogService catalogService;

    @MockBean
    private JsonSchemaGenerator jsonSchemaGenerator;

    @MockBean
    private MetadataValidator metadataValidator;

    @MockBean
    private SafeSqlCompiler sqlCompiler;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateDynamicTable() throws Exception {
        TableMetadata meta = TableMetadata.builder()
                .tableName("sys_logs")
                .description("System Log Table")
                .columns(List.of(
                        ColumnMetadata.builder().name("id").dataType("BIGSERIAL").primaryKey(true).build(),
                        ColumnMetadata.builder().name("message").dataType("TEXT").nullable(false).build()
                ))
                .build();

        mockMvc.perform(post("/api/tables/sys_logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(meta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableName").value("sys_logs"))
                .andExpect(jsonPath("$.description").value("System Log Table"));

        verify(ddlEngine).createTableIfNotExists(any(TableMetadata.class));
    }

    @Test
    void shouldReturnJsonSchema() throws Exception {
        TableMetadata meta = TableMetadata.builder()
                .tableName("sys_logs")
                .description("System Log Table")
                .columns(List.of())
                .build();

        Map<String, Object> schemaMap = Map.of(
                "$schema", "http://json-schema.org/draft-07/schema#",
                "title", "sys_logs",
                "type", "object"
        );

        when(catalogService.getTableMetadata("sys_logs")).thenReturn(Optional.of(meta));
        when(jsonSchemaGenerator.generateJsonSchema(meta)).thenReturn(schemaMap);

        mockMvc.perform(get("/api/tables/sys_logs/schema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("sys_logs"))
                .andExpect(jsonPath("$.type").value("object"));
    }

    @Test
    void shouldDeleteTable() throws Exception {
        mockMvc.perform(delete("/api/tables/sys_logs"))
                .andExpect(status().isNoContent());

        verify(ddlEngine).dropTable("sys_logs");
    }
}
