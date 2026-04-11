package com.example.unimagdalena.TiendaEcommerce.services.mapper;

import com.example.unimagdalena.TiendaEcommerce.dto.ProductDto.*;
import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import com.example.unimagdalena.TiendaEcommerce.entities.Category;

public class ProductMapper {

    public static Product toEntity(CreateProductRequest req, Category category) {
        Product p = new Product();
        p.setName(req.name());
        p.setSku(req.sku());
        p.setPrice(req.price());
        p.setCategory(category);
        p.setActive(true);
        return p;
    }

    public static void patch(Product p, UpdateProductRequest req, Category category) {

        if (req.name() != null) p.setName(req.name());
        if (req.price() != null) p.setPrice(req.price());
        if (req.sku() != null) p.setSku(req.sku());
        if (category != null) p.setCategory(category);
    }
}