document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    checkAuth();
    
    // 绑定退出登录事件
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }
    
    // 绑定表单提交事件
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateProfile();
        });
    }
    
    // 绑定更换头像事件
    const changeAvatarBtn = document.getElementById('changeAvatarBtn');
    const avatarInput = document.getElementById('avatarInput');
    if (changeAvatarBtn && avatarInput) {
        changeAvatarBtn.addEventListener('click', function() {
            avatarInput.click();
        });
        
        avatarInput.addEventListener('change', function(e) {
            previewAvatar(e);
        });
    }
    
    // 绑定修改密码事件
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const savePasswordBtn = document.getElementById('savePasswordBtn');
    if (changePasswordBtn) {
        changePasswordBtn.addEventListener('click', function() {
            const modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
            // 清空密码输入框
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmNewPassword').value = '';
            modal.show();
        });
    }
    
    if (savePasswordBtn) {
        savePasswordBtn.addEventListener('click', function() {
            changePassword();
        });
    }
    
    // 加载用户资料
    loadUserProfile();
});

async function checkAuth() {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/login';
        return;
    }
    
    try {
        const response = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            window.location.href = '/login';
        }
    } catch (error) {
        console.error('验证用户身份时出错:', error);
        window.location.href = '/login';
    }
}

async function logout() {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    
    try {
        if (token) {
            await fetch('/api/user/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        }
    } catch (error) {
        console.error('登出时出错:', error);
    } finally {
        // 清除本地存储的令牌
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
        // 跳转到登录页面
        window.location.href = '/login';
    }
}

async function loadUserProfile() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        const response = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('获取用户资料失败');
        }

        const result = await response.json();
        const userData = result.data;

        // 填充表单数据
        const usernameEl = document.getElementById('username');
        if (usernameEl) usernameEl.value = userData.username || '';
        
        const emailEl = document.getElementById('email');
        if (emailEl) emailEl.value = userData.email || '';
        
        const nicknameEl = document.getElementById('nickname');
        if (nicknameEl) nicknameEl.value = userData.nickname || '';
        
        const birthdayEl = document.getElementById('birthday');
        if (birthdayEl) birthdayEl.value = userData.birthday || '';
        
        const genderEl = document.getElementById('gender');
        if (genderEl) genderEl.value = userData.gender || '';
        
        const heightEl = document.getElementById('height');
        if (heightEl) heightEl.value = userData.height || '';
        
        const weightEl = document.getElementById('weight');
        if (weightEl) weightEl.value = userData.weight || '';
        
        const userNameEl = document.getElementById('userName');
        if (userNameEl) userNameEl.textContent = userData.nickname || userData.username || '';

        // 设置头像
        const profileImage = document.getElementById('profileImage');
        const headerAvatar = document.getElementById('headerAvatar');
        const userAvatarElements = document.querySelectorAll('.user-avatar');
        if (userData.avatar) {
            if (profileImage) profileImage.src = userData.avatar;
            if (headerAvatar) headerAvatar.src = userData.avatar;
            userAvatarElements.forEach(el => el.src = userData.avatar);
        } else {
            if (profileImage) profileImage.src = '/images/avatar.png';
            if (headerAvatar) headerAvatar.src = '/images/avatar.png';
            userAvatarElements.forEach(el => el.src = '/images/avatar.png');
        }
        
        // 更新头部信息
        const headerUsername = document.getElementById('headerUsername');
        if (headerUsername) headerUsername.textContent = userData.nickname || userData.username || '用户';
        
        // 注册时间
        const registerDate = document.getElementById('registerDate');
        if (userData.registerTime && registerDate) {
            const regDate = new Date(userData.registerTime);
            registerDate.textContent = regDate.toLocaleDateString('zh-CN');
        }
        
        // TODO: 以下统计数据需要从后端API获取真实数据
        // 临时使用默认值
        const monthDuration = document.getElementById('monthDuration');
        if (monthDuration) monthDuration.textContent = '0';
        
        const monthCount = document.getElementById('monthCount');
        if (monthCount) monthCount.textContent = '0';
        
        const badgeCount = document.getElementById('badgeCount');
        if (badgeCount) badgeCount.textContent = '0';
        
        const streakDays = document.getElementById('streakDays');
        if (streakDays) streakDays.textContent = '0';
        
    } catch (error) {
        console.error('加载用户资料时出错:', error);
        showAlert('加载用户资料失败: ' + error.message, 'error');
    }
}

