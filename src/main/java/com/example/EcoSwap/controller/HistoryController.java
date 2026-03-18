package com.example.EcoSwap.controller;

import com.example.EcoSwap.entity.ExchangeRequest;
import com.example.EcoSwap.entity.Product;
import com.example.EcoSwap.entity.User;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.example.EcoSwap.entity.ExchangeRequest.ExchangeStatus;

@Controller
@RequiredArgsConstructor
public class HistoryController {

    private final ExchangeService exchangeService;
    private final ProductService productService;
    private final UserService userService;

    private User requireCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));
    }

    @GetMapping("/history/exchanges")
    public String exchangeHistory(Model model,
                                  Authentication authentication,
                                  @RequestParam(defaultValue = "0") int page) {
        User currentUser = requireCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, 10, Sort.by("updatedAt").descending());
        Page<ExchangeRequest> result = exchangeService.getUserHistoryByStatus(currentUser.getId(), ExchangeStatus.COMPLETED, pageable);
        model.addAttribute("exchanges", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        return "history/exchanges";
    }

    @GetMapping("/history/sales")
    public String salesHistory(Model model,
                               Authentication authentication,
                               @RequestParam(defaultValue = "0") int page) {
        User currentUser = requireCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, 12, Sort.by("updatedAt").descending());
        Page<Product> result = productService.getUserProductsHistory(currentUser.getId(), List.of("SOLD", "EXCHANGED"), pageable);
        model.addAttribute("products", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        return "history/sales";
    }
}

