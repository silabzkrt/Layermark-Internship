package com.hrmanproject.domain.engine.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationMetadata {

    public enum RelationType {
        MANY_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY,
        ONE_TO_ONE
    }

    private String sourceTable;
    private String targetTable;
    private String foreignKeyColumn;
    private RelationType relationType;
    private String joinTable;
    private String sourceJoinColumn;
    private String targetJoinColumn;
}
