package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.LoginDTO;
import com.hongyuting.sports.dto.RegisterDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.User;
import com.hongyuting.sports.mapper.UserMapper;
import com.hongyuting.sports.service.TokenService;
import com.hongyuting.sports.service.UserService;
import com.hongyuting.sports.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    private TokenService tokenService;

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

        return userService.register(registerDTO);
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
    @GetMapping("/info")
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
            if (!PasswordUtil.validatePassword(oldPassword, user.getSalt(), user.getPassword())) {
                return ResponseDTO.error("旧密码错误");
            }

            // 验证新密码强度
            if (newPassword.length() < 6) {
                return ResponseDTO.error("新密码长度不能少于6位");
            }

            // 生成新盐值
            String newSalt = PasswordUtil.generateSalt();
            String encryptedNewPassword = PasswordUtil.encryptPassword(newPassword, newSalt);

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