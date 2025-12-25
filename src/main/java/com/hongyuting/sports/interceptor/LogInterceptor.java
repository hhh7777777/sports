package com.hongyuting.sports.interceptor;

import com.hongyuting.sports.entity.OperationLog;
import com.hongyuting.sports.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作日志拦截器 - 记录所有用户和管理员的操作
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 记录所有用户和管理员的操作日志
        String requestURI = request.getRequestURI();

        // 避免记录静态资源请求和健康检查等无意义的日志
        if (shouldRecordLog(requestURI)) {
            OperationLog operationLog = new OperationLog();

            // 检查是否为管理员操作
            Integer adminId = (Integer) request.getAttribute("adminId");
            if (adminId != null) {
                operationLog.setAdminId(adminId);
                operationLog.setUserType("ADMIN");
            } else {
                // 检查是否为普通用户操作
                Integer userId = (Integer) request.getAttribute("userId");
                if (userId != null) {
                    operationLog.setUserId(userId);
                    operationLog.setUserType("USER");
                }
            }

            // 只有当存在用户或管理员ID时才记录日志
            if (operationLog.getAdminId() != null || operationLog.getUserId() != null) {
                operationLog.setOperation(request.getMethod() + " " + requestURI);
                operationLog.setTargetType("API");
                operationLog.setIpAddress(getClientIP(request));
                operationLog.setOperationType(getOperationType(request.getMethod(), requestURI));

                operationLogService.addOperationLog(operationLog);
            }
        }
    }

    /**
     * 判断是否应该记录日志
     */
    private boolean shouldRecordLog(String requestURI) {
        // 排除静态资源、健康检查等不需要记录的操作
        return !requestURI.contains("/static/") &&
               !requestURI.contains("/assets/") &&
               !requestURI.contains("/favicon.ico") &&
               !requestURI.contains("/health") &&
               !requestURI.startsWith("/api/file/") && // 文件访问通常不需要记录
               !requestURI.endsWith(".css") &&
               !requestURI.endsWith(".js") &&
               !requestURI.endsWith(".png") &&
               !requestURI.endsWith(".jpg") &&
               !requestURI.endsWith(".jpeg") &&
               !requestURI.endsWith(".gif") &&
               !requestURI.endsWith(".ico");
    }
    
    /**
     * 根据请求方法和URI确定操作类型
     */
    private String getOperationType(String method, String uri) {
        switch (method.toUpperCase()) {
            case "GET":
                return "READ";
            case "POST":
                if (uri.contains("/login") || uri.contains("/register")) {
                    return "AUTH";
                }
                return "CREATE";
            case "PUT":
            case "PATCH":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "OTHER";
        }
    }
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}