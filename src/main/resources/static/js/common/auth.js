class AuthManager {
    constructor() {
        this.currentUser = null;
        this.token = null;
    }

    async checkAuth() {
        this.token = CommonUtils.storage.get('accessToken');

        if (!this.token) {
            return false;
        }

        try {
            const response = await fetch('/api/user/validate-token', {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });

            if (response.ok) {
                const result = await response.json();
                if (result.code === 200) {
                    this.currentUser = result.data;
                    return true;
                }
            }
            // 如果响应不成功或者返回码不是200，清除认证信息
            this.clearAuth();
            return false;
        } catch (error) {
            console.error('Auth validation error:', error);
            // 出现错误时清除认证信息
            this.clearAuth();
            return false;
        }
    }

    async login(credentials) {
        try {
            const response = await fetch('/api/user/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            });

            const result = await response.json();

            if (response.ok && result.code === 200) {
                this.token = result.data.token;
                this.currentUser = result.data.user;

                if (credentials.rememberMe) {
                    CommonUtils.storage.set('accessToken', this.token);
                } else {
                    sessionStorage.setItem('accessToken', this.token);
                }

                CommonUtils.showAlert('登录成功！', 'success');
                return { success: true, data: result.data };
            } else {
                throw new Error(result.message || '登录失败');
            }
        } catch (error) {
            CommonUtils.showAlert(error.message, 'error');
            return { success: false, error: error.message };
        }
    }

    async register(userData) {
        try {
            const response = await fetch('/api/user/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            const result = await response.json();

            if (response.ok && result.code === 200) {
                CommonUtils.showAlert('注册成功！', 'success');
                return { success: true };
            } else {
                throw new Error(result.message || '注册失败');
            }
        } catch (error) {
            CommonUtils.showAlert(error.message, 'error');
            return { success: false, error: error.message };
        }
    }

    async logout() {
        try {
            if (this.token) {
                await fetch('/api/user/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.token}`
                    }
                });
            }
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            this.clearAuth();
            CommonUtils.showAlert('已退出登录', 'info');
            window.location.href = '/';
        }
    }

    clearAuth() {
        this.currentUser = null;
        this.token = null;
        CommonUtils.storage.remove('accessToken');
        sessionStorage.removeItem('accessToken');
    }

    getAuthHeaders() {
        return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    }
}

// 全局认证管理器实例
try {
    window.authManager = new AuthManager();
} catch (error) {
    console.error('Failed to initialize AuthManager:', error);
}