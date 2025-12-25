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
        filterLogs(1); // 搜索时重置到第一页
    });
    
    // 页面加载时获取日志列表
    loadLogs(1);
});

// 当前页码
let currentPage = 1;

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

async function loadLogs(page = 1) {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 构建查询参数
        let url = `/api/admin/logs?page=${page}&size=10`;
        
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderLogs(result.data);
            } else {
                throw new Error(result.message || '获取日志列表失败');
            }
        } else {
            throw new Error('获取日志列表失败');
        }
    } catch (error) {
        console.error('加载日志列表时出错:', error);
        showAlert('加载日志列表失败: ' + error.message, 'error');
    }
}

function renderLogs(data) {
    const logs = data.logs;
    const tbody = document.getElementById('logsTableBody');
    tbody.innerHTML = '';
    
    if (!Array.isArray(logs) || logs.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="5" class="text-center">暂无日志数据</td>';
        tbody.appendChild(row);
        
        // 更新分页控件
        renderPagination(1, 1, 0);
        return;
    }
    
    logs.forEach(log => {
        const row = document.createElement('tr');
        // AdminLog实体类中的字段
        row.innerHTML = `
            <td>${log.operationTime ? new Date(log.operationTime).toLocaleString() : ''}</td>
            <td>${getLogLevelBadge(log.operation)}</td>
            <td>${log.operation || ''}</td>
            <td>${log.detail || ''}</td>
            <td>${log.ipAddress || ''}</td>
        `;
        tbody.appendChild(row);
    });
    
    // 更新分页控件
    renderPagination(data.currentPage, data.totalPages, data.totalCount);
}

function getLogLevelBadge(operation) {
    // 根据操作类型简单判断日志级别
    if (operation && typeof operation === 'string') {
        if (operation.includes('删除') || operation.includes('错误') || operation.includes('失败')) {
            return '<span class="log-level-error">ERROR</span>';
        } else if (operation.includes('警告') || operation.includes('注意')) {
            return '<span class="log-level-warn">WARN</span>';
        } else {
            return '<span class="log-level-info">INFO</span>';
        }
    }
    return '<span class="log-level-info">INFO</span>';
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

async function filterLogs(page = 1) {
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
        let url = `/api/admin/logs?page=${page}&size=10`;
        const params = new URLSearchParams();
        
        // 根据前端的logLevel筛选转换为后端的operation参数
        if (logLevel) {
            // 这里可以根据需要做映射
            params.append('operation', logLevel);
        }
        if (startDate) {
            // 需要转换为完整的时间格式
            params.append('startTime', startDate + ' 00:00:00');
        }
        if (endDate) {
            // 需要转换为完整的时间格式
            params.append('endTime', endDate + ' 23:59:59');
        }
        
        if (params.toString()) {
            url += '&' + params.toString();
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
            } else {
                throw new Error(result.message || '筛选日志失败');
            }
        } else {
            throw new Error('筛选日志失败');
        }
    } catch (error) {
        console.error('筛选日志时出错:', error);
        showAlert('筛选日志失败: ' + error.message, 'error');
    }
}

function renderPagination(currentPage, totalPages, totalCount) {
    const paginationElement = document.querySelector('.pagination');
    paginationElement.innerHTML = '';
    
    // 上一页按钮
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="#" ${currentPage !== 1 ? `onclick="changePage(${currentPage - 1}); return false;"` : ''}>上一页</a>`;
    paginationElement.appendChild(prevLi);
    
    // 页码按钮 (最多显示5个页码)
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, startPage + 4);
    
    if (endPage - startPage < 4) {
        startPage = Math.max(1, endPage - 4);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item ${i === currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `<a class="page-link" href="#" onclick="changePage(${i}); return false;">${i}</a>`;
        paginationElement.appendChild(pageLi);
    }
    
    // 下一页按钮
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages || totalPages === 0 ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="#" ${currentPage !== totalPages && totalPages > 0 ? `onclick="changePage(${currentPage + 1}); return false;"` : ''}>下一页</a>`;
    paginationElement.appendChild(nextLi);
    
    // 更新全局当前页
    window.currentPage = currentPage;
}

// 页面切换函数
async function changePage(page) {
    // 检查是否有筛选条件
    const logLevel = document.getElementById('logLevel').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    if (logLevel || startDate || endDate) {
        // 如果有筛选条件，使用筛选函数
        await filterLogs(page);
    } else {
        // 否则使用普通加载函数
        await loadLogs(page);
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