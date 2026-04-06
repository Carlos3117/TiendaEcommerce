package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ConflictException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
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


    @Override
    public Category createCategory(Category category) {

        if (category.getName() == null || category.getName().isBlank()) {
            throw new BusinessException("El nombre de la categoría es obligatorio");
        }

        categoryRepository.findByName(category.getName())
                .ifPresent(c -> {
                    throw new ConflictException("La categoría ya existe");
                });

        return categoryRepository.save(category);
    }


    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    @Override
    public Category updateCategory(Long id, Category updated) {

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (updated.getName() != null && !updated.getName().isBlank()) {

            categoryRepository.findByName(updated.getName())
                    .ifPresent(c -> {
                        if (!c.getId().equals(id)) {
                            throw new ConflictException("Ya existe una categoría con ese nombre");
                        }
                    });

            existing.setName(updated.getName());
        }

        return categoryRepository.save(existing);
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