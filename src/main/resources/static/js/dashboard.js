document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    checkAuth();
    
    // 绑定退出登录事件
    document.getElementById('logoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        logout();
    });
    
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

async function loadDashboardData() {
    try {
        // 模拟统计数据
        document.getElementById('totalDuration').textContent = '150 分钟';
        document.getElementById('badgeCount').textContent = '5 个';
        document.getElementById('streakDays').textContent = '7 天';
        document.getElementById('totalPoints').textContent = '350 分';
        
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
    const weeklyCtx = document.getElementById('weeklyChart').getContext('2d');
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
    
    // 运动类型分布图
    const typeCtx = document.getElementById('typeChart').getContext('2d');
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

async function loadRecentActivities() {
    try {
        // 模拟最近活动数据
        const activities = [
            {
                date: '2024-01-15',
                type: '跑步',
                duration: 30,
                description: '晨跑5公里'
            },
            {
                date: '2024-01-14',
                type: '游泳',
                duration: 45,
                description: '游泳训练'
            },
            {
                date: '2024-01-13',
                type: '骑行',
                duration: 60,
                description: '周末骑行'
            },
            {
                date: '2024-01-12',
                type: '瑜伽',
                duration: 30,
                description: '晚间瑜伽'
            },
            {
                date: '2024-01-11',
                type: '跑步',
                duration: 30,
                description: '夜跑'
            }
        ];
        
        // 渲染最近活动
        const tbody = document.querySelector('#recentActivities tbody');
        tbody.innerHTML = '';
        
        activities.forEach(activity => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${activity.date}</td>
                <td>${activity.type}</td>
                <td>${activity.duration}</td>
                <td>${activity.description}</td>
            `;
            tbody.appendChild(row);
        });
        
    } catch (error) {
        console.error('加载最近活动时出错:', error);
        showAlert('加载活动数据失败', 'error');
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