package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.services.mapper.ICategoryMapper;
import com.example.unimagdalena.TiendaEcommerce.repositories.CategoryRepository;
import com.example.unimagdalena.TiendaEcommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ICategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        // Regla de negocio
        categoryRepository.findByName(request.name())
                .ifPresent(c -> {
                    throw new ConflictException("La categoría ya existe");
                });

        Category category = categoryMapper.toEntity(request);

        Category saved = categoryRepository.save(category);

        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (request.name() != null) {
            categoryRepository.findByName(request.name())
                    .ifPresent(c -> {
                        if (!c.getId().equals(id)) {
                            throw new ConflictException("Ya existe una categoría con ese nombre");
                        }
                    });
        }

        categoryMapper.patch(existing, request);

        Category updated = categoryRepository.save(existing);

        return categoryMapper.toResponse(updated);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (!productRepository.findByCategoryAndActiveTrue(category).isEmpty()) {
            throw new BusinessException("No se puede eliminar una categoría con productos asociados");
        }

        categoryRepository.delete(category);
    }
}