package com.nitesh.product.catalog.resource;

import com.nitesh.product.catalog.dto.ProductConnection;
import com.nitesh.product.catalog.dto.ProductFilter;
import com.nitesh.product.catalog.dto.SortCriteria;
import com.nitesh.product.catalog.entity.Product;
import com.nitesh.product.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @QueryMapping
    public Flux<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    @QueryMapping
    public Flux<Product> getProducts(@Argument ProductFilter filter) {
        return productService.getProducts(filter);
    }

    @QueryMapping
    public Flux<Product> getProductsSorted(@Argument ProductFilter filter,
                                  @Argument SortCriteria sort) {
        return productService.findProductsSorted(filter, sort);
    }

    @QueryMapping
    public Mono<ProductConnection> getProductsSortedPaged(@Argument ProductFilter filter,
                                            @Argument SortCriteria sort,
                                            @Argument int page,
                                            @Argument int size) {
        return productService.findProductsSortedPaged(filter, sort, page, size);
    }


}
