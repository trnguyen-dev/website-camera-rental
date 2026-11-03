package com.camerarental.config;

import com.camerarental.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_KEY = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute(SESSION_USER_KEY) : null;

        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        boolean isAdminOnlyPath = uri.startsWith("/statistics")
                || uri.startsWith("/equipments/save")
                || uri.startsWith("/equipments/delete")
                || uri.startsWith("/categories")
                || uri.startsWith("/settings/system")
                || uri.startsWith("/settings/users")
                || uri.startsWith("/settings/backup");

        if (isAdminOnlyPath && currentUser.getRole() != User.Role.ADMIN) {
            String msg = URLEncoder.encode("Bạn đang đăng nhập với quyền Nhân viên. Chức năng này chỉ dành cho Quản trị viên (Admin)!", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/?error=" + msg);
            return false;
        }

        request.setAttribute("currentUser", currentUser);
        request.setAttribute("isAdmin", currentUser.getRole() == User.Role.ADMIN);
        return true;
    }
}
