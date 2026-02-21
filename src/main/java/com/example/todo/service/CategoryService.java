package com.example.todo.service;

import com.example.todo.api.dto.CategoryCreateRequest;
import com.example.todo.api.dto.CategoryResponse;
import com.example.todo.api.dto.CategoryUpdateRequest;
import com.example.todo.api.dto.PaginatedResponse;
import com.example.todo.api.mapper.CategoryMapper;
import com.example.todo.model.Category;
import com.example.todo.repository.CategoryRepository;
import com.example.todo.service.exception.NotFoundServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        LOGGER.info("category_service_create_requested label={}", request.getLabel());
        Category category = categoryMapper.toEntity(request);
        Category created = categoryRepository.save(category);
        LOGGER.info("category_service_create_succeeded categoryId={}", created.getId());
        return categoryMapper.toResponse(created);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        LOGGER.info("category_service_update_requested categoryId={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    LOGGER.warn("category_service_update_not_found categoryId={}", categoryId);
                    return new NotFoundServiceException("Category not found");
                });
        categoryMapper.updateFromRequest(request, category);
        Category updated = categoryRepository.save(category);
        LOGGER.info("category_service_update_succeeded categoryId={}", categoryId);
        return categoryMapper.toResponse(updated);
    }

    public void deleteCategory(Long categoryId) {
        LOGGER.info("category_service_delete_requested categoryId={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    LOGGER.warn("category_service_delete_not_found categoryId={}", categoryId);
                    return new NotFoundServiceException("Category not found");
                });
        categoryRepository.delete(category);
        LOGGER.info("category_service_delete_succeeded categoryId={}", categoryId);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long categoryId) {
        LOGGER.info("category_service_find_by_id_requested categoryId={}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    LOGGER.warn("category_service_find_by_id_not_found categoryId={}", categoryId);
                    return new NotFoundServiceException("Category not found");
                });
        LOGGER.info("category_service_find_by_id_succeeded categoryId={}", categoryId);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> listCategories(int page, int size) {
        LOGGER.info("category_service_list_requested page={} size={}", page, size);
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.ASC, "label"));
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        List<CategoryResponse> items = categoryPage.getContent().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        LOGGER.info("category_service_list_succeeded page={} size={} returned={}", page, size, items.size());
        return new PaginatedResponse<>(page, size, items);
    }
}
