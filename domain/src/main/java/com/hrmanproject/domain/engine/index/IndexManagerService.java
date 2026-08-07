package com.hrmanproject.domain.engine.index;

import com.hrmanproject.domain.engine.validation.SqlIdentifierValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndexManagerService {

    private static final Logger log = LoggerFactory.getLogger(IndexManagerService.class);
    private final JdbcTemplate jdbcTemplate;

    public IndexManagerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Creates a B-Tree index on a specific column of a dynamic table.
     */
    @Transactional
    public void createIndex(String tableName, String columnName) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(columnName);

        String indexName = generateIndexName(tableName, columnName);
        SqlIdentifierValidator.validate(indexName);

        // CREATE INDEX IF NOT EXISTS is standard in PostgreSQL
        String sql = String.format("CREATE INDEX IF NOT EXISTS %s ON %s (%s)", indexName, tableName, columnName);
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Created index {} on table {} column {}", indexName, tableName, columnName);
        } catch (Exception e) {
            log.error("Failed to create index on {}.{}: {}", tableName, columnName, e.getMessage());
            throw new RuntimeException("Could not create index", e);
        }
    }

    /**
     * Drops an index from a dynamic table.
     */
    @Transactional
    public void dropIndex(String tableName, String columnName) {
        SqlIdentifierValidator.validate(tableName);
        SqlIdentifierValidator.validate(columnName);

        String indexName = generateIndexName(tableName, columnName);
        SqlIdentifierValidator.validate(indexName);

        String sql = String.format("DROP INDEX IF EXISTS %s", indexName);
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Dropped index {}", indexName);
        } catch (Exception e) {
            log.error("Failed to drop index {}: {}", indexName, e.getMessage());
            throw new RuntimeException("Could not drop index", e);
        }
    }

    private String generateIndexName(String tableName, String columnName) {
        // Example: idx_pyshop_orders_status
        return "idx_" + tableName + "_" + columnName;
    }
}
