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
            // 检查需要认证的路由
            const protectedRoutes = ['/dashboard', '/behavior', '/achievements', '/profile', '/admin/dashboard'];
            if (protectedRoutes.includes(path) && !path.startsWith('/admin')) {
                const isAuthenticated = await authManager.checkAuth();
                if (!isAuthenticated) {
                    this.navigate('/login');
                    return;
                }
            }

            if (path.startsWith('/admin') && path !== '/admin/login') {
                const isAdminAuthenticated = await this.checkAdminAuth();
                if (!isAdminAuthenticated) {
                    this.navigate('/admin/login');
                    return;
                }
            }

            this.currentRoute = path;
            await handler();
        }
    }

    async navigate(path, data = {}) {
        window.history.pushState(data, '', path);
        await this.handleRouteChange();
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
            return false;
        }
    }

    // 页面渲染方法
    async renderHomePage() {
        const html = await this.loadTemplate('home');
        document.getElementById('app').innerHTML = html;
        this.attachHomeEventListeners();
    }

    async renderLoginPage() {
        const html = await this.loadTemplate('login');
        document.getElementById('app').innerHTML = html;
        this.attachLoginEventListeners();
    }

    async renderRegisterPage() {
        const html = await this.loadTemplate('register');
        document.getElementById('app').innerHTML = html;
        this.attachRegisterEventListeners();
    }

    async renderDashboard() {
        const html = await this.loadTemplate('dashboard');
        document.getElementById('app').innerHTML = html;
        await this.loadDashboardData();
    }

    async renderBehaviorPage() {
        const html = await this.loadTemplate('behavior');
        document.getElementById('app').innerHTML = html;
        await this.loadBehaviorData();
    }

    async renderAchievementsPage() {
        const html = await this.loadTemplate('achievements');
        document.getElementById('app').innerHTML = html;
        await this.loadAchievementsData();
    }

    // 模板加载方法
    async loadTemplate(templateName) {
        try {
            const response = await fetch(`/templates/${templateName}.html`);
            if (!response.ok) throw new Error('Template not found');
            return await response.text();
        } catch (error) {
            return this.getFallbackTemplate(templateName);
        }
    }

    getFallbackTemplate(templateName) {
        // 返回内联模板作为备用
        const templates = {
            'home': `<!-- 首页模板 -->`,
            'login': `<!-- 登录页模板 -->`,
            // ... 其他模板
        };
        return templates[templateName] || '<div>页面未找到</div>';
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

                const result = await authManager.login(credentials);
                if (result.success) {
                    this.navigate('/dashboard');
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
        const response = await fetch('/api/user/dashboard', {
            headers: authManager.getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            this.updateDashboardUI(data);
        }
    } catch (error) {
        Utils.showAlert('加载仪表板数据失败', 'error');
    }
};

Router.prototype.loadBehaviorData = async function() {
    // 运动记录页面数据加载
};

Router.prototype.loadAchievementsData = async function() {
    // 成就页面数据加载
};

// 初始化路由
window.router = new Router();