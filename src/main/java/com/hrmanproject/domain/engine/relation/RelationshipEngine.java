package com.hrmanproject.domain.engine.relation;

import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RelationshipEngine {

    private final JdbcTemplate jdbcTemplate;
    private final SafeSqlCompiler sqlCompiler;

    public RelationshipEngine(JdbcTemplate jdbcTemplate, SafeSqlCompiler sqlCompiler) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlCompiler = sqlCompiler;
    }

    public <T> Optional<T> loadManyToOne(String targetTable, String idColumn, Object fkValue, RowMapper<T> rowMapper) {
        if (fkValue == null) {
            return Optional.empty();
        }
        String sql = sqlCompiler.buildSelectById(targetTable, idColumn);
        List<T> results = jdbcTemplate.query(sql, rowMapper, fkValue);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public <T> List<T> loadManyToMany(String joinTable,
                                      String sourceJoinCol,
                                      Object sourceId,
                                      String targetTable,
                                      String targetIdCol,
                                      RowMapper<T> targetMapper) {
        String sql = String.format(
                "SELECT t.* FROM %s t INNER JOIN %s j ON t.%s = j.%s WHERE j.%s = ?",
                targetTable, joinTable, targetIdCol, targetTable.equals("projects") ? "project_id" : "target_id", sourceJoinCol
        );
        return jdbcTemplate.query(sql, targetMapper, sourceId);
    }
}
