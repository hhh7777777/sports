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

function filterUsers() {
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const status = document.getElementById('status').value;
    
    // 这里应该调用API进行筛选，目前只是模拟
    console.log('筛选条件:', { username, email, status });
    showAlert('筛选功能演示', 'info');
}

function editUser(userId) {
    // 模拟编辑用户
    document.getElementById('addUserModalLabel').textContent = '编辑用户';
    document.getElementById('userId').value = userId;
    document.getElementById('modalUsername').value = '用户' + userId;
    document.getElementById('modalEmail').value = 'user' + userId + '@example.com';
    document.getElementById('modalPassword').value = '';
    
    // 显示模态框
    const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
    modal.show();
}

function deleteUser(userId) {
    if (confirm('确定要删除这个用户吗？')) {
        // 模拟删除用户
        console.log('删除用户ID:', userId);
        showAlert('用户删除成功', 'success');
        
        // 在实际应用中，这里应该调用API删除用户
        // 删除后重新加载用户列表
        setTimeout(() => {
            location.reload();
        }, 1000);
    }
}

function saveUser() {
    const userId = document.getElementById('userId').value;
    const username = document.getElementById('modalUsername').value;
    const email = document.getElementById('modalEmail').value;
    const password = document.getElementById('modalPassword').value;
    const status = document.getElementById('modalStatus').value;
    
    if (!username || !email) {
        showAlert('请填写必填字段', 'warning');
        return;
    }
    
    // 模拟保存用户
    console.log('保存用户:', { userId, username, email, password, status });
    showAlert(userId ? '用户更新成功' : '用户添加成功', 'success');
    
    // 隐藏模态框
    const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
    modal.hide();
    
    // 重新加载用户列表
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