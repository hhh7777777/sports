document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    CommonUtils.checkUserAuth();
    
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
            // 清空密码输入框（当前密码字段已隐藏，无需清空）
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
    
    // 绑定退出登录事件
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }
    
    // 加载用户资料
    loadUserProfile();
});

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
            // 清除无效的token
            localStorage.removeItem('accessToken');
            sessionStorage.removeItem('accessToken');
            window.location.href = '/login';
            return;
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
            if (profileImage) profileImage.src = '/images/avatar/avatar.png';
            if (headerAvatar) headerAvatar.src = '/images/avatar/avatar.png';
            userAvatarElements.forEach(el => el.src = '/images/avatar/avatar.png');
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
        
        // 获取真实统计数据
        const userId = CommonUtils.getUserIdFromToken(token);
        
        // 获取本月运动统计
        const startDate = new Date();
        startDate.setDate(1); // 本月第一天
        const endDate = new Date();
        
        const activityStatsResponse = await fetch(`/api/user/activity-stats?startDate=${startDate.toISOString().split('T')[0]}&endDate=${endDate.toISOString().split('T')[0]}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let monthDuration = 0;
        let monthCount = 0;
        if (activityStatsResponse.ok) {
            const activityStatsResult = await activityStatsResponse.json();
            if (activityStatsResult.code === 200 && activityStatsResult.data) {
                monthDuration = activityStatsResult.data.totalDuration || 0;
                
                // 计算本月运动次数
                if (activityStatsResult.data.typeDistribution) {
                    monthCount = activityStatsResult.data.typeDistribution.reduce((total, item) => {
                        return total + (item.recordCount || 0);
                    }, 0);
                }
            }
        }
        
        // 获取徽章统计
        const badgeStatsResponse = await fetch('/api/user/badges', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let badgeCount = 0;
        if (badgeStatsResponse.ok) {
            const badgeStatsResult = await badgeStatsResponse.json();
            if (badgeStatsResult.code === 200 && badgeStatsResult.data) {
                // 只统计已获得且进度为100%的徽章
                badgeCount = Array.isArray(badgeStatsResult.data) ? 
                    badgeStatsResult.data.filter(badge => badge.achieved && badge.progress >= 100).length : 0;
            }
        }
        
        // 获取连续打卡天数
        const streakDaysResponse = await fetch('/api/user/streak-days', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let streakDays = 0;
        if (streakDaysResponse.ok) {
            const streakDaysResult = await streakDaysResponse.json();
            if (streakDaysResult.code === 200 && streakDaysResult.data) {
                streakDays = streakDaysResult.data.streakDays || 0;
            }
        }
        
        // 更新统计数据
        const monthDurationEl = document.getElementById('monthDuration');
        if (monthDurationEl) monthDurationEl.textContent = monthDuration;
        
        const monthCountEl = document.getElementById('monthCount');
        if (monthCountEl) monthCountEl.textContent = monthCount;
        
        const badgeCountEl = document.getElementById('badgeCount');
        if (badgeCountEl) badgeCountEl.textContent = badgeCount;
        
        const streakDaysEl = document.getElementById('streakDays');
        if (streakDaysEl) streakDaysEl.textContent = streakDays;
        
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

        if (!response.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (response.status === 401) {
                localStorage.removeItem('accessToken');
                sessionStorage.removeItem('accessToken');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 即使响应不是2xx，我们也想查看响应内容
        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (result.code === 200) {
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

        if (!response.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (response.status === 401) {
                localStorage.removeItem('accessToken');
                sessionStorage.removeItem('accessToken');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (result.code === 200) {
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

    const newPassword = document.getElementById('newPassword')?.value || '';
    const confirmNewPassword = document.getElementById('confirmNewPassword')?.value || '';
    
    if (!newPassword || !confirmNewPassword) {
        showAlert('请填写新密码和确认密码', 'warning');
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
        const response = await fetch('/api/user/password-new', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': `Bearer ${token}`
            },
            body: new URLSearchParams({
                newPassword: newPassword
            })
        });

        if (!response.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (response.status === 401) {
                localStorage.removeItem('accessToken');
                sessionStorage.removeItem('accessToken');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const resultText = await response.text();
        let result;
        try {
            result = JSON.parse(resultText);
        } catch (e) {
            throw new Error(`服务器响应不是有效的JSON格式: ${resultText}`);
        }

        if (result.code === 200) {
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

async function logout() {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
    if (!token) {
        // 如果没有token，直接跳转到登录页
        window.location.href = '/login';
        return;
    }

    try {
        // 向后端发送登出请求
        const response = await fetch('/api/user/logout', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        // 无论后端登出请求是否成功，都清除本地认证信息并跳转
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
        window.location.href = '/login';
    } catch (error) {
        console.error('登出时出错:', error);
        // 出错时也清除本地认证信息并跳转
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
        window.location.href = '/login';
    }
}

// 辅助函数：从token中提取用户ID
function getUserIdFromToken(token) {
    try {
        const base64Payload = token.split('.')[1];
        const payload = JSON.parse(atob(base64Payload));
        return payload.userId || payload.sub || payload.id;
    } catch (e) {
        console.error('解析token失败:', e);
        return null;
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