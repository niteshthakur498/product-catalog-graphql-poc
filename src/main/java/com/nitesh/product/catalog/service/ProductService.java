package com.nitesh.product.catalog.service;

import com.nitesh.product.catalog.dto.ProductConnection;
import com.nitesh.product.catalog.dto.ProductFilter;
import com.nitesh.product.catalog.dto.SortCriteria;
import com.nitesh.product.catalog.entity.Product;
import com.nitesh.product.catalog.repository.ProductRepository;
import io.r2dbc.spi.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final DatabaseClient databaseClient;

    public Flux<Product> getAllProducts(){
        return  productRepository.findAll();
    }

    public Flux<Product> getProducts(ProductFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.getName() != null) {
            sql.append(" AND name ILIKE :name");
            params.put("name", "%" + filter.getName() + "%");
        }
        if (filter.getCategory() != null) {
            sql.append(" AND category = :category");
            params.put("category", filter.getCategory());
        }
        if (filter.getPriceMin() != null) {
            sql.append(" AND price >= :priceMin");
            params.put("priceMin", filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            sql.append(" AND price <= :priceMax");
            params.put("priceMax", filter.getPriceMax());
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }

        return spec.map((row, meta) -> mapRowToProduct(row)).all();
    }



    private Product mapRowToProduct(Row row) {
        return new Product(
                row.get("id", Long.class),
                row.get("name", String.class),
                row.get("description", String.class),
                row.get("category", String.class),
                row.get("price", BigDecimal.class), // Convert BigDecimal to Float
                row.get("stock", Integer.class),
                row.get("sku", String.class),
                row.get("active", Boolean.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

    public Flux<Product> findProductsSorted(ProductFilter filter, SortCriteria sort) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
        Map<String, Object> params = new HashMap<>();
        if(filter!=null ) {
            if (filter.getName() != null) {
                sql.append(" AND name ILIKE :name");
                params.put("name", "%" + filter.getName() + "%");
            }
            if (filter.getCategory() != null) {
                sql.append(" AND category = :category");
                params.put("category", filter.getCategory());
            }
            if (filter.getPriceMin() != null) {
                sql.append(" AND price >= :priceMin");
                params.put("priceMin", filter.getPriceMin());
            }
            if (filter.getPriceMax() != null) {
                sql.append(" AND price <= :priceMax");
                params.put("priceMax", filter.getPriceMax());
            }
        }
        if (sort != null && sort.getField() != null && sort.getDirection() != null) {
            sql.append(" ORDER BY ").append(safeColumn(sort.getField()))
                    .append(" ").append(sort.getDirection().equalsIgnoreCase("desc") ? "DESC" : "ASC");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }

        return spec.map((row, meta) -> mapRowToProduct(row)).all();
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "price", "created_at", "updated_at", "category", "stock"
    );

    private String safeColumn(String field) {
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }
        return field;
    }

    public Mono<ProductConnection> findProductsSortedPaged(ProductFilter filter, SortCriteria sort, int page, int size) {
        String sql = buildQuery(filter, sort);

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql);

        if (filter.getName() != null) {
            spec = spec.bind("name", "%" + filter.getName() + "%");
        }
        if (filter.getCategory() != null) {
            spec = spec.bind("category", filter.getCategory());
        }
        if (filter.getPriceMin() != null) {
            spec = spec.bind("priceMin", filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            spec = spec.bind("priceMax", filter.getPriceMax());
        }

        int offset = page * size;

        spec = spec.bind("limit", size).bind("offset", offset);

        Mono<List<Product>> content = spec.map((row, meta) -> mapRowToProduct(row)).all().collectList();

        Mono<Integer> total = countTotalMatching(filter);

        return Mono.zip(content, total)
                .map(tuple -> {
                    List<Product> items = tuple.getT1();
                    int totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / size);

                    return new ProductConnection(
                            items,
                            totalElements,
                            totalPages,
                            page,
                            size
                    );
                });
    }

    public Mono<Integer> countTotalMatching(ProductFilter filter) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM product WHERE 1=1");

        if (filter.getName() != null) {
            countSql.append(" AND name ILIKE :name");
        }
        if (filter.getCategory() != null) {
            countSql.append(" AND category = :category");
        }
        if (filter.getPriceMin() != null) {
            countSql.append(" AND price >= :priceMin");
        }
        if (filter.getPriceMax() != null) {
            countSql.append(" AND price <= :priceMax");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(countSql.toString());

        if (filter.getName() != null) {
            spec = spec.bind("name", "%" + filter.getName() + "%");
        }
        if (filter.getCategory() != null) {
            spec = spec.bind("category", filter.getCategory());
        }
        if (filter.getPriceMin() != null) {
            spec = spec.bind("priceMin", filter.getPriceMin());
        }
        if (filter.getPriceMax() != null) {
            spec = spec.bind("priceMax", filter.getPriceMax());
        }

        return spec.map(row -> {
            Object count = row.get(0);
            System.out.println("Type of count: " + count.getClass());
            return ((Number) count).intValue();
        }).one();
    }


    public String buildQuery(ProductFilter filter, SortCriteria sort) {
        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
        if(filter!=null) {
            if (filter.getName() != null) {
                sql.append(" AND name ILIKE :name");
            }
            if (filter.getCategory() != null) {
                sql.append(" AND category = :category");
            }
            if (filter.getPriceMin() != null) {
                sql.append(" AND price >= :priceMin");
            }
            if (filter.getPriceMax() != null) {
                sql.append(" AND price <= :priceMax");
            }
        }
        // Sorting
        if (sort != null && sort.getField() != null && sort.getDirection() != null) {
            sql.append(" ORDER BY ")
                    .append(safeColumn(sort.getField()))
                    .append(" ").append(sort.getDirection().equalsIgnoreCase("desc") ? "DESC" : "ASC");
        }

        sql.append(" LIMIT :limit OFFSET :offset");

        return sql.toString();
    }

}

