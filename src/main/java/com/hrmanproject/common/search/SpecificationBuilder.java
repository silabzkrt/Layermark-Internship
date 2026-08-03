package com.hrmanproject.common.search;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SpecificationBuilder<T> {

    public static <T> Specification<T> build(List<SearchCriteria> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return Specification.where(null);
        }

        Specification<T> result = null;
        for (SearchCriteria criteria : criteriaList) {
            Specification<T> spec = new GenericSpecification<>(criteria);
            result = (result == null) ? Specification.where(spec) : result.and(spec);
        }
        return result;
    }
}
