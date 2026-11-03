package com.camerarental.controller;

import com.camerarental.config.AuthInterceptor;
import com.camerarental.entity.User;
import com.camerarental.service.SettingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public String settingsPage(@RequestParam(value = "tab", defaultValue = "account") String tab,
                               HttpSession session,
                               Model model) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        boolean isAdmin = currentUser != null && currentUser.getRole() == User.Role.ADMIN;

        if (!isAdmin && ("system".equals(tab) || "users".equals(tab))) {
            tab = "account";
        }

        model.addAttribute("activeTab", tab);
        model.addAttribute("settingsMap", settingService.getAllSettingsMap());
        if (isAdmin) {
            model.addAttribute("users", settingService.getAllUsers());
            model.addAttribute("roles", User.Role.values());
        }
        model.addAttribute("activityLogs", settingService.getRecentLogs());
        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "email", required = false) String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            User updated = settingService.updateOwnProfile(currentUser.getId(), fullName, phone, email);
            session.setAttribute(AuthInterceptor.SESSION_USER_KEY, updated);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=account";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            settingService.changeOwnPassword(currentUser.getId(), oldPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=account";
    }

    @PostMapping("/system")
    public String updateSystemSettings(@RequestParam Map<String, String> formParams,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            Map<String, String> updates = new LinkedHashMap<>();
            String[] allowedKeys = {
                    SettingService.KEY_LATE_FEE_PERCENT,
                    SettingService.KEY_DEFAULT_DEPOSIT_PERCENT,
                    SettingService.KEY_DEFAULT_DEPOSIT_AMOUNT,
                    SettingService.KEY_MAX_RENTAL_DAYS,
                    SettingService.KEY_DUE_WARNING_DAYS,
                    SettingService.KEY_SHOP_NAME,
                    SettingService.KEY_SHOP_ADDRESS,
                    SettingService.KEY_SHOP_PHONE,
                    SettingService.KEY_PAGE_SIZE
            };
            for (String key : allowedKeys) {
                if (formParams.containsKey(key)) {
                    updates.put(key, formParams.get(key));
                }
            }
            settingService.updateSystemSettings(updates, currentUser);
            String redirectTab = formParams.getOrDefault("redirectTab", "system");
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu cấu hình cài đặt hệ thống thành công!");
            return "redirect:/settings?tab=" + redirectTab;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/settings?tab=system";
        }
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("fullName") String fullName,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "email", required = false) String email,
                             @RequestParam("role") User.Role role,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            User created = settingService.createUser(username, password, fullName, phone, email, role, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thêm tài khoản @" + created.getUsername() + " (" + created.getFullName() + ") thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable("id") Long userId,
                             @RequestParam("role") User.Role role,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            settingService.changeUserRole(userId, role, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật vai trò người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleActive(@PathVariable("id") Long userId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            settingService.toggleUserActive(userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hoạt động của tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable("id") Long userId,
                                @RequestParam("newPassword") String newPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        try {
            settingService.resetUserPassword(userId, newPassword, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu mới cho nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings?tab=users";
    }

    @GetMapping("/backup")
    public ResponseEntity<byte[]> downloadBackup(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        String sqlContent = settingService.generateSqlBackup(currentUser);
        String fileName = "procam_backup_" + LocalDate.now() + ".sql";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("application", "sql", StandardCharsets.UTF_8))
                .body(sqlContent.getBytes(StandardCharsets.UTF_8));
    }
}
