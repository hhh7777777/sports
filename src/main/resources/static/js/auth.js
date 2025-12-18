class AuthManager {
    constructor() {
        this.currentUser = null;
        this.token = null;
    }

    async checkAuth() {
        this.token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');

        if (!this.token) {
            return false;
        }

        try {
            const response = await fetch('/api/auth/validate', {
                headers: {
                    'Authorization': `Bearer ${this.token}`
                }
            });

            if (response.ok) {
                const userData = await response.json();
                this.currentUser = userData;
                return true;
            }
        } catch (error) {
            console.error('Auth validation error:', error);
        }

        this.clearAuth();
        return false;
    }

    async login(credentials) {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                this.token = result.data.token;
                this.currentUser = result.data.user;

                if (credentials.rememberMe) {
                    localStorage.setItem('accessToken', this.token);
                } else {
                    sessionStorage.setItem('accessToken', this.token);
                }

                Utils.showAlert('登录成功！', 'success');
                return { success: true, data: result.data };
            } else {
                throw new Error(result.message || '登录失败');
            }
        } catch (error) {
            Utils.showAlert(error.message, 'error');
            return { success: false, error: error.message };
        }
    }

    async register(userData) {
        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                Utils.showAlert('注册成功！', 'success');
                return { success: true };
            } else {
                throw new Error(result.message || '注册失败');
            }
        } catch (error) {
            Utils.showAlert(error.message, 'error');
            return { success: false, error: error.message };
        }
    }

    async logout() {
        try {
            if (this.token) {
                await fetch('/api/auth/logout', {
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
            Utils.showAlert('已退出登录', 'info');
            window.location.href = '/';
        }
    }

    clearAuth() {
        this.currentUser = null;
        this.token = null;
        localStorage.removeItem('accessToken');
        sessionStorage.removeItem('accessToken');
    }

    getAuthHeaders() {
        return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    }
}

// 全局认证管理器实例
window.authManager = new AuthManager();