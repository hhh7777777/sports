document.addEventListener('DOMContentLoaded', function() {
    initializeAdminBadges();
});

// 初始化管理员徽章管理功能
function initializeAdminBadges() {
    try {
        // 检查管理员是否已登录
        checkAdminAuth();
        
        // 绑定退出登录事件
        const logoutBtn = document.getElementById('adminLogoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function(e) {
                e.preventDefault();
                adminLogout();
            });
        }
        
        // 绑定添加徽章按钮事件
        const saveBadgeBtn = document.getElementById('saveBadgeBtn');
        if (saveBadgeBtn) {
            saveBadgeBtn.addEventListener('click', saveBadge);
        }
        
        // 绑定表单搜索事件
        const filterForm = document.getElementById('filterForm');
        if (filterForm) {
            filterForm.addEventListener('submit', function(e) {
                e.preventDefault();
                filterBadges();
            });
        }
        
        // 绑定编辑和删除按钮事件
        const badgesContainer = document.getElementById('badgesContainer');
        if (badgesContainer) {
            badgesContainer.addEventListener('click', function(e) {
                if (e.target.closest('.edit-btn')) {
                    const badgeId = e.target.closest('.edit-btn').dataset.id;
                    editBadge(badgeId);
                }
                
                if (e.target.closest('.delete-btn')) {
                    const badgeId = e.target.closest('.delete-btn').dataset.id;
                    deleteBadge(badgeId);
                }
            });
        }
        
        // 页面加载时获取徽章列表
        loadBadges();
    } catch (error) {
        console.error('初始化管理员徽章管理功能时出错:', error);
        showAlert('初始化失败: ' + error.message, 'error');
    }
}

async function checkAdminAuth() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
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
            } else {
                throw new Error(result.message || '获取徽章列表失败');
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
    try {
        const container = document.getElementById('badgesContainer');
        if (!container) {
            console.warn('未找到徽章容器 (#badgesContainer)');
            return;
        }
        
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
    } catch (error) {
        console.error('渲染徽章时出错:', error);
        showAlert('渲染徽章失败: ' + error.message, 'error');
    }
}

