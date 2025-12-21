package com.hongyuting.sports.interceptor;

import com.hongyuting.sports.entity.AdminLog;
import com.hongyuting.sports.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 日志拦截器
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminService adminService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 记录管理员操作日志（只记录管理员操作）
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/admin/")) {
            Integer adminId = (Integer) request.getAttribute("adminId");
            if (adminId != null) {
                AdminLog adminLog = new AdminLog();
                adminLog.setAdminId(adminId);
                adminLog.setOperation(request.getMethod() + " " + requestURI);
                adminLog.setTargetType("API");
                adminLog.setIpAddress(getClientIP(request));

                adminService.addAdminLog(adminLog);
            }
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}