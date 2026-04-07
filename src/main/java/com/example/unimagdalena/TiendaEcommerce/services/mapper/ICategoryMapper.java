package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.CategoryDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ICategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CreateCategoryRequest req);

    CategoryResponse toResponse(Category entity);
}