document.addEventListener('DOMContentLoaded', function() {
    // 检查管理员是否已登录
    checkAdminAuth();
    
    // 绑定退出登录事件
    document.getElementById('adminLogoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        adminLogout();
    });
    
    // 加载仪表板数据
    loadDashboardData();
});

async function checkAdminAuth() {
    const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
    if (!token) {
        window.location.href = '/admin/login';
        return;
    }
    
    try {
        const response = await fetch('/api/admin/validate', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            window.location.href = '/admin/login';
        }
    } catch (error) {
        console.error('验证管理员身份时出错:', error);
        window.location.href = '/admin/login';
    }
}

async function adminLogout() {
    const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
    
    try {
        if (token) {
            await fetch('/api/admin/logout', {
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
        localStorage.removeItem('adminToken');
        sessionStorage.removeItem('adminToken');
        // 跳转到登录页面
        window.location.href = '/admin/login';
    }
}

async function loadDashboardData() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 获取系统统计信息
        const statsResponse = await fetch('/api/admin/stats/system', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (statsResponse.ok) {
            const statsResult = await statsResponse.json();
            if (statsResult.code === 200 && statsResult.data) {
                const stats = statsResult.data;
                document.getElementById('totalUsers').textContent = stats.totalUsers || 0;
                document.getElementById('activeToday').textContent = stats.activeToday || 0;
                document.getElementById('totalRecords').textContent = stats.totalRecords || 0;
                document.getElementById('totalBadges').textContent = stats.totalBadges || 0;
            }
        }
        
        // 初始化图表
        initCharts();
        
        // 加载最新活动
        loadRecentActivities();
        
    } catch (error) {
        console.error('加载仪表板数据时出错:', error);
    }
}

async function loadRecentActivities() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            return;
        }
        
        // 获取最新的行为记录
        const recentResponse = await fetch('/api/admin/behaviors/recent', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        let recentActivities = [];
        if (recentResponse.ok) {
            const recentResult = await recentResponse.json();
            if (recentResult.code === 200 && recentResult.data) {
                recentActivities = Array.isArray(recentResult.data) ? recentResult.data : [];
            }
        }
        
        const tbody = document.querySelector('#recentActivities tbody');
        tbody.innerHTML = '';
        
        if (recentActivities.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = '<td colspan="4" class="text-center">暂无数据</td>';
            tbody.appendChild(row);
        } else {
            recentActivities.forEach(activity => {
                const row = document.createElement('tr');
                // 这里需要根据实际返回的数据结构调整
                row.innerHTML = `
                    <td>${activity.userId || '未知用户'}</td>
                    <td>${activity.typeName || '未知类型'}</td>
                    <td>${activity.duration || 0}</td>
                    <td>${activity.createTime || '未知时间'}</td>
                `;
                tbody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('加载最新活动时出错:', error);
    }
}

function initCharts() {
    // 用户增长趋势图
    const userGrowthCtx = document.getElementById('userGrowthChart').getContext('2d');
    
    // 先获取真实数据
    fetchUserGrowthData().then(data => {
        new Chart(userGrowthCtx, {
            type: 'line',
            data: {
                labels: data.labels || ['一月', '二月', '三月', '四月', '五月', '六月'],
                datasets: [{
                    label: '用户增长',
                    data: data.data || [120, 190, 130, 180, 210, 250],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
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
    }).catch(error => {
        console.error('获取用户增长数据失败:', error);
        // 使用默认数据
        new Chart(userGrowthCtx, {
            type: 'line',
            data: {
                labels: ['一月', '二月', '三月', '四月', '五月', '六月'],
                datasets: [{
                    label: '用户增长',
                    data: [120, 190, 130, 180, 210, 250],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
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
    });

    // 运动类型分布图
    const activityTypeCtx = document.getElementById('activityTypeChart').getContext('2d');
    
    // 先获取真实数据
    fetchActivityTypeDistribution().then(data => {
        new Chart(activityTypeCtx, {
            type: 'pie',
            data: {
                labels: data.labels || ['跑步', '游泳', '骑行', '瑜伽'],
                datasets: [{
                    label: '运动类型分布',
                    data: data.data || [45, 25, 20, 10],
                    backgroundColor: [
                        'rgb(255, 99, 132)',
                        'rgb(54, 162, 235)',
                        'rgb(255, 205, 86)',
                        'rgb(75, 192, 192)'
                    ]
                }]
            },
            options: {
                responsive: true
            }
        });
    }).catch(error => {
        console.error('获取运动类型分布数据失败:', error);
        // 使用默认数据
        new Chart(activityTypeCtx, {
            type: 'pie',
            data: {
                labels: ['跑步', '游泳', '骑行', '瑜伽'],
                datasets: [{
                    label: '运动类型分布',
                    data: [45, 25, 20, 10],
                    backgroundColor: [
                        'rgb(255, 99, 132)',
                        'rgb(54, 162, 235)',
                        'rgb(255, 205, 86)',
                        'rgb(75, 192, 192)'
                    ]
                }]
            },
            options: {
                responsive: true
            }
        });
    });
}

async function fetchUserGrowthData() {
    const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
    if (!token) {
        throw new Error('未登录');
    }
    
    const response = await fetch('/api/admin/stats/user-growth', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    if (response.ok) {
        const result = await response.json();
        if (result.code === 200 && result.data) {
            return result.data;
        }
    }
    
    throw new Error('获取数据失败');
}

async function fetchActivityTypeDistribution() {
    const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
    if (!token) {
        throw new Error('未登录');
    }
    
    const response = await fetch('/api/admin/stats/activity-type-distribution', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    
    if (response.ok) {
        const result = await response.json();
        if (result.code === 200 && result.data) {
            return result.data;
        }
    }
    
    throw new Error('获取数据失败');
}