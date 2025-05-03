package com.nitesh.product.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {
    private String name;
    private String category;
    private Float priceMin;
    private Float priceMax;
}
