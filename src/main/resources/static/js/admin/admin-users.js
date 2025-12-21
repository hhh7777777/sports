document.addEventListener('DOMContentLoaded', function() {
    // 检查管理员是否已登录
    checkAdminAuth();
    
    // 绑定退出登录事件
    document.getElementById('adminLogoutBtn').addEventListener('click', function(e) {
        e.preventDefault();
        adminLogout();
    });
    
    // 绑定添加用户按钮事件
    document.getElementById('saveUserBtn').addEventListener('click', saveUser);
    
    // 绑定表单搜索事件
    document.getElementById('filterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        filterUsers();
    });
    
    // 绑定编辑和删除按钮事件
    document.getElementById('usersTableBody').addEventListener('click', function(e) {
        if (e.target.closest('.edit-btn')) {
            const userId = e.target.closest('.edit-btn').dataset.id;
            editUser(userId);
        }
        
        if (e.target.closest('.delete-btn')) {
            const userId = e.target.closest('.delete-btn').dataset.id;
            deleteUser(userId);
        }
    });
    
    // 页面加载时获取用户列表
    loadUsers();
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

async function loadUsers() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch('/api/admin/users', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                renderUsers(result.data);
            }
        } else {
            throw new Error('获取用户列表失败');
        }
    } catch (error) {
        console.error('加载用户列表时出错:', error);
        showAlert('加载用户列表失败: ' + error.message, 'error');
    }
}

function renderUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '';
    
    if (!Array.isArray(users) || users.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="7" class="text-center">暂无用户数据</td>';
        tbody.appendChild(row);
        return;
    }
    
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.userId || ''}</td>
            <td>${user.username || ''}</td>
            <td>${user.email || ''}</td>
            <td>${user.registerTime ? new Date(user.registerTime).toLocaleDateString() : ''}</td>
            <td>${user.lastLoginTime ? new Date(user.lastLoginTime).toLocaleString() : ''}</td>
            <td>${getStatusBadge(user.userStatus)}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary edit-btn" data-id="${user.userId}">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger delete-btn" data-id="${user.userId}">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function getStatusBadge(status) {
    switch (status) {
        case 1:
            return '<span class="badge bg-success">活跃</span>';
        case 0:
            return '<span class="badge bg-warning">未激活</span>';
        case -1:
            return '<span class="badge bg-danger">封禁</span>';
        default:
            return '<span class="badge bg-secondary">未知</span>';
    }
}

async function filterUsers() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const status = document.getElementById('status').value;
        
        // 构建查询参数
        let url = '/api/admin/users';
        const params = new URLSearchParams();
        
        if (username) params.append('username', username);
        if (email) params.append('email', email);
        if (status) params.append('status', status);
        
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
                renderUsers(result.data);
            }
        } else {
            throw new Error('筛选用户失败');
        }
    } catch (error) {
        console.error('筛选用户时出错:', error);
        showAlert('筛选用户失败: ' + error.message, 'error');
    }
}

async function editUser(userId) {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch(`/api/admin/users/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const user = result.data;
                
                document.getElementById('addUserModalLabel').textContent = '编辑用户';
                document.getElementById('userId').value = user.userId;
                document.getElementById('modalUsername').value = user.username;
                document.getElementById('modalEmail').value = user.email;
                document.getElementById('modalPassword').value = '';
                document.getElementById('modalStatus').value = user.userStatus || 1;
                
                // 显示模态框
                const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
                modal.show();
            }
        } else {
            throw new Error('获取用户信息失败');
        }
    } catch (error) {
        console.error('编辑用户时出错:', error);
        showAlert('获取用户信息失败: ' + error.message, 'error');
    }
}

async function deleteUser(userId) {
    if (!confirm('确定要删除这个用户吗？')) {
        return;
    }
    
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const response = await fetch(`/api/admin/users/${userId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                showAlert('用户删除成功', 'success');
                // 重新加载用户列表
                loadUsers();
            } else {
                throw new Error(result.message || '删除用户失败');
            }
        } else {
            throw new Error('删除用户失败');
        }
    } catch (error) {
        console.error('删除用户时出错:', error);
        showAlert('删除用户失败: ' + error.message, 'error');
    }
}

async function saveUser() {
    try {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) {
            window.location.href = '/admin/login';
            return;
        }
        
        const userId = document.getElementById('userId').value;
        const username = document.getElementById('modalUsername').value;
        const email = document.getElementById('modalEmail').value;
        const password = document.getElementById('modalPassword').value;
        const status = document.getElementById('modalStatus').value;
        
        if (!username || !email) {
            showAlert('请填写必填字段', 'warning');
            return;
        }
        
        const userData = {
            username: username,
            email: email,
            userStatus: parseInt(status)
        };
        
        // 如果有密码，则添加到数据中
        if (password) {
            userData.password = password;
        }
        
        let url = '/api/admin/users';
        let method = 'POST';
        
        // 如果是编辑用户，则使用PUT方法
        if (userId) {
            url += `/${userId}`;
            method = 'PUT';
            userData.userId = parseInt(userId);
        }
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                showAlert(userId ? '用户更新成功' : '用户添加成功', 'success');
                
                // 隐藏模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
                modal.hide();
                
                // 重新加载用户列表
                loadUsers();
            } else {
                throw new Error(result.message || (userId ? '更新用户失败' : '添加用户失败'));
            }
        } else {
            throw new Error(userId ? '更新用户失败' : '添加用户失败');
        }
    } catch (error) {
        console.error('保存用户时出错:', error);
        showAlert('保存用户失败: ' + error.message, 'error');
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