package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.Product;
import java.util.List;
public interface ProductService {

    Product createProduct(Product product);


    Product getProductById(Long id);


    List<Product> getAllProducts();


    Product updateProduct(Long id, Product updatedProduct);


    Product changeProductStatus(Long id, Boolean active);
}