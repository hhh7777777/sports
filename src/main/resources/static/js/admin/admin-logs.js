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
        
        // 构建查询参数 - 使用新的API端点来获取所有操作日志
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
        row.innerHTML = '<td colspan="6" class="text-center">暂无日志数据</td>';
        tbody.appendChild(row);
        
        // 更新分页控件
        renderPagination(1, 1, 0);
        return;
    }
    
    logs.forEach(log => {
        const row = document.createElement('tr');
        // OperationLog实体类中的字段
        row.innerHTML = `
            <td>${log.operationTime ? new Date(log.operationTime).toLocaleString() : ''}</td>
            <td>${getUserTypeBadge(log.userType)}</td>
            <td>${getOperationTypeBadge(log.operationType)}</td>
            <td>${log.operation || ''}</td>
            <td>${log.detail || ''}</td>
            <td>${log.ipAddress || ''}</td>
        `;
        tbody.appendChild(row);
    });
    
    // 更新分页控件
    renderPagination(data.currentPage, data.totalPages, data.totalCount);
}

function getUserTypeBadge(userType) {
    if (!userType) return '<span class="badge bg-secondary">未知</span>';
    if (userType === 'ADMIN') {
        return '<span class="badge bg-danger">管理员</span>';
    } else if (userType === 'USER') {
        return '<span class="badge bg-success">普通用户</span>';
    } else {
        return `<span class="badge bg-secondary">${userType}</span>`;
    }
}

function getOperationTypeBadge(operationType) {
    if (!operationType) return '<span class="badge bg-secondary">未知</span>';
    switch (operationType) {
        case 'LOGIN':
            return '<span class="badge bg-info">登录</span>';
        case 'CREATE':
            return '<span class="badge bg-primary">创建</span>';
        case 'UPDATE':
            return '<span class="badge bg-warning">更新</span>';
        case 'DELETE':
            return '<span class="badge bg-danger">删除</span>';
        case 'READ':
            return '<span class="badge bg-success">查看</span>';
        case 'AUTH':
            return '<span class="badge bg-info">认证</span>';
        default:
            return `<span class="badge bg-secondary">${operationType}</span>`;
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

async function filterLogs(page = 1) {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 使用新的参数获取过滤条件
        const userType = document.getElementById('userType').value;
        const operationType = document.getElementById('operationType').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        // 构建查询参数
        let url = `/api/admin/logs?page=${page}&size=10`;
        const params = new URLSearchParams();
        
        if (userType) {
            params.append('userType', userType);
        }
        if (operationType) {
            params.append('operationType', operationType);
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
    const userType = document.getElementById('userType').value;
    const operationType = document.getElementById('operationType').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    if (userType || operationType || startDate || endDate) {
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