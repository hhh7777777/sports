document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    checkAuth();
    
    // 绑定退出登录事件
    document.getElementById('logoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        logout();
    });
    
    // 绑定表单提交事件
    document.getElementById('profileForm').addEventListener('submit', function(e) {
        e.preventDefault();
        updateProfile();
    });
    
    // 绑定更换头像事件
    document.getElementById('changeAvatarBtn').addEventListener('click', function() {
        document.getElementById('avatarInput').click();
    });
    
    document.getElementById('avatarInput').addEventListener('change', function(e) {
        changeAvatar(e);
    });
    
    // 绑定修改密码事件
    document.getElementById('changePasswordBtn').addEventListener('click', function() {
        const modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
        modal.show();
    });
    
    document.getElementById('savePasswordBtn').addEventListener('click', function() {
        changePassword();
    });
    
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
        // 模拟用户数据
        const userData = {
            username: '张三',
            email: 'zhangsan@example.com',
            nickname: '运动达人',
            birthday: '1990-01-01',
            gender: 'male',
            height: 175,
            weight: 70
        };
        
        // 填充表单数据
        document.getElementById('username').value = userData.username;
        document.getElementById('email').value = userData.email;
        document.getElementById('nickname').value = userData.nickname;
        document.getElementById('birthday').value = userData.birthday;
        document.getElementById('gender').value = userData.gender;
        document.getElementById('height').value = userData.height;
        document.getElementById('weight').value = userData.weight;
        document.getElementById('userName').textContent = userData.nickname || userData.username;
        
    } catch (error) {
        console.error('加载用户资料时出错:', error);
        showAlert('加载用户资料失败', 'error');
    }
}

async function updateProfile() {
    const formData = {
        email: document.getElementById('email').value,
        nickname: document.getElementById('nickname').value,
        birthday: document.getElementById('birthday').value,
        gender: document.getElementById('gender').value,
        height: document.getElementById('height').value,
        weight: document.getElementById('weight').value
    };
    
    try {
        // 模拟保存用户资料
        console.log('保存用户资料:', formData);
        showAlert('资料更新成功', 'success');
        
        // 在实际应用中，这里应该调用API保存用户资料
        // await apiService.request('/api/user/profile', {
        //     method: 'PUT',
        //     body: JSON.stringify(formData)
        // });
        
    } catch (error) {
        console.error('保存用户资料时出错:', error);
        showAlert('资料更新失败', 'error');
    }
}

function changeAvatar(event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const profileImage = document.getElementById('profileImage');
            if (profileImage) {
                profileImage.src = e.target.result;
            }
        };
        reader.readAsDataURL(file);
    }
}

async function changePassword() {
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmNewPassword = document.getElementById('confirmNewPassword').value;
    
    if (!currentPassword || !newPassword || !confirmNewPassword) {
        showAlert('请填写所有密码字段', 'warning');
        return;
    }
    
    if (newPassword !== confirmNewPassword) {
        showAlert('新密码与确认密码不一致', 'warning');
        return;
    }
    
    try {
        // 模拟修改密码
        console.log('修改密码');
        showAlert('密码修改成功', 'success');
        
        // 在实际应用中，这里应该调用API修改密码
        // await apiService.request('/api/user/password', {
        //     method: 'PUT',
        //     body: JSON.stringify({
        //         currentPassword,
        //         newPassword
        //     })
        // });
        
        // 关闭模态框
        const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
        modal.hide();
        
    } catch (error) {
        console.error('修改密码时出错:', error);
        showAlert('密码修改失败', 'error');
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
        alertDiv.remove();
    }, 3000);
}