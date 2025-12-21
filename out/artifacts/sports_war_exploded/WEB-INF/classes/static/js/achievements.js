/**
 * 成就页面JavaScript文件
 * 处理成就徽章的加载和显示
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化成就页面
    initAchievementsPage();
});

// 初始化成就页面
async function initAchievementsPage() {
    try {
        // 加载用户信息
        await loadUserInfo();
        
        // 加载徽章数据
        await loadBadges();
        
        // 绑定事件
        bindEvents();
    } catch (error) {
        console.error('初始化成就页面失败:', error);
    }
}

// 加载用户信息
async function loadUserInfo() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            // 未登录用户，重定向到登录页面
            window.location.href = '/login';
            return;
        }

        const response = await fetch('/api/user/validate-token', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                // 更新页面上的用户信息
                document.getElementById('userName').textContent = result.data.nickname || result.data.username;
                
                // 更新头像
                const avatarElements = document.querySelectorAll('.user-avatar');
                avatarElements.forEach(img => {
                    if (result.data.avatar) {
                        img.src = result.data.avatar;
                    } else {
                        img.src = '/images/avatar.png';
                    }
                });
            } else {
                // Token无效，重定向到登录页面
                window.location.href = '/login';
            }
        } else {
            // Token验证失败，重定向到登录页面
            window.location.href = '/login';
        }
    } catch (error) {
        console.error('加载用户信息失败:', error);
        window.location.href = '/login';
    }
}

// 加载徽章数据
async function loadBadges() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // 获取所有徽章
        const allBadgesResponse = await fetch('/api/badge/all', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        let allBadges = [];
        if (allBadgesResponse.ok) {
            const allBadgesResult = await allBadgesResponse.json();
            if (allBadgesResult.code === 200) {
                allBadges = allBadgesResult.data || [];
            }
        }

        // 获取用户徽章信息
        const userBadgesResponse = await fetch('/api/user/badges', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        let userBadges = [];
        if (userBadgesResponse.ok) {
            const userBadgesResult = await userBadgesResponse.json();
            if (userBadgesResult.code === 200) {
                userBadges = userBadgesResult.data || [];
            }
        }

        // 渲染徽章
        renderBadges(allBadges, userBadges);
    } catch (error) {
        console.error('加载徽章数据失败:', error);
    }
}

// 渲染徽章
function renderBadges(allBadges, userBadges) {
    // 渲染全部徽章
    renderBadgeCategory('allBadges', allBadges, userBadges, 'all');
    
    // 渲染已获得徽章
    const earnedBadges = allBadges.filter(badge => 
        userBadges.some(ub => ub.badgeId === badge.badgeId && ub.progress >= 100)
    );
    renderBadgeCategory('earnedBadges', earnedBadges, userBadges, 'earned');
    
    // 渲染进行中徽章
    const progressBadges = allBadges.filter(badge => 
        userBadges.some(ub => ub.badgeId === badge.badgeId && ub.progress > 0 && ub.progress < 100)
    );
    renderBadgeCategory('progressBadges', progressBadges, userBadges, 'progress');
}

// 渲染徽章分类
function renderBadgeCategory(containerId, badges, userBadges, category) {
    const container = document.getElementById(containerId);
    if (!container) return;

    if (badges.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-5">
                    <i class="fas fa-trophy fa-3x text-muted mb-3"></i>
                    <p class="text-muted">暂无${getCategoryText(category)}徽章</p>
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = badges.map(badge => {
        const userBadge = userBadges.find(ub => ub.badgeId === badge.badgeId);
        const progress = userBadge ? userBadge.progress || 0 : 0;
        const isEarned = progress >= 100;
        const isProgress = progress > 0 && progress < 100;
        
        let badgeClass = '';
        if (isEarned) badgeClass = 'earned';
        else if (isProgress) badgeClass = 'in-progress';
        else badgeClass = 'locked';
        
        return `
            <div class="col-lg-3 col-md-4 col-sm-6 mb-4">
                <div class="badge-card p-4 text-center h-100">
                    <div class="badge-icon ${isEarned ? '' : 'badge-locked'}">
                        <i class="${badge.iconUrl || 'fas fa-medal'}" style="color: ${getBadgeColor(badge.level)};"></i>
                    </div>
                    <h6 class="mt-3 mb-2">${badge.badgeName}</h6>
                    <p class="text-muted small mb-2">${badge.description}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="badge ${isEarned ? 'bg-success' : 'bg-warning'}">${isEarned ? '已获得' : `${progress}%`}</span>
                        <small class="text-muted">等级 ${badge.level}</small>
                    </div>
                    ${!isEarned ? `
                        <div class="progress mt-2">
                            <div class="progress-bar" role="progressbar" 
                                 style="width: ${progress}%" 
                                 aria-valuenow="${progress}" 
                                 aria-valuemin="0" 
                                 aria-valuemax="100">
                                ${progress}%
                            </div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }).join('');
}

// 获取分类文本
function getCategoryText(category) {
    const categories = {
        'all': '全部',
        'earned': '已获得',
        'progress': '进行中'
    };
    return categories[category] || '';
}

// 获取徽章颜色
function getBadgeColor(level) {
    const colors = {
        1: '#FFD700', // 金色
        2: '#C0C0C0', // 银色
        3: '#CD7F32', // 铜色
        4: '#964B00', // 棕色
        5: '#FF6B6B'  // 红色
    };
    return colors[level] || '#6c757d';
}

// 绑定事件
function bindEvents() {
    // 绑定退出按钮事件
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
}

// 处理退出登录
async function handleLogout() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (token) {
            await fetch('/api/user/logout', {
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
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
        // 重定向到登录页面
        window.location.href = '/login';
    }
}

// 刷新徽章数据
async function refreshBadges() {
    try {
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // 重新加载徽章数据
        await loadBadges();
    } catch (error) {
        console.error('刷新徽章数据失败:', error);
    }
}