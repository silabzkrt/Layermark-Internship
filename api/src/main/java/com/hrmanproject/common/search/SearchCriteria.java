package com.hrmanproject.common.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteria {

    @NotBlank(message = "Search field name cannot be blank")
    private String field;

    @NotNull(message = "Search operator must be provided")
    private SearchOperator operator;

    private Object value;
}
