package com.example.EcoSwap.controller;

import com.example.EcoSwap.dto.ProductFilterDTO;
import com.example.EcoSwap.entity.Category;
import com.example.EcoSwap.entity.Product;
import com.example.EcoSwap.service.CategoryService;
import com.example.EcoSwap.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/category/{id}")
    public String categoryProducts(@PathVariable Long id, Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String condition,
                                  @RequestParam(required = false) Double minPrice,
                                  @RequestParam(required = false) Double maxPrice,
                                  @RequestParam(required = false) String location,
                                  @RequestParam(required = false) String sortBy) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isPresent()) {
            ProductFilterDTO filter = ProductFilterDTO.builder()
                    .categoryId(id)
                    .condition(condition)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .location(location)
                    .sortBy(sortBy)
                    .build();
            Sort sort = productService.getSortFromFilter(sortBy);
            Pageable pageable = PageRequest.of(page, 12, sort);
            Page<Product> products = productService.filterProducts(filter, pageable);
            model.addAttribute("category", categoryOpt.get());
            model.addAttribute("products", products.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("filter", filter);
            model.addAttribute("categories", categoryService.getAllCategories());
        }
        return "categories/detail";
    }
}
