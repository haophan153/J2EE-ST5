package com.example.EcoSwap.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductFilterDTO {
    private String keyword;
    private Long categoryId;
    private String condition;      // NEW, LIKE_NEW, USED
    private Double minPrice;
    private Double maxPrice;
    private String location;
    private String sortBy;         // newest, price_asc, price_desc, title
}
