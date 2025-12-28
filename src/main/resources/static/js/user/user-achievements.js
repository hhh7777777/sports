// 页面加载完成时执行
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
    
    // 绑定标签页切换事件
    document.querySelectorAll('#achievementTabs button').forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-bs-target').substring(1);
            loadAchievements(tabId);
        });
    });
    
    // 加载成就数据
    loadAchievements('all');
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

async function loadAchievements(tabId) {
    try {
        // 获取认证token
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            window.location.href = '/login';
            return;
        }

        // 获取所有徽章
        const badgesResponse = await fetch('/api/badge/list', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!badgesResponse.ok) {
            throw new Error('获取徽章数据失败');
        }

        const badgesResult = await badgesResponse.json();
        const allBadges = badgesResult.data || [];

        // 获取用户已获得的徽章
        const userBadgesResponse = await fetch('/api/badge/my-achievements', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        let userBadges = [];
        if (userBadgesResponse.ok) {
            const userBadgesResult = await userBadgesResponse.json();
            userBadges = userBadgesResult.data || [];
        }

        // 合并徽章数据和用户进度数据
        const achievements = allBadges.map(badge => {
            const userBadge = userBadges.find(ub => ub.badgeId === badge.badgeId);
            return {
                id: badge.badgeId,
                name: badge.badgeName,
                description: badge.description,
                icon: badge.iconUrl || 'fa-medal',
                color: getColorByLevel(badge.level),
                earned: userBadge && userBadge.achieved,
                progress: userBadge ? userBadge.progress : 0,
                earnedDate: userBadge && userBadge.achieveTime ? new Date(userBadge.achieveTime).toLocaleDateString() : null
            };
        });

        // 根据标签页过滤成就
        let filteredAchievements = [];
        switch (tabId) {
            case 'earned':
                filteredAchievements = achievements.filter(a => a.earned);
                break;
            case 'progress':
                filteredAchievements = achievements.filter(a => !a.earned);
                break;
            default:
                filteredAchievements = [...achievements];
        }

        // 渲染成就
        renderAchievements(filteredAchievements, tabId);

    } catch (error) {
        console.error('加载成就时出错:', error);
        showAlert('加载成就失败: ' + error.message, 'error');
    }
}

function getColorByLevel(level) {
    const colorMap = {
        1: 'text-primary',
        2: 'text-success',
        3: 'text-warning',
        4: 'text-danger',
        5: 'text-info'
    };
    return colorMap[level] || 'text-primary';
}

function renderAchievements(achievements, tabId) {
    // 构造容器ID
    const containerId = `${tabId}Badges`;
    const container = document.getElementById(containerId);
    
    // 检查容器元素是否存在
    if (!container) {
        console.error(`找不到ID为 ${containerId} 的元素`);
        return;
    }
    
    container.innerHTML = '';
    
    if (achievements.length === 0) {
        container.innerHTML = '<div class="col-12"><p class="text-center text-muted">暂无成就</p></div>';
        return;
    }
    
    achievements.forEach(achievement => {
        const badgeCard = document.createElement('div');
        badgeCard.className = 'col-md-6 col-lg-4 mb-4';
        
        // 根据徽章名称选择合适的图标
        const iconSrc = getBadgeIcon(achievement.name);
        
        // 根据成就状态设置样式类
        const cardClass = achievement.earned ? 'card badge-card h-100' : 'card badge-card h-100 badge-locked';
        const iconClass = achievement.earned ? '' : 'opacity-50'; // 未获得的徽章图标半透明
        
        badgeCard.innerHTML = `
            <div class="${cardClass}">
                <div class="card-body text-center">
                    <div class="badge-icon ${iconClass}">
                        <img src="${iconSrc}" alt="${achievement.name}" class="badge-img">
                    </div>
                    <h5 class="card-title">${achievement.name}</h5>
                    <p class="card-text">${achievement.description}</p>
                    ${achievement.earned ? 
                        `<div class="text-success">
                            <small>获得于 ${achievement.earnedDate}</small>
                        </div>` :
                        `<div class="progress mt-3">
                            <div class="progress-bar" role="progressbar" 
                                style="width: ${achievement.progress}%;" 
                                aria-valuenow="${achievement.progress}" 
                                aria-valuemin="0" 
                                aria-valuemax="100">
                                ${achievement.progress}%
                            </div>
                        </div>`
                    }
                </div>
            </div>
        `;
        container.appendChild(badgeCard);
    });
}

function getBadgeIcon(badgeName) {
    // 根据徽章名称映射到对应的图片
    const badgeNameMap = {
        '第一步': '/images/icons/early-bird.png',
        '坚持不懈': '/images/icons/iron-will.png',
        '健身爱好者': '/images/icons/all-rounder.png',
        '跑步达人': '/images/icons/marathon.png',
        '运动新人': '/images/icons/new.png',
        '坚持之星': '/images/icons/consist.png',
        '运动达人': '/images/icons/professor.png',
        '全能选手': '/images/icons/export.png',
        '周年纪念': '/images/icons/anniversary.png',
        '生日特别': '/images/icons/birthday.png',
        '节日限定': '/images/icons/holiday.png',
        '分享之星': '/images/icons/share.png',
        '团队领袖': '/images/icons/team-leader.png',
        '社交达人': '/images/icons/social.png',
        '白金': '/images/icons/platinum.png',
        '白银': '/images/icons/silver.png',
        '钻石': '/images/icons/diamond.png',
        '青铜': '/images/icons/bronze.png',
        '黄金': '/images/icons/gold.png',
        '周末战士': '/images/icons/weekend-warrior.png',
        '夜猫子': '/images/icons/night-owl.png',
        '全能王': '/images/icons/all-rounder.png',
        '耐力王': '/images/icons/endurance.png',
        '马拉松选手': '/images/icons/marathon.png',
        '千分钟达人': '/images/icons/1000min.png',
        '百公里俱乐部': '/images/icons/100km.png',
        '钢铁意志': '/images/icons/iron-will.png'
    };

    // 如果有匹配的名称，返回对应的图片路径
    if (badgeNameMap[badgeName]) {
        return badgeNameMap[badgeName];
    }

    // 默认返回青铜徽章
    return '/images/icons/bronze.png';
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