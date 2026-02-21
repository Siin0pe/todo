package com.example.todo.service;

import com.example.todo.api.dto.AnnonceMetaResponse;
import com.example.todo.model.Annonce;
import com.example.todo.service.exception.BadRequestServiceException;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnnonceMetadataService {
    private static final List<String> FILTERABLE_QUERY_PARAMS = List.of(
            "q", "status", "categoryId", "authorId", "fromDate", "toDate"
    );

    private final Set<String> sortableFields;
    private final Set<String> searchableStringFields;

    public AnnonceMetadataService(
            @Value("${todo.annonce.searchable-string-fields:title,description,adress}") String searchableFieldConfig
    ) {
        this.sortableFields = resolveSortableFields();
        this.searchableStringFields = resolveSearchableStringFields(searchableFieldConfig);
    }

    public Set<String> getSearchableStringFields() {
        return searchableStringFields;
    }

    public Pageable validateAndNormalize(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "date");
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            if (!sortableFields.contains(property)) {
                throw new BadRequestServiceException("Unsupported sort field: " + property);
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    public AnnonceMetaResponse describe() {
        return new AnnonceMetaResponse(sortableFields, searchableStringFields, FILTERABLE_QUERY_PARAMS);
    }

    private Set<String> resolveSortableFields() {
        LinkedHashSet<String> fields = new LinkedHashSet<>();
        for (Field field : Annonce.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToMany.class)) {
                continue;
            }
            if (isSortableType(field.getType())) {
                fields.add(field.getName());
            }
        }
        return fields;
    }

    private Set<String> resolveSearchableStringFields(String config) {
        Set<String> availableStringFields = Arrays.stream(Annonce.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> field.getType().equals(String.class))
                .filter(field -> field.isAnnotationPresent(Column.class))
                .map(Field::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> configuredFields = Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<String> selected = new LinkedHashSet<>();
        for (String configuredField : configuredFields) {
            if (availableStringFields.contains(configuredField)) {
                selected.add(configuredField);
            }
        }
        if (selected.isEmpty()) {
            selected.addAll(availableStringFields);
        }
        return selected;
    }

    private boolean isSortableType(Class<?> type) {
        return type.equals(String.class)
                || Number.class.isAssignableFrom(type)
                || type.isPrimitive()
                || type.isEnum()
                || Timestamp.class.isAssignableFrom(type)
                || Instant.class.isAssignableFrom(type);
    }
}
