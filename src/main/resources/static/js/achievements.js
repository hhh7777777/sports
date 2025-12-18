document.addEventListener('DOMContentLoaded', function() {
    // 检查用户是否已登录
    checkAuth();
    
    // 绑定退出登录事件
    document.getElementById('logoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        logout();
    });
    
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

async function loadAchievements(tabId) {
    try {
        // 模拟成就数据
        const achievements = [
            {
                id: 1,
                name: '第一步',
                description: '完成第一次运动记录',
                icon: 'fa-walking',
                color: 'text-primary',
                earned: true,
                progress: 100,
                earnedDate: '2024-01-01'
            },
            {
                id: 2,
                name: '坚持不懈',
                description: '连续锻炼7天',
                icon: 'fa-calendar-check',
                color: 'text-success',
                earned: true,
                progress: 100,
                earnedDate: '2024-01-07'
            },
            {
                id: 3,
                name: '健身爱好者',
                description: '累计锻炼30天',
                icon: 'fa-heart',
                color: 'text-danger',
                earned: false,
                progress: 65,
                earnedDate: null
            },
            {
                id: 4,
                name: '跑步达人',
                description: '累计跑步50公里',
                icon: 'fa-running',
                color: 'text-warning',
                earned: false,
                progress: 40,
                earnedDate: null
            }
        ];
        
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
        showAlert('加载成就失败', 'error');
    }
}

function renderAchievements(achievements, tabId) {
    const container = document.getElementById(`${tabId}Badges`);
    container.innerHTML = '';
    
    if (achievements.length === 0) {
        container.innerHTML = '<div class="col-12"><p class="text-center text-muted">暂无成就</p></div>';
        return;
    }
    
    achievements.forEach(achievement => {
        const badgeCard = document.createElement('div');
        badgeCard.className = 'col-md-6 col-lg-4 mb-4';
        badgeCard.innerHTML = `
            <div class="card badge-card h-100 ${achievement.earned ? '' : 'badge-locked'}">
                <div class="card-body text-center">
                    <div class="badge-icon ${achievement.color}">
                        <i class="fas ${achievement.icon}"></i>
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