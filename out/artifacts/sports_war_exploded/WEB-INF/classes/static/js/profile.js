/**
 * 个人资料页面JavaScript文件
 * 处理用户资料的加载和更新
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化个人资料页面
    initProfilePage();
});

// 初始化个人资料页面
async function initProfilePage() {
    try {
        // 加载用户信息
        await loadUserProfile();
        
        // 绑定事件
        bindProfileEvents();
    } catch (error) {
        console.error('初始化个人资料页面失败:', error);
    }
}

// 加载用户信息
async function loadUserProfile() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            // 未登录用户，重定向到登录页面
            window.location.href = '/login';
            return;
        }

        const response = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const user = result.data;
                
                // 更新页面上的用户信息
                document.getElementById('userName').textContent = user.nickname || user.username;
                document.getElementById('headerUsername').textContent = user.nickname || user.username;
                
                // 更新头像
                const avatarElements = document.querySelectorAll('.user-avatar, .profile-img');
                avatarElements.forEach(img => {
                    if (user.avatar) {
                        img.src = user.avatar;
                    } else {
                        img.src = '/images/avatar/avatar.png';
                    }
                });
                
                // 更新表单字段
                document.getElementById('username').value = user.username || '';
                document.getElementById('email').value = user.email || '';
                document.getElementById('nickname').value = user.nickname || '';
                
                if (user.birthday) {
                    // 将日期格式转换为 YYYY-MM-DD
                    const birthday = new Date(user.birthday);
                    const formattedDate = birthday.toISOString().split('T')[0];
                    document.getElementById('birthday').value = formattedDate;
                }
                
                document.getElementById('gender').value = user.gender || '';
                document.getElementById('height').value = user.height || '';
                document.getElementById('weight').value = user.weight || '';
                
                // 更新注册信息
                if (user.registerTime) {
                    const registerDate = new Date(user.registerTime);
                    document.getElementById('registerDate').textContent = 
                        registerDate.toLocaleDateString('zh-CN');
                }
                
                // 加载统计数据
                await loadUserStats(user.userId);
            } else {
                // Token无效，重定向到登录页面
                window.location.href = '/login';
            }
        } else {
            // Token验证失败，重定向到登录页面
            window.location.href = '/login';
        }
    } catch (error) {
        console.error('加载用户信息失败:', error);
        window.location.href = '/login';
    }
}

// 加载用户统计数据
async function loadUserStats(userId) {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            return;
        }

        // 获取本月统计数据
        const statsResponse = await fetch(`/api/user/stats/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (statsResponse.ok) {
            const statsResult = await statsResponse.json();
            if (statsResult.code === 200 && statsResult.data) {
                const stats = statsResult.data;
                
                // 更新统计数据
                document.getElementById('monthDuration').textContent = stats.monthDuration || 0;
                document.getElementById('monthCount').textContent = stats.monthCount || 0;
                document.getElementById('badgeCount').textContent = stats.badgeCount || 0;
                document.getElementById('streakDays').textContent = stats.streakDays || 0;
            }
        }
    } catch (error) {
        console.error('加载用户统计数据失败:', error);
    }
}

// 绑定事件
function bindProfileEvents() {
    // 表单提交事件
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', handleProfileUpdate);
    }
    
    // 退出按钮事件
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    // 更换头像按钮事件
    const changeAvatarBtn = document.getElementById('changeAvatarBtn');
    const avatarInput = document.getElementById('avatarInput');
    if (changeAvatarBtn && avatarInput) {
        changeAvatarBtn.addEventListener('click', () => {
            avatarInput.click();
        });
        
        avatarInput.addEventListener('change', handleAvatarChange);
    }
    
    // 修改密码按钮事件
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    if (changePasswordBtn) {
        changePasswordBtn.addEventListener('click', showChangePasswordModal);
    }
    
    // 保存密码按钮事件
    const savePasswordBtn = document.getElementById('savePasswordBtn');
    if (savePasswordBtn) {
        savePasswordBtn.addEventListener('click', handleChangePassword);
    }
}

// 处理资料更新
async function handleProfileUpdate(event) {
    event.preventDefault();
    
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // 获取表单数据
        const formData = {
            email: document.getElementById('email').value,
            nickname: document.getElementById('nickname').value,
            birthday: document.getElementById('birthday').value,
            gender: document.getElementById('gender').value,
            height: parseFloat(document.getElementById('height').value) || null,
            weight: parseFloat(document.getElementById('weight').value) || null
        };

        const response = await fetch('/api/user/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();
        if (result.code === 200) {
            showNotification('资料更新成功', 'success');
        } else {
            showNotification(result.message || '资料更新失败', 'error');
        }
    } catch (error) {
        console.error('更新资料失败:', error);
        showNotification('资料更新失败', 'error');
    }
}

// 处理头像更改
async function handleAvatarChange(event) {
    const file = event.target.files[0];
    if (!file) return;

    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // 验证文件类型
        const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
        if (!validTypes.includes(file.type)) {
            showNotification('请选择有效的图片文件(jpg, png, gif)', 'error');
            return;
        }

        // 验证文件大小（限制为5MB）
        if (file.size > 5 * 1024 * 1024) {
            showNotification('图片大小不能超过5MB', 'error');
            return;
        }

        // 创建FormData对象
        const formData = new FormData();
        formData.append('avatar', file);

        const response = await fetch('/api/user/update-avatar', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        const result = await response.json();
        if (result.code === 200) {
            // 更新页面上的头像
            const reader = new FileReader();
            reader.onload = function(e) {
                const avatarElements = document.querySelectorAll('.user-avatar, .profile-img');
                avatarElements.forEach(img => {
                    img.src = e.target.result;
                });
            };
            reader.readAsDataURL(file);
            
            showNotification('头像更新成功', 'success');
        } else {
            showNotification(result.message || '头像更新失败', 'error');
        }
    } catch (error) {
        console.error('更新头像失败:', error);
        showNotification('头像更新失败', 'error');
    }
}

// 显示修改密码模态框
function showChangePasswordModal() {
    const modalElement = document.getElementById('changePasswordModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
}

// 处理密码更改
async function handleChangePassword() {
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmNewPassword = document.getElementById('confirmNewPassword').value;

    // 验证密码
    if (newPassword !== confirmNewPassword) {
        showNotification('新密码和确认密码不匹配', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showNotification('密码长度不能少于6位', 'error');
        return;
    }

    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        const formData = {
            currentPassword,
            newPassword
        };

        const response = await fetch('/api/user/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();
        if (result.code === 200) {
            showNotification('密码修改成功', 'success');
            
            // 关闭模态框
            const modalElement = document.getElementById('changePasswordModal');
            if (modalElement) {
                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) {
                    modal.hide();
                }
            }
            
            // 清空表单
            document.getElementById('changePasswordForm').reset();
        } else {
            showNotification(result.message || '密码修改失败', 'error');
        }
    } catch (error) {
        console.error('修改密码失败:', error);
        showNotification('密码修改失败', 'error');
    }
}

// 处理退出登录
async function handleLogout() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (token) {
            await fetch('/api/user/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        }
    } catch (error) {
        console.error('退出时出错:', error);
    } finally {
        // 清除本地存储的令牌
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
        // 重定向到登录页面
        window.location.href = '/login';
    }
}

// 显示通知
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
    notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(notification);
    
    // 自动移除通知
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}
