package com.example.EcoSwap.controller;

import com.example.EcoSwap.dto.ProductFilterDTO;
import com.example.EcoSwap.entity.Category;
import com.example.EcoSwap.entity.Product;
import com.example.EcoSwap.entity.User;
import com.example.EcoSwap.service.CategoryService;
import com.example.EcoSwap.service.ProductService;
import com.example.EcoSwap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String condition,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) String sortBy,
                       Authentication auth) {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .categoryId(categoryId)
                .condition(condition)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .location(location)
                .sortBy(sortBy)
                .build();
        Sort sort = productService.getSortFromFilter(sortBy);
        Pageable pageable = PageRequest.of(page, 12, sort);
        Page<Product> products = productService.filterProducts(filter, pageable);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("filter", filter);

        if (auth != null && auth.isAuthenticated()) {
            userService.getUserByUsername(auth.getName()).ifPresent(user -> {
                List<Product> recommended = productService.getRecommendedForUser(user.getId(), 8);
                model.addAttribute("recommendedProducts", recommended);
            });
        }

        return "index";
    }

    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String condition,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) String location,
                        @RequestParam(required = false) String sortBy,
                        Authentication auth) {
        ProductFilterDTO filter = ProductFilterDTO.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .condition(condition)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .location(location)
                .sortBy(sortBy)
                .build();
        Sort sort = productService.getSortFromFilter(sortBy);
        Pageable pageable = PageRequest.of(page, 12, sort);
        Page<Product> products = productService.filterProducts(filter, pageable);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("filter", filter);

        if (auth != null && auth.isAuthenticated()) {
            userService.getUserByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("recommendedProducts", productService.getRecommendedForUser(user.getId(), 8));
            });
        }

        return "index";
    }
}
