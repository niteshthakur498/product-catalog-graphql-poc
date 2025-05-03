package com.nitesh.product.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SortCriteria {

    private String field;
    private String direction; // "asc" or "desc"

}
