package com.hongyuting.sports.interceptor;

import com.hongyuting.sports.service.AdminService;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.util.JwtUtil;
import com.hongyuting.sports.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final AdminService adminService;
    private final UserContext userContext;
    /**
     * 请求处理之前执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 不是控制器方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供有效的认证令牌\"}");
            return false;
        }

        String token = authHeader.substring(7);
        String requestURI = request.getRequestURI();

        // 管理员接口处理
        if (requestURI.startsWith("/api/admin/")) {
            // 排除登录和刷新token接口
            if (requestURI.contains("/api/admin/login") || 
                requestURI.contains("/api/admin/refresh")) {
                return true;
            }

            // 验证JWT Token格式是否有效
            if (jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"认证令牌格式无效\"}");
                return false;
            }

            // 验证Token是否存在于Redis中
            if (adminService.existsToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"认证令牌无效或已过期\"}");
                return false;
            }

            // 获取管理员ID并设置到请求属性中
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("adminId", adminId);
            request.setAttribute("userId", adminId); // 兼容普通用户属性
            
            // 同时设置UserContext
            userContext.setUserId(adminId);

            // 刷新Token过期时间（如果快过期）
            if (jwtUtil.isTokenExpiringSoon(token, 30)) { // 30分钟内过期
                adminService.refreshToken(token);
            }
            
            return true;
        }

        // 普通用户接口处理
        // 验证JWT Token格式是否有效
        if (jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"认证令牌格式无效\"}");
            return false;
        }

        // 验证Token是否存在于Redis中
        if (!tokenService.existsToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"认证令牌无效或已过期\"}");
            return false;
        }

        // 获取用户信息并设置到请求属性中
        Integer userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId);
        
        // 同时设置UserContext
        userContext.setUserId(userId);

        // 刷新Token过期时间（如果快过期）
        if (jwtUtil.isTokenExpiringSoon(token, 30)) { // 30分钟内过期
            tokenService.refreshToken(token);
        }

        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理UserContext，避免内存泄漏
        userContext.clear();
    }
}