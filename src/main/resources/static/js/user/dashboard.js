document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    AuthUtils.checkAuth();
    
    // 加载仪表板数据
    loadDashboardData();
    
    // 每隔一段时间自动刷新数据（例如每5分钟）
    setInterval(loadDashboardData, 5 * 60 * 1000);
});

async function loadDashboardData() {
    try {
        const isAuthenticated = await CommonUtils.checkUserAuth();
        if (!isAuthenticated) {
            window.location.href = '/login';
            return;
        }

        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

        // 获取用户基本信息
        const profileResponse = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!profileResponse.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (profileResponse.status === 401) {
                CommonUtils.clearAuth();
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${profileResponse.status}`);
        }

        const profileResult = await profileResponse.json();
        const userData = profileResult.data;
        
        // 显示用户信息
        const welcomeUserElement = document.getElementById('welcomeUser');
        if (welcomeUserElement) {
            welcomeUserElement.textContent = userData.nickname || userData.username || '用户';
        }
        
        // 设置头像
        const userAvatarElements = document.querySelectorAll('.user-avatar');
        userAvatarElements.forEach(element => {
            if (userData.avatar) {
                element.src = userData.avatar;
            } else {
                element.src = '/images/avatar/avatar.png';
            }
        });
        
        // 更新欢迎横幅中的用户头像
        const bannerAvatarElement = document.querySelector('.welcome-banner img');
        if (bannerAvatarElement) {
            if (userData.avatar) {
                bannerAvatarElement.src = userData.avatar;
            } else {
                bannerAvatarElement.src = '/images/avatar/avatar.png';
            }
        }

        // 获取本周运动数据
        const weekStart = new Date();
        weekStart.setDate(weekStart.getDate() - weekStart.getDay() + (weekStart.getDay() === 0 ? -6 : 1)); // 本周周一
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekStart.getDate() + 6); // 本周周日
        
        const weekStatsResponse = await fetch(`/api/user/activity-stats?startDate=${weekStart.toISOString().split('T')[0]}&endDate=${weekEnd.toISOString().split('T')[0]}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!weekStatsResponse.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (weekStatsResponse.status === 401) {
                CommonUtils.clearAuth();
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${weekStatsResponse.status}`);
        }

        const weekStatsResult = await weekStatsResponse.json();
        const weekStats = weekStatsResult.data;
        
        // 更新本周运动数据
        const weeklyDurationElement = document.getElementById('weeklyDuration');
        if (weeklyDurationElement) {
            weeklyDurationElement.textContent = weekStats.totalDuration || 0;
        }
        
        // 计算本周运动次数
        let weeklyCount = 0;
        if (weekStats.typeDistribution) {
            weeklyCount = weekStats.typeDistribution.reduce((total, item) => total + (item.recordCount || 0), 0);
        }
        
        const weeklyCountElement = document.getElementById('weeklyCount');
        if (weeklyCountElement) {
            weeklyCountElement.textContent = weeklyCount;
        }

        // 获取本月运动数据
        const monthStart = new Date();
        monthStart.setDate(1); // 本月第一天
        const monthEnd = new Date();
        
        const monthStatsResponse = await fetch(`/api/user/activity-stats?startDate=${monthStart.toISOString().split('T')[0]}&endDate=${monthEnd.toISOString().split('T')[0]}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!monthStatsResponse.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (monthStatsResponse.status === 401) {
                CommonUtils.clearAuth();
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${monthStatsResponse.status}`);
        }

        const monthStatsResult = await monthStatsResponse.json();
        const monthStats = monthStatsResult.data;
        
        // 更新本月运动数据
        const monthlyDurationElement = document.getElementById('monthlyDuration');
        if (monthlyDurationElement) {
            monthlyDurationElement.textContent = monthStats.totalDuration || 0;
        }
        
        // 计算本月运动次数
        let monthlyCount = 0;
        if (monthStats.typeDistribution) {
            monthlyCount = monthStats.typeDistribution.reduce((total, item) => total + (item.recordCount || 0), 0);
        }
        
        const monthlyCountElement = document.getElementById('monthlyCount');
        if (monthlyCountElement) {
            monthlyCountElement.textContent = monthlyCount;
        }

        // 获取徽章数据
        const badgesResponse = await fetch('/api/user/badges', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!badgesResponse.ok) {
            // 如果是认证错误，清除token并重定向到登录页
            if (badgesResponse.status === 401) {
                CommonUtils.clearAuth();
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${badgesResponse.status}`);
        }

        const badgesResult = await badgesResponse.json();
        const badges = badgesResult.data;
        
        // 更新徽章数量（只计算已获得且进度为100%的徽章）
        const badgeCountElement = document.getElementById('badgeCount');
        if (badgeCountElement) {
            const achievedBadges = Array.isArray(badges) ? badges.filter(badge => badge.achieved && badge.progress >= 100) : [];
            badgeCountElement.textContent = achievedBadges.length;
        }
        
        // 加载最近活动
        loadRecentActivities();
        
        // 初始化图表
        // 使用setTimeout确保DOM完全加载后再初始化图表
        setTimeout(() => {
            initCharts(monthStats.typeDistribution || []);
        }, 0);
    } catch (error) {
        console.error('加载仪表板数据时出错:', error);
        CommonUtils.showAlert('加载数据失败，请稍后重试: ' + error.message, 'error');
    }
}

function initCharts(typeDistribution) {
    // 确保Chart.js已加载
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js 未加载，跳过图表初始化');
        return;
    }
    
    // 销毁已有的图表实例，防止重复创建
    if (window.weeklyChartInstance) {
        window.weeklyChartInstance.destroy();
    }
    
    if (window.typeChartInstance) {
        window.typeChartInstance.destroy();
    }
    
    // 本周运动统计图（使用真实数据）
    const weeklyChart = document.getElementById('weeklyChart');
    if (weeklyChart) {
        const weeklyCtx = weeklyChart.getContext('2d');
        window.weeklyChartInstance = new Chart(weeklyCtx, {
            type: 'bar',
            data: {
                labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
                datasets: [{
                    label: '运动时长(分钟)',
                    data: [30, 0, 45, 30, 60, 45, 30], // 这里应该从后端获取真实数据
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
    
    // 运动类型分布图（使用真实数据）
    const typeChart = document.getElementById('typeChart');
    if (typeChart && typeDistribution) {
        const typeCtx = typeChart.getContext('2d');
        
        // 处理类型分布数据
        const labels = typeDistribution.map(item => item.typeName || '未知类型');
        const data = typeDistribution.map(item => item.totalDuration || 0);
        
        window.typeChartInstance = new Chart(typeCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: [
                        '#FF6384',
                        '#36A2EB',
                        '#FFCE56',
                        '#4BC0C0',
                        '#9966FF',
                        '#FF9F40'
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
        const isAuthenticated = await CommonUtils.checkUserAuth();
        if (!isAuthenticated) {
            window.location.href = '/login';
            return;
        }
        
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        
        // 获取用户ID
        let userId = CommonUtils.getUserIdFromToken(token);
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
        const recentActivitiesTable = document.querySelector('#recentActivities tbody');
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
        CommonUtils.showAlert('加载活动数据失败: ' + error.message, 'error');
    }
}