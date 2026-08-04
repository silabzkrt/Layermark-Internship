package com.hrmanproject.domain.engine.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnMetadata {

    private String name;
    private String dataType; // VARCHAR(255), INTEGER, BIGINT, DOUBLE PRECISION, BOOLEAN, TEXT, TIMESTAMP
    @Builder.Default
    private boolean primaryKey = false;
    @Builder.Default
    private boolean nullable = true;
    @Builder.Default
    private boolean unique = false;
    private String defaultValue;
    private String regexPattern;
    private String validationType;
}
