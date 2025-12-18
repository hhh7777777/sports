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
            document.getElementById('userName').textContent = result.data.username;
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
        // 获取筛选条件
        const activityType = document.getElementById('activityType').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        // 模拟活动数据
        const activities = [
            {
                id: 1,
                type: 'running',
                typeName: '跑步',
                date: '2024-01-15',
                duration: 30,
                distance: 5.0,
                calories: 300,
                description: '晨跑5公里'
            },
            {
                id: 2,
                type: 'swimming',
                typeName: '游泳',
                date: '2024-01-14',
                duration: 45,
                distance: 1.5,
                calories: 400,
                description: '游泳训练'
            },
            {
                id: 3,
                type: 'cycling',
                typeName: '骑行',
                date: '2024-01-13',
                duration: 60,
                distance: 20.0,
                calories: 500,
                description: '周末骑行'
            }
        ];
        
        // 根据筛选条件过滤活动
        let filteredActivities = [...activities];
        if (activityType) {
            filteredActivities = filteredActivities.filter(a => a.type === activityType);
        }
        
        // 渲染活动列表
        renderActivities(filteredActivities);
        
    } catch (error) {
        console.error('加载活动时出错:', error);
        showAlert('加载活动失败', 'error');
    }
}

function renderActivities(activities) {
    const container = document.getElementById('activityList');
    container.innerHTML = '';
    
    if (activities.length === 0) {
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
        activityCol.innerHTML = `
            <div class="card activity-card h-100">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h5 class="card-title">
                                <i class="fas fa-${activity.type === 'running' ? 'running' : 
                                                  activity.type === 'swimming' ? 'swimmer' : 
                                                  activity.type === 'cycling' ? 'bicycle' : 'spa'} me-2 ${
                                                  activity.type === 'running' ? 'running' : 
                                                  activity.type === 'swimming' ? 'swimming' : 
                                                  activity.type === 'cycling' ? 'cycling' : 'yoga'}"></i>
                                ${activity.typeName}
                            </h5>
                            <p class="card-text text-muted">${activity.description}</p>
                        </div>
                        <button class="btn btn-sm btn-outline-danger delete-activity" data-id="${activity.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                    <div class="row mt-3">
                        <div class="col-6">
                            <small class="text-muted">日期</small>
                            <p class="mb-1">${activity.date}</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">时长</small>
                            <p class="mb-1">${activity.duration} 分钟</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">距离</small>
                            <p class="mb-1">${activity.distance} km</p>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">消耗</small>
                            <p class="mb-1">${activity.calories} 卡路里</p>
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
        // 模拟添加活动
        console.log('添加活动:', { activityType, activityDate, activityDuration, activityDescription });
        showAlert('活动添加成功', 'success');
        
        // 在实际应用中，这里应该调用API添加活动
        // await apiService.request('/api/user/activities', {
        //     method: 'POST',
        //     body: JSON.stringify({
        //         type: activityType,
        //         date: activityDate,
        //         duration: parseInt(activityDuration),
        //         description: activityDescription
        //     })
        // });
        
        // 关闭模态框
        const modal = bootstrap.Modal.getInstance(document.getElementById('addActivityModal'));
        modal.hide();
        
        // 重新加载活动列表
        loadActivities();
        
    } catch (error) {
        console.error('添加活动时出错:', error);
        showAlert('活动添加失败', 'error');
    }
}

async function deleteActivity(activityId) {
    if (!confirm('确定要删除这条运动记录吗？')) {
        return;
    }
    
    try {
        // 模拟删除活动
        console.log('删除活动ID:', activityId);
        showAlert('活动删除成功', 'success');
        
        // 在实际应用中，这里应该调用API删除活动
        // await apiService.request(`/api/user/activities/${activityId}`, {
        //     method: 'DELETE'
        // });
        
        // 重新加载活动列表
        loadActivities();
        
    } catch (error) {
        console.error('删除活动时出错:', error);
        showAlert('活动删除失败', 'error');
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