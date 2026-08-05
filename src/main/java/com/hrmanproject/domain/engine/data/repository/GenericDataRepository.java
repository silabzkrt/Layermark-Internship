package com.hrmanproject.domain.engine.data.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class GenericDataRepository {

    private final JdbcTemplate jdbcTemplate;

    public GenericDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String tableName, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<>();

        data.forEach((key, value) -> {
            if (!columns.isEmpty()) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(key);
            placeholders.append("?");
            values.add(value);
        });

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        jdbcTemplate.update(sql, values.toArray());
    }

    public List<Map<String, Object>> findAll(String tableName) {
        String sql = String.format("SELECT * FROM %s", tableName);
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> findById(String tableName, Long id) {
        String sql = String.format("SELECT * FROM %s WHERE id = ?", tableName);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public void delete(String tableName, Long id) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        jdbcTemplate.update(sql, id);
    }
}
