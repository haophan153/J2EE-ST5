package com.example.EcoSwap.controller;

import com.example.EcoSwap.entity.User;
import com.example.EcoSwap.service.FileUploadService;
import com.example.EcoSwap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profile/index";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User user, 
                                Authentication authentication,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) MultipartFile avatarFile,
                                Model model) {
        String username = authentication.getName();
        User existingUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());
        
        if (newPassword != null && !newPassword.isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        
        // Cập nhật ảnh đại diện nếu có file mới
        boolean avatarError = false;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarPath = fileUploadService.uploadImage(avatarFile);
            if (avatarPath != null) {
                String oldAvatar = existingUser.getAvatar();
                existingUser.setAvatar(avatarPath);
                if (oldAvatar != null && !oldAvatar.isEmpty()) {
                    fileUploadService.deleteFile(oldAvatar);
                }
            } else {
                avatarError = true;
            }
        }
        
        userService.updateUser(existingUser);
        return avatarError ? "redirect:/profile?success&avatarError" : "redirect:/profile?success";
    }
}
