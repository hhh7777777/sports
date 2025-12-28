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
    
    // 页面加载时获取运动记录列表
    loadBehaviors();
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

async function loadBehaviors() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        // 先加载行为类型下拉框
        await loadBehaviorTypes();
        
        const response = await fetch('/api/admin/behaviors', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderBehaviors(result.data);
            }
        } else {
            throw new Error('获取运动记录列表失败');
        }
    } catch (error) {
        console.error('加载运动记录列表时出错:', error);
        showAlert('加载运动记录列表失败: ' + error.message, 'error');
    }
}

async function loadBehaviorTypes() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch('/api/admin/behavior-types', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const activityTypeSelect = document.getElementById('activityType');
                // 保存第一个选项（全部）
                const firstOption = activityTypeSelect.firstElementChild;
                // 清空选项
                activityTypeSelect.innerHTML = '';
                // 恢复第一个选项
                activityTypeSelect.appendChild(firstOption);
                // 添加行为类型选项
                result.data.forEach(type => {
                    const option = document.createElement('option');
                    option.value = type.typeId;
                    option.textContent = type.typeName;
                    activityTypeSelect.appendChild(option);
                });
            }
        }
    } catch (error) {
        console.error('加载行为类型时出错:', error);
    }
}

function renderBehaviors(behaviors) {
    const tbody = document.getElementById('behaviorsTableBody');
    tbody.innerHTML = '';
    
    if (!Array.isArray(behaviors) || behaviors.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="9" class="text-center">暂无运动记录数据</td>';
        tbody.appendChild(row);
        return;
    }
    
    behaviors.forEach(behavior => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${behavior.recordId || ''}</td>
            <td>${behavior.userId || ''}</td>
            <td>${behavior.userName || behavior.userId || ''}</td>
            <td>${behavior.typeName || behavior.type || '未知类型'}</td>
            <td>${behavior.duration || 0}</td>
            <td>${behavior.distance || 0}</td>
            <td>${behavior.calories || 0}</td>
            <td>${behavior.createTime ? new Date(behavior.createTime).toLocaleString() : ''}</td>
            <td>
                <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${behavior.recordId}">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
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

async function filterBehaviors() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const userId = document.getElementById('userId').value;
        const activityType = document.getElementById('activityType').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        // 构建查询参数
        let url = '/api/admin/behaviors';
        const params = new URLSearchParams();
        
        if (userId) params.append('userId', userId);
        if (activityType) params.append('typeId', activityType);
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
                renderBehaviors(result.data);
            }
        } else {
            throw new Error('筛选运动记录失败');
        }
    } catch (error) {
        console.error('筛选运动记录时出错:', error);
        showAlert('筛选运动记录失败: ' + error.message, 'error');
    }
}

async function deleteBehavior(behaviorId) {
    if (!confirm('确定要删除这条运动记录吗？')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch(`/api/admin/behaviors/${behaviorId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                showAlert('运动记录删除成功', 'success');
                // 重新加载列表
                loadBehaviors();
            } else {
                throw new Error(result.message || '删除运动记录失败');
            }
        } else {
            throw new Error('删除运动记录失败');
        }
    } catch (error) {
        console.error('删除运动记录时出错:', error);
        showAlert('删除运动记录失败: ' + error.message, 'error');
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