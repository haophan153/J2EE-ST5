package com.example.EcoSwap.controller;

import com.example.EcoSwap.dto.ProductFilterDTO;
import com.example.EcoSwap.entity.Category;
import com.example.EcoSwap.entity.Product;
import com.example.EcoSwap.entity.User;
import com.example.EcoSwap.service.CategoryService;
import com.example.EcoSwap.service.FileUploadService;
import com.example.EcoSwap.service.ExchangeService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final FileUploadService fileUploadService;
    private final ExchangeService exchangeService;

    private User requireCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));
    }

    @GetMapping("/products")
    public String products(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String condition,
                          @RequestParam(required = false) Double minPrice,
                          @RequestParam(required = false) Double maxPrice,
                          @RequestParam(required = false) String location,
                          @RequestParam(required = false) String sortBy) {
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
        Page<Product> result = productService.filterProducts(filter, pageable);
        model.addAttribute("products", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("filter", filter);
        return "products/list";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> {
            model.addAttribute("product", product);
            model.addAttribute("recommendedProducts", productService.getRecommendedProducts(id, 8));
        });
        return "products/detail";
    }

    @GetMapping("/products/{id}")
    public String productDetailAlt(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> {
            model.addAttribute("product", product);
            model.addAttribute("recommendedProducts", productService.getRecommendedProducts(id, 8));
        });
        return "products/detail";
    }

    @GetMapping("/products/create")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/create";
    }

    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute Product product, 
                                @RequestParam("imageFile") MultipartFile imageFile,
                                Authentication authentication) {
        User currentUser = requireCurrentUser(authentication);

        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new RuntimeException("Category is required");
        }
        Category category = categoryService.getCategoryById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
        
        // Upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileUploadService.uploadFile(imageFile);
            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
            }
        }
        
        // Gán user hiện tại làm chủ sản phẩm
        product.setUser(currentUser);
        product.setStatus("AVAILABLE");
        productService.createProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/products/my")
    public String myProducts(Model model,
                             Authentication authentication,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String status) {
        User currentUser = requireCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, 12, Sort.by("createdAt").descending());
        Page<Product> result;
        if (status != null && !status.isBlank()) {
            result = productService.getUserProductsHistory(currentUser.getId(), List.of(status), pageable);
        } else {
            result = productService.getProductsByUserPaged(currentUser.getId(), pageable);
        }

        model.addAttribute("products", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("status", status == null ? "" : status);
        return "products/my";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model, Authentication authentication) {
        User currentUser = requireCurrentUser(authentication);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/products/my?error=not_owner";
        }
        if ("EXCHANGED".equals(product.getStatus())) {
            return "redirect:/products/my?error=cannot_edit_exchanged";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/edit";
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable Long id,
                                @ModelAttribute Product form,
                                @RequestParam(required = false) MultipartFile imageFile,
                                Authentication authentication) {
        User currentUser = requireCurrentUser(authentication);
        Product existing = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/products/my?error=not_owner";
        }
        if ("EXCHANGED".equals(existing.getStatus())) {
            return "redirect:/products/my?error=cannot_edit_exchanged";
        }

        existing.setTitle(form.getTitle());
        existing.setDescription(form.getDescription());
        existing.setPrice(form.getPrice());
        existing.setCondition(form.getCondition());
        existing.setLocation(form.getLocation());
        if (form.getCategory() != null && form.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(form.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existing.setCategory(category);
        }

        // Cho phép đánh dấu SOLD hoặc giữ AVAILABLE (không cho set EXCHANGED bằng tay)
        if ("SOLD".equals(form.getStatus())) {
            existing.setStatus("SOLD");
        } else if ("AVAILABLE".equals(form.getStatus()) || form.getStatus() == null) {
            existing.setStatus("AVAILABLE");
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = fileUploadService.uploadFile(imageFile);
            if (newImageUrl != null) {
                if (existing.getImageUrl() != null && !existing.getImageUrl().isBlank()) {
                    fileUploadService.deleteFile(existing.getImageUrl());
                }
                existing.setImageUrl(newImageUrl);
            }
        }

        productService.updateProduct(existing);
        return "redirect:/products/my?success=updated";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        User currentUser = requireCurrentUser(authentication);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/products/my?error=not_owner";
        }
        if (exchangeService.isProductInAnyExchange(id)) {
            return "redirect:/products/my?error=in_exchange";
        }
        if ("EXCHANGED".equals(product.getStatus())) {
            return "redirect:/products/my?error=cannot_delete_exchanged";
        }

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            fileUploadService.deleteFile(product.getImageUrl());
        }
        productService.deleteProduct(id);
        return "redirect:/products/my?success=deleted";
    }
}