async function updateProfile() {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    // 获取表单数据
    const email = document.getElementById('email')?.value || '';
    const nickname = document.getElementById('nickname')?.value || '';
    const birthday = document.getElementById('birthday')?.value || null;
    const gender = document.getElementById('gender')?.value || null;
    const height = document.getElementById('height')?.value ? parseFloat(document.getElementById('height').value) : null;
    const weight = document.getElementById('weight')?.value ? parseFloat(document.getElementById('weight').value) : null;

    const formData = {
        email: email,
        nickname: nickname,
        birthday: birthday,
        gender: gender,
        height: height,
        weight: weight
    };
    
    try {
        const response = await fetch('/api/user/info', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(formData)
        });

        // 即使响应不是2xx，我们也想查看响应内容
        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (response.ok && result.code === 200) {
            showAlert('资料更新成功', 'success');
            // 重新加载用户资料以更新界面上的显示
            loadUserProfile();
        } else {
            const errorMessage = result.message || '未知错误';
            throw new Error(errorMessage);
        }
        
    } catch (error) {
        console.error('保存用户资料时出错:', error);
        showAlert('资料更新失败: ' + error.message, 'error');
    }
}

function previewAvatar(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const profileImage = document.getElementById('profileImage');
            if (profileImage) {
                profileImage.src = e.target.result;
            }
            
            // 自动上传头像
            uploadAvatar(file);
        };
        reader.readAsDataURL(file);
    }
}

async function uploadAvatar(file) {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    const formData = new FormData();
    formData.append('avatar', file);

    try {
        const response = await fetch('/api/user/avatar', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (response.ok && result.code === 200) {
            showAlert('头像上传成功', 'success');
            // 更新所有头像显示
            const avatarUrl = result.data;
            const profileImage = document.getElementById('profileImage');
            const headerAvatar = document.getElementById('headerAvatar');
            const userAvatarElements = document.querySelectorAll('.user-avatar');
            
            if (profileImage) profileImage.src = avatarUrl;
            if (headerAvatar) headerAvatar.src = avatarUrl;
            userAvatarElements.forEach(el => el.src = avatarUrl);
        } else {
            const errorMessage = result.message || '未知错误';
            throw new Error(errorMessage);
        }
    } catch (error) {
        console.error('上传头像时出错:', error);
        showAlert('头像上传失败: ' + error.message, 'error');
        
        // 恢复原来的头像
        loadUserProfile();
    }
}

async function changePassword() {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    const currentPassword = document.getElementById('currentPassword')?.value || '';
    const newPassword = document.getElementById('newPassword')?.value || '';
    const confirmNewPassword = document.getElementById('confirmNewPassword')?.value || '';
    
    if (!currentPassword || !newPassword || !confirmNewPassword) {
        showAlert('请填写所有密码字段', 'warning');
        return;
    }
    
    if (newPassword !== confirmNewPassword) {
        showAlert('新密码与确认密码不一致', 'warning');
        return;
    }
    
    if (newPassword.length < 6) {
        showAlert('新密码长度不能少于6位', 'warning');
        return;
    }
    
    try {
        const response = await fetch('/api/user/password', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': `Bearer ${token}`
            },
            body: new URLSearchParams({
                oldPassword: currentPassword,
                newPassword: newPassword
            })
        });

        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (response.ok && result.code === 200) {
            showAlert('密码修改成功，请重新登录', 'success');
            
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
            if (modal) {
                modal.hide();
            }
            
            // 延迟跳转到登录页面
            setTimeout(() => {
                localStorage.removeItem('accessToken');
                sessionStorage.removeItem('accessToken');
                window.location.href = '/login';
            }, 1500);
        } else {
            const errorMessage = result.message || '未知错误';
            throw new Error(errorMessage);
        }
        
    } catch (error) {
        console.error('修改密码时出错:', error);
        showAlert('密码修改失败: ' + error.message, 'error');
    }
}

function showAlert(message, type) {
    // 创建提示元素
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    alertDiv.style.top = '20px';
    alertDiv.style.right = '20px';
    alertDiv.style.zIndex = '9999';
    
    // 添加到页面
    document.body.appendChild(alertDiv);
    
    // 3秒后自动移除
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.parentNode.removeChild(alertDiv);
        }
    }, 3000);
}