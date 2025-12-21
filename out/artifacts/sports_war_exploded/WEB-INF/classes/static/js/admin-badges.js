document.addEventListener('DOMContentLoaded', function() {
    // 检查管理员是否已登录
    checkAdminAuth();
    
    // 绑定退出登录事件
    document.getElementById('adminLogoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        adminLogout();
    });
    
    // 绑定添加徽章按钮事件
    document.getElementById('saveBadgeBtn').addEventListener('click', saveBadge);
    
    // 绑定表单搜索事件
    document.getElementById('filterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        filterBadges();
    });
    
    // 绑定编辑和删除按钮事件
    document.getElementById('badgesContainer').addEventListener('click', function(e) {
        if (e.target.closest('.edit-btn')) {
            const badgeId = e.target.closest('.edit-btn').dataset.id;
            editBadge(badgeId);
        }
        
        if (e.target.closest('.delete-btn')) {
            const badgeId = e.target.closest('.delete-btn').dataset.id;
            deleteBadge(badgeId);
        }
    });
    
    // 页面加载时获取徽章列表
    loadBadges();
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

async function loadBadges() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch('/api/admin/badges', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderBadges(result.data);
            }
        } else {
            throw new Error('获取徽章列表失败');
        }
    } catch (error) {
        console.error('加载徽章列表时出错:', error);
        showAlert('加载徽章列表失败: ' + error.message, 'error');
    }
}

function renderBadges(badges) {
    const container = document.getElementById('badgesContainer');
    container.innerHTML = '';
    
    if (!Array.isArray(badges) || badges.length === 0) {
        container.innerHTML = '<div class="col-12"><p class="text-center">暂无徽章数据</p></div>';
        return;
    }
    
    badges.forEach(badge => {
        const col = document.createElement('div');
        col.className = 'col-lg-4 col-md-6 mb-4';
        // 使用 iconUrl 来显示图标
        let iconHtml = '';
        if (badge.iconUrl) {
            let iconUrl = badge.iconUrl;
            if (iconUrl.startsWith('/icons/')) {
                iconUrl = iconUrl.replace('/icons/', '/images/icons/');
            }
            iconHtml = `<img src="${iconUrl}" alt="${badge.badgeName}" style="width: 64px; height: 64px;">`;
        } else {
            // 默认使用奖杯图标
            iconHtml = '<i class="fas fa-trophy"></i>';
        }
        
        col.innerHTML = `
            <div class="card badge-card h-100">
                <div class="card-body text-center">
                    <div class="badge-icon text-primary">
                        ${iconHtml}
                    </div>
                    <h5 class="card-title">${badge.badgeName || ''}</h5>
                    <p class="card-text">${badge.description || ''}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="text-muted">类型: ${getBadgeTypeText(badge.badgeType)}</small>
                        <div>
                            <button class="btn btn-sm btn-outline-primary edit-btn" data-id="${badge.badgeId}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${badge.badgeId}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}

function getBadgeTypeText(type) {
    switch (type) {
        case 'activity':
            return '活动徽章';
        case 'achievement':
            return '成就徽章';
        case 'participation':
            return '参与徽章';
        default:
            return '未知类型';
    }
}

async function filterBadges() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const badgeName = document.getElementById('badgeName').value;
        const badgeType = document.getElementById('badgeType').value;
        
        // 构建查询参数
        let url = '/api/admin/badges';
        const params = new URLSearchParams();
        
        if (badgeName) params.append('badgeName', badgeName);
        if (badgeType) params.append('badgeType', badgeType);
        
        if (params.toString()) {
            url += '?' + params.toString();
        }
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderBadges(result.data);
            }
        } else {
            throw new Error('筛选徽章失败');
        }
    } catch (error) {
        console.error('筛选徽章时出错:', error);
        showAlert('筛选徽章失败: ' + error.message, 'error');
    }
}

async function editBadge(badgeId) {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch(`/api/admin/badges/${badgeId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const badge = result.data;
                
                // 检查DOM元素是否存在
                const modalTitleElement = document.getElementById('addBadgeModalLabel');
                const badgeIdElement = document.getElementById('badgeId');
                const badgeNameElement = document.getElementById('modalBadgeName');
                const badgeDescriptionElement = document.getElementById('modalBadgeDescription');
                const badgeIconUrlElement = document.getElementById('modalBadgeIconUrl');
                const currentIconPreviewElement = document.getElementById('currentIconPreview');
                const currentIconImgElement = document.getElementById('currentIconImg');
                const badgeTypeElement = document.getElementById('modalBadgeType');
                
                if (!modalTitleElement || !badgeIdElement || !badgeNameElement || 
                    !badgeDescriptionElement || !badgeIconUrlElement || 
                    !badgeTypeElement) {
                    throw new Error('页面元素缺失，请刷新页面后重试');
                }
                
                modalTitleElement.textContent = '编辑徽章';
                badgeIdElement.value = badge.badgeId;
                badgeNameElement.value = badge.badgeName;
                badgeDescriptionElement.value = badge.description;
                // 设置图标URL到隐藏字段
                badgeIconUrlElement.value = badge.iconUrl || '';
                
                // 显示当前图标预览
                if (badge.iconUrl && currentIconPreviewElement && currentIconImgElement) {
                    currentIconImgElement.src = badge.iconUrl;
                    currentIconPreviewElement.style.display = 'block';
                }
                
                badgeTypeElement.value = badge.badgeType || 'activity';
                
                // 显示模态框
                const modal = new bootstrap.Modal(document.getElementById('addBadgeModal'));
                modal.show();
            } else {
                throw new Error(result.message || '获取徽章信息失败');
            }
        } else {
            throw new Error('获取徽章信息失败');
        }
    } catch (error) {
        console.error('编辑徽章时出错:', error);
        showAlert('获取徽章信息失败: ' + error.message, 'error');
    }
}

async function deleteBadge(badgeId) {
    if (!confirm('确定要删除这个徽章吗？')) {
        return;
