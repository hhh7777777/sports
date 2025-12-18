document.addEventListener('DOMContentLoaded', function() {
    // 检查管理员是否已登录
    checkAdminAuth();
    
    // 绑定退出登录事件
    document.getElementById('adminLogoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        adminLogout();
    });
    
    // 初始化日期选择器
    initDatePickers();
    
    // 绑定表单搜索事件
    document.getElementById('filterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        filterBehaviors();
    });
    
    // 绑定删除按钮事件
    document.getElementById('behaviorsTableBody').addEventListener('click', function(e) {
        if (e.target.closest('.delete-btn')) {
            const behaviorId = e.target.closest('.delete-btn').dataset.id;
            deleteBehavior(behaviorId);
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

function initDatePickers() {
    // 初始化日期选择器
    flatpickr("#startDate", {
        locale: "zh",
        dateFormat: "Y-m-d",
    });
    
    flatpickr("#endDate", {
        locale: "zh",
        dateFormat: "Y-m-d",
    });
}

function filterBehaviors() {
    const userId = document.getElementById('userId').value;
    const activityType = document.getElementById('activityType').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    // 这里应该调用API进行筛选，目前只是模拟
    console.log('筛选条件:', { userId, activityType, startDate, endDate });
    showAlert('筛选功能演示', 'info');
}

function deleteBehavior(behaviorId) {
    if (confirm('确定要删除这条运动记录吗？')) {
        // 模拟删除运动记录
        console.log('删除运动记录ID:', behaviorId);
        showAlert('运动记录删除成功', 'success');
        
        // 在实际应用中，这里应该调用API删除运动记录
        // 删除后重新加载列表
        setTimeout(() => {
            location.reload();
        }, 1000);
    }
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