package com.hrmanproject.domain.engine.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableMetadata {

    private String tableName;
    private String description;

    @Builder.Default
    private List<ColumnMetadata> columns = new ArrayList<>();

    @Builder.Default
    private List<RelationMetadata> relations = new ArrayList<>();

    public Optional<ColumnMetadata> getColumn(String columnName) {
        if (columns == null) return Optional.empty();
        return columns.stream()
                .filter(c -> c.getName().equalsIgnoreCase(columnName))
                .findFirst();
    }

    public Optional<RelationMetadata> getRelation(String targetTable) {
        if (relations == null) return Optional.empty();
        return relations.stream()
                .filter(r -> r.getTargetTable().equalsIgnoreCase(targetTable))
                .findFirst();
    }
}
