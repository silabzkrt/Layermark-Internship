-- V2__create_sys_metadata_tables.sql
-- Flyway migration script for persistent System Metadata Catalog tables

CREATE TABLE IF NOT EXISTS sys_table_metadata (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_column_metadata (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL REFERENCES sys_table_metadata(table_name) ON DELETE CASCADE,
    column_name VARCHAR(255) NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    is_primary_key BOOLEAN DEFAULT FALSE,
    is_nullable BOOLEAN DEFAULT TRUE,
    is_unique BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(255),
    regex_pattern VARCHAR(255),
    validation_type VARCHAR(50),
    CONSTRAINT uk_sys_col_meta UNIQUE (table_name, column_name)
);
