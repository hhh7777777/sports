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
    
    // 页面加载时获取日志列表
    loadLogs();
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

async function loadLogs() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch('/api/admin/logs', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderLogs(result.data);
            }
        } else {
            throw new Error('获取日志列表失败');
        }
    } catch (error) {
        console.error('加载日志列表时出错:', error);
        showAlert('加载日志列表失败: ' + error.message, 'error');
    }
}

function renderLogs(logs) {
    const tbody = document.getElementById('logsTableBody');
    tbody.innerHTML = '';
    
    if (!Array.isArray(logs) || logs.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="5" class="text-center">暂无日志数据</td>';
        tbody.appendChild(row);
        return;
    }
    
    logs.forEach(log => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${log.operationTime ? new Date(log.operationTime).toLocaleString() : ''}</td>
            <td>${getLogLevelBadge(log.logLevel)}</td>
            <td>${log.module || ''}</td>
            <td>${log.message || ''}</td>
            <td>${log.ipAddress || ''}</td>
        `;
        tbody.appendChild(row);
    });
}

function getLogLevelBadge(level) {
    switch (level) {
        case 'ERROR':
            return '<span class="log-level-error">ERROR</span>';
        case 'WARN':
            return '<span class="log-level-warn">WARN</span>';
        case 'INFO':
            return '<span class="log-level-info">INFO</span>';
        default:
            return level;
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

async function filterLogs() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const logLevel = document.getElementById('logLevel').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        // 构建查询参数
        let url = '/api/admin/logs';
        const params = new URLSearchParams();
        
        if (logLevel) params.append('logLevel', logLevel);
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        
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
                renderLogs(result.data);
            }
        } else {
            throw new Error('筛选日志失败');
        }
    } catch (error) {
        console.error('筛选日志时出错:', error);
        showAlert('筛选日志失败: ' + error.message, 'error');
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