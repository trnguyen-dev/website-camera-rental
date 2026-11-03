package com.camerarental.config;

import com.camerarental.entity.User;
import com.camerarental.service.SettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final SettingService settingService;

    public GlobalControllerAdvice(SettingService settingService) {
        this.settingService = settingService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute(AuthInterceptor.SESSION_USER_KEY);
        }
        return null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("systemSettings")
    public Map<String, String> getSystemSettings() {
        return settingService.getAllSettingsMap();
    }
}
