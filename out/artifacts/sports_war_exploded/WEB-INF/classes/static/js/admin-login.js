/**
 * 管理员登录页面JavaScript文件
 * 处理管理员登录逻辑
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化管理员登录页面
    initAdminLoginPage();
});

// 初始化管理员登录页面
function initAdminLoginPage() {
    // 绑定登录表单提交事件
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleAdminLogin);
    }
    
    // 绑定回车键登录
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                handleAdminLogin();
            }
        });
    }
    
    // 检查是否已登录
    checkAdminAuth();
}

// 检查管理员认证状态
async function checkAdminAuth() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            return; // 未登录，继续显示登录页面
        }
        
        const response = await fetch('/api/admin/validate-token', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                // 已登录，重定向到管理员首页
                window.location.href = '/admin';
            }
        } else {
            // Token无效，清除本地存储
            localStorage.removeItem('adminToken');
            sessionStorage.removeItem('adminToken');
        }
    } catch (error) {
        console.error('检查管理员认证状态失败:', error);
        // 清除本地存储
        localStorage.removeItem('adminToken');
        sessionStorage.removeItem('adminToken');
    }
}

// 处理管理员登录
async function handleAdminLogin(event) {
    if (event) {
        event.preventDefault();
    }
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;
    
    // 验证输入
    if (!username || !password) {
        showNotification('请输入用户名和密码', 'error');
        return;
    }
    
    try {
        // 显示加载状态
        const loginBtn = document.querySelector('button[type="submit"]');
        const originalText = loginBtn.innerHTML;
        loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>登录中...';
        loginBtn.disabled = true;
        
        // 发送登录请求
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        
        const result = await response.json();
        
        if (result.code === 200) {
            // 登录成功，存储令牌
            const token = result.data.token;
            if (rememberMe) {
                localStorage.setItem('adminToken', token);
            } else {
                sessionStorage.setItem('adminToken', token);
            }
            
            showNotification('登录成功', 'success');
            
            // 延迟跳转以显示成功消息
            setTimeout(() => {
                window.location.href = '/admin';
            }, 1000);
        } else {
            showNotification(result.message || '登录失败', 'error');
        }
    } catch (error) {
        console.error('登录请求失败:', error);
        showNotification('登录请求失败，请检查网络连接', 'error');
    } finally {
        // 恢复按钮状态
        const loginBtn = document.querySelector('button[type="submit"]');
        if (loginBtn) {
            loginBtn.innerHTML = '<i class="fas fa-sign-in-alt me-2"></i>登录';
            loginBtn.disabled = false;
        }
    }
}

// 显示通知
function showNotification(message, type = 'info') {
    // 移除现有的通知
    const existingNotifications = document.querySelectorAll('.login-notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show login-notification`;
    notification.style.cssText = 'margin-top: 15px;';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const form = document.querySelector('form');
    form.parentNode.insertBefore(notification, form.nextSibling);
    
    // 自动移除通知
    setTimeout(() => {
        if (notification.parentNode && notification.parentNode === form.parentNode) {
            notification.remove();
        }
    }, 5000);
}