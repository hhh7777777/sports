package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.entity.UserBadge;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.BadgeService;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.service.FileUploadService;
import com.hongyuting.sports.util.SaltUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
    /**
     * 用户控制器
     */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    /**
     * 用户服务
     */
    private final UserService userService;
    private final UserMapper userMapper;
    private final FileUploadService fileUploadService; // 添加文件上传服务
    private final BadgeService badgeService;
    private final TokenService tokenService;
    private final SaltUtil saltUtil;
    
    /**
     * 用户注册
     */

    @PostMapping("/register")
    public ResponseDTO register(@RequestBody RegisterDTO registerDTO, HttpSession session) {
        // 验证确认密码
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return ResponseDTO.error("密码和确认密码不一致");
        }

        // 验证密码强度
        if (registerDTO.getPassword().length() < 6) {
            return ResponseDTO.error("密码长度不能少于6位");
        }

        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(registerDTO.getCaptcha())) {
            return ResponseDTO.error("验证码错误");
        }
        // 检查用户名是否已存在
        ResponseDTO result = userService.register(registerDTO);
        if (result.getCode() == 200) {
            // 注册成功，清除验证码
            session.removeAttribute("captcha");
        }
        return result;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseDTO login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        // 验证验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(loginDTO.getCaptcha())) {
            return ResponseDTO.error("验证码错误");
        }
        // 登录
        ResponseDTO result = userService.login(loginDTO);
        if (result.getCode() == 200) {
            // 登录成功，清除验证码
            session.removeAttribute("captcha");
        }
        return result;
    }

    /**
     * 用户退出
     */
    @PostMapping("/logout")
    public ResponseDTO logout(@RequestHeader("Authorization") String token, HttpSession session) {
        session.invalidate();
        // 使该用户所有token失效
        return userService.logout(token);
    }

    /**
     * 验证Token有效性
     */
    @PostMapping("/validate-token")
    public ResponseDTO validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseDTO.error("缺少有效的认证令牌");
        }
        // 获取Token
        String token = authHeader.substring(7);
        return userService.validateToken(token);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ResponseDTO refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseDTO.error("缺少有效的认证令牌");
        }

        String token = authHeader.substring(7);
        return userService.refreshToken(token);
    }


    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    public ResponseDTO getCurrentUserInfo(@RequestAttribute Integer userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return ResponseDTO.success("获取成功", user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public ResponseDTO updateUserInfo(@RequestBody User user, @RequestAttribute Integer userId) {
        user.setUserId(userId);
        return userService.updateUserInfo(user);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseDTO updatePassword(@RequestParam String oldPassword,
                                      @RequestParam String newPassword,
                                      @RequestAttribute Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseDTO.error("用户不存在");
            }

            // 验证旧密码（使用盐值）
            if (!saltUtil.verifyPassword(oldPassword, user.getPassword(), user.getSalt())) {
                return ResponseDTO.error("旧密码错误");
            }

            // 验证新密码强度
            if (newPassword.length() < 6) {
                return ResponseDTO.error("新密码长度不能少于6位");
            }

            // 生成新盐值
            String newSalt = saltUtil.generateSalt();
            String encryptedNewPassword = saltUtil.encryptPassword(newPassword, newSalt);

            // 更新密码和盐值
            user.setPassword(encryptedNewPassword);
            user.setSalt(newSalt);
            int result = userMapper.updateUser(user);

            if (result > 0) {
                // 使该用户的所有token失效
                tokenService.deleteTokenByUserId(userId);
                return ResponseDTO.success("密码修改成功，请重新登录");
            } else {
                return ResponseDTO.error("密码修改失败");
            }

        } catch (Exception e) {
            return ResponseDTO.error("密码修改异常: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseDTO uploadAvatar(@RequestParam("avatar") MultipartFile file,
                                   @RequestAttribute Integer userId) {
        try {
            if (file.isEmpty()) {
                return ResponseDTO.error("请选择文件");
            }

            // 上传文件
            String avatarUrl = fileUploadService.uploadImage(file);

            // 更新用户头像信息
            User user = new User();
            user.setUserId(userId);
            user.setAvatar(avatarUrl);
            int result = userService.updateUserAvatar(user);

            if (result > 0) {
                return ResponseDTO.success("头像上传成功", avatarUrl);
            } else {
                // 如果更新数据库失败，则删除已上传的文件
                fileUploadService.deleteImage(avatarUrl);
                return ResponseDTO.error("头像上传失败");
            }
        } catch (Exception e) {
            return ResponseDTO.error("头像上传异常: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的徽章
     */
    @GetMapping("/badges")
    public ResponseDTO getUserBadges(@RequestAttribute Integer userId) {
        try {
            List<UserBadge> userBadges = badgeService.getUserBadges(userId);
            return ResponseDTO.success("获取成功", userBadges);
        } catch (Exception e) {
            log.error("获取用户徽章异常，用户ID: {}", userId, e);
            return ResponseDTO.error("获取用户徽章异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/activity-stats")
    public ResponseDTO getUserActivityStats(@RequestAttribute Integer userId,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Map<String, Object> stats = userService.getUserActivityStats(userId, startDate, endDate);
        return ResponseDTO.success("获取成功", stats);
    }

    /* 管理员：获取所有用户列表
     */
    @GetMapping("/admin/list")
    public ResponseDTO getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 隐藏密码
        users.forEach(user -> user.setPassword(null));
        return ResponseDTO.success("获取成功", users);
    }

    /**
     * 管理员：更新用户状态
     */
    @PutMapping("/admin/status")
    public ResponseDTO updateUserStatus(@RequestParam Integer userId, @RequestParam Integer status) {
        return userService.updateUserStatus(userId, status);
    }

    /**
     * 管理员：删除用户
     */
    @DeleteMapping("/admin/{userId}")
    public ResponseDTO deleteUser(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseDTO checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return ResponseDTO.success("查询成功", exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseDTO checkEmailExists(@RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        return ResponseDTO.success("查询成功", exists);
    }
}