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
        // 模拟统计数据
        document.getElementById('totalUsers').textContent = '1,234';
        document.getElementById('activeToday').textContent = '56';
        document.getElementById('totalRecords').textContent = '8,901';
        document.getElementById('totalBadges').textContent = '234';
        
        // 初始化图表
        initCharts();
    } catch (error) {
        console.error('加载仪表板数据时出错:', error);
    }
}

function initCharts() {
    // 用户增长趋势图
    const userGrowthCtx = document.getElementById('userGrowthChart').getContext('2d');
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
    
    // 运动类型分布图
    const activityTypeCtx = document.getElementById('activityTypeChart').getContext('2d');
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
}