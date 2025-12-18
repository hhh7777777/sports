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
        filterLogs();
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

function filterLogs() {
    const logLevel = document.getElementById('logLevel').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    // 这里应该调用API进行筛选，目前只是模拟
    console.log('筛选条件:', { logLevel, startDate, endDate });
    showAlert('筛选功能演示', 'info');
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