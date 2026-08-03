package com.hrmanproject.common.search;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.stream.Collectors;

public class GenericSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criteria == null || criteria.getField() == null || criteria.getOperator() == null) {
            return null;
        }

        Path<?> path = getPath(root, criteria.getField());
        Object castedValue = castToRequiredType(path.getJavaType(), criteria.getValue());

        switch (criteria.getOperator()) {
            case EQUAL:
                if (castedValue == null) {
                    return builder.isNull(path);
                }
                return builder.equal(path, castedValue);
            case NOT_EQUAL:
                if (castedValue == null) {
                    return builder.isNotNull(path);
                }
                return builder.notEqual(path, castedValue);
            case GREATER_THAN:
                return builder.greaterThan((Expression<Comparable>) path, (Comparable) castedValue);
            case GREATER_THAN_EQUAL:
                return builder.greaterThanOrEqualTo((Expression<Comparable>) path, (Comparable) castedValue);
            case LESS_THAN:
                return builder.lessThan((Expression<Comparable>) path, (Comparable) castedValue);
            case LESS_THAN_EQUAL:
                return builder.lessThanOrEqualTo((Expression<Comparable>) path, (Comparable) castedValue);
            case LIKE:
                if (criteria.getValue() == null) {
                    return builder.conjunction();
                }
                return builder.like(builder.lower(path.as(String.class)), "%" + criteria.getValue().toString().toLowerCase() + "%");
            case IN:
                if (criteria.getValue() instanceof Collection) {
                    Collection<?> collection = (Collection<?>) criteria.getValue();
                    Collection<Object> castedCollection = collection.stream()
                            .map(item -> castToRequiredType(path.getJavaType(), item))
                            .collect(Collectors.toList());
                    return path.in(castedCollection);
                }
                return builder.equal(path, castedValue);
            default:
                return null;
        }
    }

    private Path<?> getPath(Root<T> root, String field) {
        String[] parts = field.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object castToRequiredType(Class<?> targetType, Object value) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        String str = value.toString().trim();
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, str);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(str);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(str);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(str);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(str);
        }
        return value;
    }
}
