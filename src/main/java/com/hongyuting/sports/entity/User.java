package com.hongyuting.sports.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    private String nickname;

    @Column(unique = true)
    private String email;

    @Column(name = "wx_openid")
    private String wxOpenid;

    private String avatar;

    private String bio;

    @Column(name = "user_status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer userStatus;

    @Column(name = "register_time", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime registerTime;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
}
