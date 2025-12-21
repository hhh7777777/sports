/**
 * 管理员仪表板JavaScript文件
 * 处理管理员仪表板的数据加载和显示
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化管理员仪表板
    initAdminDashboard();
});

// 初始化管理员仪表板
async function initAdminDashboard() {
    try {
        // 检查管理员认证状态
        const isAuthenticated = await checkAdminAuth();
        if (!isAuthenticated) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 加载统计数据
        await loadDashboardStats();
        
        // 加载图表数据
        await loadCharts();
        
        // 绑定事件
        bindAdminEvents();
    } catch (error) {
        console.error('初始化管理员仪表板失败:', error);
    }
}

// 检查管理员认证状态
async function checkAdminAuth() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            return false;
        }

        const response = await fetch('/api/admin/validate-token', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                // 更新页面上的管理员信息
                document.getElementById('adminName').textContent = result.data.username || '管理员';
                return true;
            }
        }
        
        return false;
    } catch (error) {
        console.error('检查管理员认证状态失败:', error);
        return false;
    }
}

// 加载仪表板统计数据
async function loadDashboardStats() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }

        const response = await fetch('/api/admin/dashboard/stats', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const stats = result.data;
                
                // 更新统计数据
                document.getElementById('totalUsers').textContent = stats.totalUsers || 0;
                document.getElementById('totalBehaviors').textContent = stats.totalBehaviors || 0;
                document.getElementById('totalBadges').textContent = stats.totalBadges || 0;
                document.getElementById('totalActiveUsers').textContent = stats.totalActiveUsers || 0;
                
                // 更新趋势数据
                if (stats.userGrowth) {
                    updateGrowthIndicator('userGrowth', stats.userGrowth.rate, stats.userGrowth.trend);
                }
                if (stats.behaviorGrowth) {
                    updateGrowthIndicator('behaviorGrowth', stats.behaviorGrowth.rate, stats.behaviorGrowth.trend);
                }
            }
        }
    } catch (error) {
        console.error('加载仪表板统计数据失败:', error);
    }
}

// 更新增长指标
function updateGrowthIndicator(elementId, rate, trend) {
    const element = document.getElementById(elementId);
    if (!element) return;
    
    const trendIcon = trend > 0 ? 'fas fa-arrow-up text-success' : 'fas fa-arrow-down text-danger';
    const trendText = trend > 0 ? '上升' : '下降';
    
    element.innerHTML = `
        <i class="${trendIcon}"></i>
        ${Math.abs(rate)}% ${trendText}
    `;
}

// 加载图表数据
async function loadCharts() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            return;
        }

        // 获取用户增长数据
        const userGrowthResponse = await fetch('/api/admin/dashboard/user-growth', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (userGrowthResponse.ok) {
            const userGrowthResult = await userGrowthResponse.json();
            if (userGrowthResult.code === 200) {
                renderUserGrowthChart(userGrowthResult.data);
            }
        }

        // 获取行为类型分布数据
        const behaviorDistributionResponse = await fetch('/api/admin/dashboard/behavior-distribution', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (behaviorDistributionResponse.ok) {
            const behaviorDistributionResult = await behaviorDistributionResponse.json();
            if (behaviorDistributionResult.code === 200) {
                renderBehaviorDistributionChart(behaviorDistributionResult.data);
            }
        }
    } catch (error) {
        console.error('加载图表数据失败:', error);
    }
}

// 渲染用户增长图表 (模拟实现，实际项目中需要引入图表库如Chart.js)
function renderUserGrowthChart(data) {
    // 这里应该使用图表库来渲染实际图表
    // 模拟显示数据
    const chartContainer = document.getElementById('userGrowthChart');
    if (chartContainer) {
        chartContainer.innerHTML = `
            <div class="text-center py-5">
                <i class="fas fa-chart-line fa-3x text-muted mb-3"></i>
                <p class="text-muted">用户增长图表</p>
                <small class="text-muted">数据点: ${data ? data.length : 0} 个</small>
            </div>
        `;
    }
}

// 渲染行为类型分布图表
function renderBehaviorDistributionChart(data) {
    // 这里应该使用图表库来渲染实际图表
    // 模拟显示数据
    const chartContainer = document.getElementById('behaviorDistributionChart');
    if (chartContainer) {
        chartContainer.innerHTML = `
            <div class="text-center py-5">
                <i class="fas fa-chart-pie fa-3x text-muted mb-3"></i>
                <p class="text-muted">行为类型分布图表</p>
                <small class="text-muted">类型数: ${data ? Object.keys(data).length : 0} 种</small>
            </div>
        `;
    }
}

// 绑定管理员事件
function bindAdminEvents() {
    // 绑定退出按钮事件
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleAdminLogout);
    }
    
    // 绑定侧边栏导航事件
    bindSidebarEvents();
    
    // 绑定刷新按钮事件
    const refreshBtn = document.getElementById('refreshStatsBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            loadDashboardStats();
            loadCharts();
            showNotification('数据已刷新', 'success');
        });
    }
}

// 绑定侧边栏导航事件
function bindSidebarEvents() {
    const navLinks = document.querySelectorAll('.sidebar .nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            // 移除所有激活状态
            navLinks.forEach(l => l.classList.remove('active'));
            
            // 添加激活状态到当前链接
            this.classList.add('active');
        });
    });
}

// 处理管理员退出
async function handleAdminLogout() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (token) {
            await fetch('/api/admin/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        }
    } catch (error) {
        console.error('退出时出错:', error);
    } finally {
        // 清除本地存储的令牌
        localStorage.removeItem('adminToken');
        sessionStorage.removeItem('adminToken');
        // 重定向到登录页面
        window.location.href = '/admin/login';
    }
}

// 显示通知
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show position-fixed`;
    notification.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(notification);
    
    // 自动移除通知
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

// 刷新仪表板数据
async function refreshDashboard() {
    await loadDashboardStats();
    await loadCharts();
}