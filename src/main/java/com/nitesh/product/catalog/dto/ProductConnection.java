package com.nitesh.product.catalog.dto;

import com.nitesh.product.catalog.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductConnection {
    private List<Product> content;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
