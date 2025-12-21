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
    
    // 加载仪表板数据
    loadDashboardData();
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
                const userNameElement = document.getElementById('userName');
                if (userNameElement) {
                    userNameElement.textContent = result.data.nickname || result.data.username || '用户';
                }
                
                // 更新用户头像
                const userAvatarElements = document.querySelectorAll('.user-avatar');
                if (userAvatarElements.length > 0) {
                    const avatarUrl = result.data.avatar;
                    userAvatarElements.forEach(element => {
                        if (avatarUrl) {
                            element.src = avatarUrl;
                        } else {
                            element.src = '/images/avatar/avatar.png';
                        }
                    });
                }
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

async function loadDashboardData() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // 获取用户徽章统计
        const badgeStatsResponse = await fetch('/api/badge/my-points', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let totalPoints = 0;
        if (badgeStatsResponse.ok) {
            const badgeStatsResult = await badgeStatsResponse.json();
            if (badgeStatsResult.code === 200 && badgeStatsResult.data) {
                totalPoints = badgeStatsResult.data;
            }
        }
        
        // 显示总积分
        const totalPointsElement = document.getElementById('totalPoints');
        if (totalPointsElement) totalPointsElement.textContent = `${totalPoints} 分`;
        
        // 初始化图表
        initCharts();
        
        // 加载最近活动
        loadRecentActivities();
        
    } catch (error) {
        console.error('加载仪表板数据时出错:', error);
        showAlert('加载数据失败', 'error');
    }
}

function initCharts() {
    // 本周运动统计图
    const weeklyChart = document.getElementById('weeklyChart');
    if (weeklyChart) {
        const weeklyCtx = weeklyChart.getContext('2d');
        new Chart(weeklyCtx, {
            type: 'bar',
            data: {
                labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                datasets: [{
                    label: '运动时长(分钟)',
                    data: [30, 0, 45, 30, 60, 45, 30],
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
    
    // 运动类型分布图
    const typeChart = document.getElementById('typeChart');
    if (typeChart) {
        const typeCtx = typeChart.getContext('2d');
        new Chart(typeCtx, {
            type: 'doughnut',
            data: {
                labels: ['跑步', '游泳', '骑行', '瑜伽'],
                datasets: [{
                    data: [40, 25, 20, 15],
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0'
                    ]
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
}

async function loadRecentActivities() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }
        
        // 从API获取真实数据
        const response = await fetch('/api/behavior/record/user/' + getUserIdFromToken(token), {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let activities = [];
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                activities = Array.isArray(result.data) ? result.data : [];
                // 只显示最近5条记录
                activities = activities.slice(0, 5);
            }
        }
        
        // 渲染最近活动
        const recentActivitiesTable = document.querySelector('.table tbody');
        if (recentActivitiesTable) {
            recentActivitiesTable.innerHTML = '';
            
            if (activities.length === 0) {
                const row = document.createElement('tr');
                row.innerHTML = `<td colspan="4" class="text-center">暂无活动记录</td>`;
                recentActivitiesTable.appendChild(row);
            } else {
                activities.forEach(activity => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${activity.recordDate || activity.date || '未知日期'}</td>
                        <td><span class="activity-badge ${(activity.type || 'unknown').toLowerCase()}">${activity.typeName || activity.type || '未知类型'}</span></td>
                        <td>${activity.duration || 0}</td>
                        <td>${activity.content || activity.description || '无描述'}</td>
                    `;
                    recentActivitiesTable.appendChild(row);
                });
            }
        }
        
    } catch (error) {
        console.error('加载最近活动时出错:', error);
        showAlert('加载活动数据失败', 'error');
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