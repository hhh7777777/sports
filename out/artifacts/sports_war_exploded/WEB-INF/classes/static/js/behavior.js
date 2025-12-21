document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    checkAuth();
    
    // 绑定退出登录事件
    document.getElementById('logoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        logout();
    });
    
    // 初始化日期选择器
    initDatePickers();
    
    // 绑定筛选表单提交事件
    document.getElementById('filterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        loadActivities();
    });
    
    // 绑定添加活动按钮事件
    document.getElementById('saveActivityBtn').addEventListener('click', function() {
        addActivity();
    });
    
    // 加载活动数据
    loadActivities();
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
        } else {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                // 更新用户名，优先显示昵称，其次显示用户名
                const userNameElement = document.getElementById('userName');
                if (userNameElement) {
                    userNameElement.textContent = result.data.nickname || result.data.username || '用户';
                }
                
                // 更新用户头像
                const userAvatarElements = document.querySelectorAll('.user-avatar');
                if (userAvatarElements.length > 0) {
                    const avatarUrl = result.data.avatar;
                    userAvatarElements.forEach(element => {
                        if (avatarUrl && avatarUrl.trim() !== '') {
                            element.src = avatarUrl;
                        } else {
                            element.src = '/images/avatar/avatar.png';
                        }
                    });
                }
                
                // 同时更新下拉菜单中的用户名显示
                const dropdownUserNameElements = document.querySelectorAll('.dropdown .d-none.d-md-inline');
                dropdownUserNameElements.forEach(element => {
                    element.textContent = result.data.nickname || result.data.username || '用户';
                });
            } else {
                window.location.href = '/login';
            }
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

function initDatePickers() {
    // 初始化日期选择器
    flatpickr("#startDate, #endDate, #activityDate", {
        locale: "zh",
        dateFormat: "Y-m-d",
    });
}

async function loadActivities() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // 获取用户ID
        let userId = getUserIdFromToken(token);
        if (!userId) {
            // 如果无法从token中解析用户ID，则通过用户信息API获取
            const userInfoResponse = await fetch('/api/user/profile', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (userInfoResponse.ok) {
                const userInfoResult = await userInfoResponse.json();
                if (userInfoResult.code === 200 && userInfoResult.data) {
                    userId = userInfoResult.data.userId;
                }
            }
        }
        
        if (!userId) {
            throw new Error('无法获取用户ID');
        }
        
        // 从API获取真实数据
        const response = await fetch(`/api/behavior/record/user/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            throw new Error('获取运动记录失败');
        }
        
        const result = await response.json();
        let activities = [];
        
        // 确保正确处理后端返回的数据格式
        if (result.code === 200 && result.data) {
            activities = Array.isArray(result.data) ? result.data : [];
        }
        
        // 渲染活动列表
        renderActivities(activities);
        
    } catch (error) {
        console.error('加载活动时出错:', error);
        showAlert('加载活动失败: ' + error.message, 'error');
    }
}

function renderActivities(activities) {
    const container = document.getElementById('activityList');
    container.innerHTML = '';
    
    if (!Array.isArray(activities) || activities.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-5">
                    <i class="fas fa-running fa-3x text-muted mb-3"></i>
                    <p class="text-muted">暂无运动记录</p>
                </div>
            </div>
        `;
        return;
    }
    
    activities.forEach(activity => {
        const activityCol = document.createElement('div');
        activityCol.className = 'col-lg-6 mb-4';
        
        // 确定活动类型和图标
        const activityType = activity.type || 'running';
        const activityTypeName = activity.typeName || activity.type || '未知类型';
        const activityContent = activity.content || activity.description || '无描述';
        const activityDate = activity.recordDate || activity.date || '未知日期';
        const activityDuration = activity.duration || 0;
        const activityDistance = activity.distance || 0;
        const activityCalories = activity.calories || 0;
        
        activityCol.innerHTML = `
            <div class="card activity-card h-100">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="card-title">
                                <i class="fas fa-${activityType} me-2"></i>
                                ${activityTypeName}
                            </h5>
                            <p class="card-text text-muted">${activityContent}</p>
                        </div>
                        <button class="btn btn-sm btn-outline-danger delete-activity" data-id="${activity.recordId || activity.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                    <div class="row mt-3">
                        <div class="col-6">
                            <small class="text-muted">日期</small>
                            <p class="mb-1">${activityDate}</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">时长</small>
                            <p class="mb-1">${activityDuration} 分钟</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">距离</small>
                            <p class="mb-1">${activityDistance} km</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">消耗</small>
                            <p class="mb-1">${activityCalories} 卡路里</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.appendChild(activityCol);
    });
    
    // 绑定删除事件
    document.querySelectorAll('.delete-activity').forEach(button => {
        button.addEventListener('click', function() {
            const activityId = this.getAttribute('data-id');
            deleteActivity(activityId);
        });
    });
}

async function addActivity() {
    const activityType = document.getElementById('newActivityType').value;
    const activityDate = document.getElementById('activityDate').value;
    const activityDuration = document.getElementById('activityDuration').value;
    const activityDescription = document.getElementById('activityDescription').value;
    
    if (!activityType || !activityDate || !activityDuration) {
        showAlert('请填写必填字段', 'warning');
        return;
    }
    
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // 调用API添加活动
        const response = await fetch('/api/behavior/record', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                typeId: getActivityTypeId(activityType),
                recordDate: activityDate,
                duration: parseInt(activityDuration),
                content: activityDescription
            })
        });
        
        const result = await response.json();
        if (result.code === 200) {
            showAlert('活动添加成功', 'success');
            
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('addActivityModal'));
            modal.hide();
            
            // 重新加载活动列表
            loadActivities();
        } else {
            showAlert('活动添加失败: ' + (result.message || '未知错误'), 'error');
        }
        
    } catch (error) {
        console.error('添加活动时出错:', error);
        showAlert('活动添加失败: ' + error.message, 'error');
    }
}

async function deleteActivity(activityId) {
    if (!confirm('确定要删除这条运动记录吗？')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // 调用API删除活动
        const response = await fetch(`/api/behavior/record/${activityId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        // 注意：DELETE请求可能不会总是返回JSON格式的响应
        let result = {};
        try {
            result = await response.json();
        } catch (e) {
            // 如果无法解析JSON，则基于HTTP状态码判断结果
            result.code = response.ok ? 200 : 500;
            result.message = response.ok ? '删除成功' : '删除失败';
        }
        
        if (result.code === 200) {
            showAlert('活动删除成功', 'success');
            // 重新加载活动列表
            loadActivities();
        } else {
            showAlert('活动删除失败: ' + (result.message || '未知错误'), 'error');
        }
        
    } catch (error) {
        console.error('删除活动时出错:', error);
        showAlert('活动删除失败: ' + error.message, 'error');
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
        // 如果无法解析token，则尝试从用户信息API获取用户ID
        return null;
    }
}

// 辅助函数：根据活动类型获取类型ID
function getActivityTypeId(activityType) {
    // 这里应该从后端获取真实的类型ID映射
    // 目前使用硬编码的映射作为临时解决方案
    const typeMap = {
        'running': 1,
        'swimming': 2,
        'cycling': 3
    };
    return typeMap[activityType] || 1;
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