function getBadgeTypeText(type) {
    try {
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
    } catch (error) {
        console.error('获取徽章类型文本时出错:', error);
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
        
        const badgeName = document.getElementById('badgeName')?.value;
        const badgeType = document.getElementById('badgeType')?.value;
        
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
            } else {
                throw new Error(result.message || '筛选徽章失败');
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
                const badgeLevelElement = document.getElementById('modalBadgeLevel'); // 添加等级字段
                
                if (!modalTitleElement || !badgeIdElement || !badgeNameElement || 
                    !badgeDescriptionElement || !badgeIconUrlElement || 
                    !badgeTypeElement || !badgeLevelElement) { // 添加等级字段检查
                    throw new Error('页面元素缺失，请刷新页面后重试');
                }
                
                modalTitleElement.textContent = '编辑徽章';
                badgeIdElement.value = badge.badgeId;
                badgeNameElement.value = badge.badgeName;
                badgeDescriptionElement.value = badge.description;
                badgeLevelElement.value = badge.level || ''; // 设置等级字段值
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
    }
    
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch(`/api/admin/badges/${badgeId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                showAlert('徽章删除成功', 'success');
                // 重新加载徽章列表
                loadBadges();
            } else {
                throw new Error(result.message || '删除徽章失败');
            }
        } else {
            throw new Error('删除徽章失败');
        }
    } catch (error) {
        console.error('删除徽章时出错:', error);
        showAlert('删除徽章失败: ' + error.message, 'error');
    }
}

async function saveBadge() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 检查DOM元素是否存在
        const badgeIdElement = document.getElementById('badgeId');
        const badgeNameElement = document.getElementById('modalBadgeName');
        const badgeDescriptionElement = document.getElementById('modalBadgeDescription');
        const badgeIconUrlElement = document.getElementById('modalBadgeIconUrl');
        const badgeIconElement = document.getElementById('modalBadgeIcon');
        const badgeTypeElement = document.getElementById('modalBadgeType');
        const badgeLevelElement = document.getElementById('modalBadgeLevel'); // 新增等级字段
        
        if (!badgeNameElement || !badgeDescriptionElement || !badgeIconUrlElement || 
            !badgeIconElement || !badgeTypeElement || !badgeLevelElement) { // 添加等级字段检查
            throw new Error('页面元素缺失，请刷新页面后重试');
        }
        
        const badgeId = badgeIdElement.value;
        const badgeName = badgeNameElement.value.trim();
        const badgeDescription = badgeDescriptionElement.value.trim();
        const badgeIconUrl = badgeIconUrlElement.value;
        const badgeType = badgeTypeElement.value;
        const badgeLevel = badgeLevelElement.value; // 获取等级字段值
        
        // 验证必填字段
        if (!badgeName) {
            showAlert('请输入徽章名称', 'warning');
            return;
        }
        
        if (!badgeDescription) {
            showAlert('请输入徽章描述', 'warning');
            return;
        }
        
        // 验证等级字段
        if (!badgeLevel) {
            showAlert('请选择徽章等级', 'warning');
            return;
        }
        
        // 构造请求数据
        const badgeData = {
            badgeName: badgeName,
            description: badgeDescription,
            badgeType: badgeType,
            level: parseInt(badgeLevel),
            status: 1,              // 添加默认状态
            rewardPoints: 0         // 添加默认奖励积分
        };
        
        // 如果有图标URL，则添加到数据中
        if (badgeIconUrl) {
            badgeData.iconUrl = badgeIconUrl;
        }
        // 如果没有图标URL，但有徽章ID（编辑模式），则不强制要求图标URL
        else if (badgeId) {
            // 编辑模式下允许保留原有图标
        }
        // 如果是新建徽章且没有图标，则使用默认图标
        else {
            // 使用默认图标URL
            badgeData.iconUrl = '/images/icons/default-badge.png';
        }
        
        // 确定请求方法和URL
        let method = 'POST';
        let url = '/api/admin/badges';
        if (badgeId) {
            method = 'PUT';
            url = '/api/admin/badges';
            badgeData.badgeId = parseInt(badgeId);
        }
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(badgeData)
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                showAlert(badgeId ? '徽章更新成功' : '徽章添加成功', 'success');
                
                // 隐藏模态框
                const modalElement = document.getElementById('addBadgeModal');
                const modal = bootstrap.Modal.getInstance(modalElement);
                if (modal) {
                    modal.hide();
                }
                
                // 重新加载徽章列表
                loadBadges();
                
                // 清空表单
                document.getElementById('badgeForm')?.reset();
                document.getElementById('badgeId').value = '';
                document.getElementById('modalBadgeIconUrl').value = '';
                const currentIconPreview = document.getElementById('currentIconPreview');
                if (currentIconPreview) {
                    currentIconPreview.style.display = 'none';
                }
            } else {
                throw new Error(result.message || (badgeId ? '更新徽章失败' : '添加徽章失败'));
            }
        } else {
            const errorResult = await response.json().catch(() => ({}));
            throw new Error(errorResult.message || (badgeId ? '更新徽章失败' : '添加徽章失败'));
        }
    } catch (error) {
        console.error('保存徽章时出错:', error);
        showAlert('保存徽章失败: ' + error.message, 'error');
    }
}

// 文件上传功能
document.getElementById('modalBadgeIcon')?.addEventListener('change', async function(event) {
    try {
        const file = event.target.files[0];
        if (!file) return;
        
        // 验证文件类型
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp'];
        if (!allowedTypes.includes(file.type)) {
            showAlert('请选择有效的图片文件 (JPG, PNG, GIF, BMP)', 'warning');
            return;
        }
        
        // 验证文件大小 (最大5MB)
        if (file.size > 5 * 1024 * 1024) {
            showAlert('文件大小不能超过5MB', 'warning');
            return;
        }
        
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const formData = new FormData();
        formData.append('file', file);
        
        const response = await fetch('/api/admin/badges/upload-icon', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                // 设置图标URL到隐藏字段
                document.getElementById('modalBadgeIconUrl').value = result.data;
                
                // 显示预览
                const currentIconPreview = document.getElementById('currentIconPreview');
                const currentIconImg = document.getElementById('currentIconImg');
                if (currentIconPreview && currentIconImg) {
                    currentIconImg.src = result.data;
                    currentIconPreview.style.display = 'block';
                }
                
                showAlert('图标上传成功', 'success');
            } else {
                throw new Error(result.message || '上传图标失败');
            }
        } else {
            throw new Error('上传图标失败');
        }
    } catch (error) {
        console.error('上传图标时出错:', error);
        showAlert('上传图标失败: ' + error.message, 'error');
    }
});

function showAlert(message, type) {
    try {
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
    } catch (error) {
        console.error('显示提示信息时出错:', error);
    }
}