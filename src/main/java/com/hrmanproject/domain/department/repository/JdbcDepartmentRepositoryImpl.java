package com.hrmanproject.domain.department.repository;

import com.hrmanproject.domain.department.Department;
import com.hrmanproject.domain.engine.sql.SafeSqlCompiler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDepartmentRepositoryImpl implements DepartmentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SafeSqlCompiler sqlCompiler;

    private final RowMapper<Department> rowMapper = (rs, rowNum) -> {
        Department d = new Department(rs.getString("name"), rs.getString("code"));
        d.setId(rs.getLong("id"));
        return d;
    };

    public JdbcDepartmentRepositoryImpl(JdbcTemplate jdbcTemplate, SafeSqlCompiler sqlCompiler) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlCompiler = sqlCompiler;
    }

    @Override
    public Department save(Department department) {
        if (department.getId() == null) {
            String sql = "INSERT INTO departments (name, code) VALUES (?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, department.getName());
                ps.setString(2, department.getCode());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                department.setId(keyHolder.getKey().longValue());
            }
            return department;
        } else {
            String sql = "UPDATE departments SET name = ?, code = ? WHERE id = ?";
            jdbcTemplate.update(sql, department.getName(), department.getCode(), department.getId());
            return department;
        }
    }

    @Override
    public Optional<Department> findById(Long id) {
        String sql = sqlCompiler.buildSelectById("departments", "id");
        List<Department> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Department> findAll() {
        String sql = sqlCompiler.buildSelectAll("departments");
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = sqlCompiler.buildDeleteById("departments", "id");
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Department> findByCode(String code) {
        String sql = sqlCompiler.buildSelectByColumn("departments", "code");
        List<Department> results = jdbcTemplate.query(sql, rowMapper, code);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByName(String name) {
        String sql = sqlCompiler.buildExistsByColumn("departments", "name");
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByCode(String code) {
        String sql = sqlCompiler.buildExistsByColumn("departments", "code");
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM departments");
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM departments", Long.class);
        return count != null ? count : 0L;
    }
}
