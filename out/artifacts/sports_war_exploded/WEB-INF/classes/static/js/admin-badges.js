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

function filterBadges() {
    const badgeName = document.getElementById('badgeName').value;
    const badgeType = document.getElementById('badgeType').value;
    
    // 这里应该调用API进行筛选，目前只是模拟
    console.log('筛选条件:', { badgeName, badgeType });
    showAlert('筛选功能演示', 'info');
}

function editBadge(badgeId) {
    // 模拟编辑徽章
    document.getElementById('addBadgeModalLabel').textContent = '编辑徽章';
    document.getElementById('badgeId').value = badgeId;
    document.getElementById('modalBadgeName').value = '徽章' + badgeId;
    document.getElementById('modalBadgeDescription').value = '这是第' + badgeId + '个徽章的描述';
    
    // 显示模态框
    const modal = new bootstrap.Modal(document.getElementById('addBadgeModal'));
    modal.show();
}

function deleteBadge(badgeId) {
    if (confirm('确定要删除这个徽章吗？')) {
        // 模拟删除徽章
        console.log('删除徽章ID:', badgeId);
        showAlert('徽章删除成功', 'success');
        
        // 在实际应用中，这里应该调用API删除徽章
        // 删除后重新加载列表
        setTimeout(() => {
            location.reload();
        }, 1000);
    }
}

function saveBadge() {
    const badgeId = document.getElementById('badgeId').value;
    const badgeName = document.getElementById('modalBadgeName').value;
    const badgeDescription = document.getElementById('modalBadgeDescription').value;
    const badgeIcon = document.getElementById('modalBadgeIcon').value;
    const badgeColor = document.getElementById('modalBadgeColor').value;
    const badgeType = document.getElementById('modalBadgeType').value;
    
    if (!badgeName || !badgeDescription) {
        showAlert('请填写必填字段', 'warning');
        return;
    }
    
    // 模拟保存徽章
    console.log('保存徽章:', { badgeId, badgeName, badgeDescription, badgeIcon, badgeColor, badgeType });
    showAlert(badgeId ? '徽章更新成功' : '徽章添加成功', 'success');
    
    // 隐藏模态框
    const modal = bootstrap.Modal.getInstance(document.getElementById('addBadgeModal'));
    modal.hide();
    
    // 重新加载徽章列表
    setTimeout(() => {
        location.reload();
    }, 1000);
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