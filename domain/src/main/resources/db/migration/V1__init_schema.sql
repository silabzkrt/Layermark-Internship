-- V1__init_schema.sql
-- Flyway baseline migration script for HR Management System schema (Safha 2)

CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    target_days INTEGER NOT NULL DEFAULT 0,
    actual_days INTEGER DEFAULT 0,
    completion_rate DOUBLE PRECISION DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    mail VARCHAR(255) NOT NULL UNIQUE,
    phone_num VARCHAR(255) NOT NULL UNIQUE,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    employee_type VARCHAR(50) NOT NULL DEFAULT 'NULL',
    base_salary DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    performance_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    current_salary DOUBLE PRECISION NOT NULL DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS employee_current_projects (
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, project_id)
);

CREATE TABLE IF NOT EXISTS employee_completed_projects (
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, project_id)
);
