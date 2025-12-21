class Router {
    constructor() {
        this.routes = {};
        this.currentRoute = null;
        this.init();
    }

    init() {
        // 定义路由
        this.routes = {
            '/': this.renderHomePage.bind(this),
            '/login': this.renderLoginPage.bind(this),
            '/register': this.renderRegisterPage.bind(this),
            '/dashboard': this.renderDashboard.bind(this),
            '/behavior': this.renderBehaviorPage.bind(this),
            '/achievements': this.renderAchievementsPage.bind(this),
            '/profile': this.renderProfilePage.bind(this),
            '/admin/login': this.renderAdminLoginPage.bind(this),
            '/admin/dashboard': this.renderAdminDashboard.bind(this)
        };

        // 监听路由变化
        window.addEventListener('popstate', this.handleRouteChange.bind(this));

        // 初始路由处理
        this.handleRouteChange();
    }

    async handleRouteChange() {
        const path = window.location.pathname;
        const handler = this.routes[path] || this.routes['/'];

        if (handler) {
            try {
                // 检查需要认证的路由
                const protectedRoutes = ['/dashboard', '/behavior', '/achievements', '/profile', '/admin/dashboard'];
                if (protectedRoutes.includes(path) && !path.startsWith('/admin')) {
                    const isAuthenticated = await this.checkUserAuth();
                    if (!isAuthenticated) {
                        window.location.href = '/login';
                        return;
                    }
                }

                if (path.startsWith('/admin') && path !== '/admin/login') {
                    const isAdminAuthenticated = await this.checkAdminAuth();
                    if (!isAdminAuthenticated) {
                        window.location.href = '/admin/login';
                        return;
                    }
                }

                this.currentRoute = path;
                await handler();
            } catch (error) {
                console.error('路由处理错误:', error);
                if (typeof Utils !== 'undefined' && Utils.showAlert) {
                    Utils.showAlert('页面加载失败: ' + error.message, 'error');
                }
            }
        }
    }

    async checkUserAuth() {
        // 检查用户认证状态
        if (typeof authManager !== 'undefined' && authManager.checkAuth) {
            return await authManager.checkAuth();
        }
        return false;
    }

    async checkAdminAuth() {
        const token = localStorage.getItem('adminToken') || sessionStorage.getItem('adminToken');
        if (!token) return false;

        try {
            const response = await fetch('/api/admin/validate', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            return response.ok;
        } catch (error) {
            console.error('Admin auth check error:', error);
            return false;
        }
    }

    async navigate(path, data = {}) {
        try {
            // 对于需要后端处理的路径，直接跳转
            const backendPaths = ['/dashboard', '/behavior', '/achievements', '/profile', '/login', '/register', '/', '/admin'];
            if (backendPaths.includes(path) || path.startsWith('/admin/')) {
                window.location.href = path;
                return;
            }
            
            window.history.pushState(data, '', path);
            await this.handleRouteChange();
        } catch (error) {
            console.error('导航错误:', error);
            if (typeof Utils !== 'undefined' && Utils.showAlert) {
                Utils.showAlert('页面跳转失败: ' + error.message, 'error');
            }
        }
    }

    // 页面渲染方法
    async renderHomePage() {
        try {
            const html = await this.loadTemplate('home');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            this.attachHomeEventListeners();
        } catch (error) {
            console.error('首页渲染错误:', error);
        }
    }

    async renderLoginPage() {
        try {
            const html = await this.loadTemplate('login');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            this.attachLoginEventListeners();
        } catch (error) {
            console.error('登录页渲染错误:', error);
        }
    }

    async renderRegisterPage() {
        try {
            const html = await this.loadTemplate('register');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            this.attachRegisterEventListeners();
        } catch (error) {
            console.error('注册页渲染错误:', error);
        }
    }

    async renderDashboard() {
        try {
            const html = await this.loadTemplate('dashboard');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            await this.loadDashboardData();
        } catch (error) {
            console.error('仪表板渲染错误:', error);
        }
    }

    async renderBehaviorPage() {
        try {
            const html = await this.loadTemplate('behavior');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            await this.loadBehaviorData();
        } catch (error) {
            console.error('行为页面渲染错误:', error);
        }
    }

    async renderAchievementsPage() {
        try {
            const html = await this.loadTemplate('achievements');
            const appElement = document.getElementById('app');
            if (appElement) {
                appElement.innerHTML = html;
            }
            await this.loadAchievementsData();
        } catch (error) {
            console.error('成就页面渲染错误:', error);
        }
    }

    // 模板加载方法
    async loadTemplate(templateName) {
        try {
            // 特殊处理某些模板
            if (['dashboard', 'behavior', 'achievements', 'profile'].includes(templateName)) {
                // 对于这些页面，直接跳转到对应的URL
                window.location.href = `/${templateName}`;
                return '';
            }
            
            // 尝试从服务器加载模板
            const response = await fetch(`/templates/${templateName}.html`);
            if (!response.ok) throw new Error('Template not found');
            return await response.text();
        } catch (error) {
            console.warn('模板加载失败，使用备用模板:', error);
            return this.getFallbackTemplate(templateName);
        }
    }

    getFallbackTemplate(templateName) {
        // 返回内联模板作为备用
        const templates = {
            'home': '<div class="container mt-5"><h1 class="text-center">欢迎来到运动健康管理系统</h1><p class="text-center">开始您的健康之旅</p></div>',
            'login': '<div class="container mt-5"><h1 class="text-center">用户登录</h1><div class="row justify-content-center"><div class="col-md-6"><div class="card"><div class="card-body"><p>登录表单占位符</p></div></div></div></div></div>',
            'register': '<div class="container mt-5"><h1 class="text-center">用户注册</h1><div class="row justify-content-center"><div class="col-md-6"><div class="card"><div class="card-body"><p>注册表单占位符</p></div></div></div></div></div>'
        };
        return templates[templateName] || '<div class="container mt-5"><h2 class="text-center">页面未找到</h2></div>';
    }

    // 事件监听器附加方法
    attachHomeEventListeners() {
        // 首页事件处理
    }

    attachLoginEventListeners() {
        const form = document.getElementById('loginForm');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                const formData = new FormData(form);
                const credentials = {
                    username: formData.get('username'),
                    password: formData.get('password'),
                    captcha: formData.get('captcha'),
                    rememberMe: formData.get('rememberMe') === 'on'
                };

                if (typeof authManager !== 'undefined' && authManager.login) {
                    const result = await authManager.login(credentials);
                    if (result.success) {
                        this.navigate('/dashboard');
                    }
                }
            });
        }
    }

    attachRegisterEventListeners() {
        // 注册页事件处理
    }
}

// 页面特定的数据加载方法
Router.prototype.loadDashboardData = async function() {
    try {
        if (typeof authManager === 'undefined' || !authManager.getAuthHeaders) {
            throw new Error('认证管理器未初始化');
        }
        
        const response = await fetch('/api/user/dashboard', {
            headers: authManager.getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            this.updateDashboardUI(data);
        }
    } catch (error) {
        console.error('加载仪表板数据失败:', error);
        if (typeof Utils !== 'undefined' && Utils.showAlert) {
            Utils.showAlert('加载仪表板数据失败: ' + error.message, 'error');
        }
    }
};

Router.prototype.loadBehaviorData = async function() {
    // 运动记录页面数据加载
    console.log('加载运动记录数据');
};

Router.prototype.loadAchievementsData = async function() {
    // 成就页面数据加载
    console.log('加载成就数据');
};

// 初始化路由
try {
    window.router = new Router();
} catch (error) {
    console.error('路由器初始化失败:', error);
}