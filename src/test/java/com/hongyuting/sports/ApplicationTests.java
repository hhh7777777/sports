package com.hongyuting.sports;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文加载
        System.out.println("Spring Boot 应用启动成功");
    }

    @Test
    void exampleTest() {
        // 测试方法
    }
}

//本次考核项目统一采用以下技术栈与开发标准。所有小组必须严格按照以下技术要求进行项目开发，如无特殊情况不得擅自更换技术，否则将酌情扣分。
//     ①　前端：Thymeleaf（html）
//     ②　后端：Spring Boot + Maven + MyBatis
//     ③　数据库：MySQL
//（一）用户端功能：
//        ① 用户注册、登录、验证码验证、安全退出。
 //       ② 新增行为记录（如阅读记录、学习记录、运动记录…主题自定），需要上传记录图片凭证。
 //       ③ 查看历史记录列表。
  //      ④ 系统根据完成度自动授予成就徽章，并展示“徽章墙”。
  //      ⑤ 查看周/月记录统计情况。
  //      （二）管理员端功能：
  //      ① 管理员登录与后台管理功能。
  //      ② 行为类型管理（新增、删除、修改、查询）。
  //      ③ 徽章管理（上传图标、设置条件、CRUD）。
//④ 查看用户行为记录与活跃度排行。
 //       ⑤ 查看用户活跃度统计。
 //       ⑥ 后台操作日志记录。（如敏感操作记录等）



