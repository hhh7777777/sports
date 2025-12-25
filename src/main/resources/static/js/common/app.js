class FitnessApp {
    constructor() {
        this.isInitialized = false;
        this.currentPage = '';
        this.components = new Map();
    }

    async init() {
        if (this.isInitialized) return;

        try {
            // 初始化认证状态
            if (typeof authManager !== 'undefined' && authManager.checkAuth) {
                await authManager.checkAuth();
            }

            // 设置全局错误处理
            this.setupErrorHandling();

            // 初始化组件系统
            this.initComponents();

            this.isInitialized = true;
            if (typeof Utils !== 'undefined' && Utils.showAlert) {
                Utils.showAlert('系统初始化完成', 'success', 2000);
            }
        } catch (error) {
            console.error('应用初始化失败:', error);
            if (typeof Utils !== 'undefined' && Utils.showAlert) {
                Utils.showAlert('系统初始化失败: ' + error.message, 'error');
            }
        }
    }

    setupErrorHandling() {
        window.addEventListener('error', (e) => {
            console.error('全局错误:', e.error);
            if (typeof Utils !== 'undefined' && Utils.showAlert) {
                Utils.showAlert('发生未知错误: ' + e.error.message, 'error');
            }
        });

        window.addEventListener('unhandledrejection', (e) => {
            console.error('未处理的Promise拒绝:', e.reason);
            if (typeof Utils !== 'undefined' && Utils.showAlert) {
                Utils.showAlert('请求失败，请检查网络: ' + e.reason.message, 'error');
            }
        });
    }

    initComponents() {
        // 注册通用组件
        if (typeof NavbarComponent !== 'undefined') {
            this.registerComponent('navbar', NavbarComponent);
        }
        if (typeof SidebarComponent !== 'undefined') {
            this.registerComponent('sidebar', SidebarComponent);
        }
        if (typeof ModalComponent !== 'undefined') {
            this.registerComponent('modal', ModalComponent);
        }
    }

    registerComponent(name, ComponentClass) {
        if (name && ComponentClass) {
            this.components.set(name, ComponentClass);
        }
    }

    getComponent(name) {
        return this.components.get(name);
    }

    // 页面生命周期管理
    async showPage(pageName, data = {}) {
        if (this.currentPage === pageName) return;

        // 触发页面离开事件
        this.triggerPageEvent('beforeLeave', this.currentPage);

        this.currentPage = pageName;

        // 触发页面进入事件
        this.triggerPageEvent('beforeEnter', pageName, data);

        // 实际页面切换由路由处理
        if (typeof router !== 'undefined' && router.navigate) {
            await router.navigate(`/${pageName}`, data);
        }
    }

    triggerPageEvent(eventName, pageName, data = {}) {
        const event = new CustomEvent(`page${eventName}`, {
            detail: { pageName, data }
        });
        window.dispatchEvent(event);
    }
}

// 组件基类
class BaseComponent {
    constructor(element) {
        if (!element) return;
        this.element = element;
        this.init();
    }

    init() {
        // 由子类实现
    }

    on(event, handler) {
        if (this.element && event && handler) {
            this.element.addEventListener(event, handler);
        }
    }

    emit(event, detail) {
        if (this.element && event) {
            const customEvent = new CustomEvent(event, { detail });
            this.element.dispatchEvent(customEvent);
        }
    }
}

// 导航栏组件
class NavbarComponent extends BaseComponent {
    init() {
        this.render();
        this.attachEventListeners();
    }

    render() {
        if (!this.element) return;
        
        this.element.innerHTML = `
            <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
                <div class="container-fluid">
                    <a class="navbar-brand" href="/static" data-link>
                        <i class="fas fa-running me-2"></i>运动健康
                    </a>
                    <div class="navbar-nav ms-auto">
                        ${this.renderUserSection()}
                    </div>
                </div>
            </nav>
        `;
    }

    renderUserSection() {
        if (typeof authManager !== 'undefined' && authManager.currentUser) {
            return `
                <span class="navbar-text me-3">
                    欢迎，${authManager.currentUser.username}
                </span>
                <div class="dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="fas fa-user-circle"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="/profile" data-link>个人资料</a></li>
                        <li><a class="dropdown-item" href="/achievements" data-link>我的成就</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" href="#" id="logoutBtn">退出登录</a></li>
                    </ul>
                </div>
            `;
        } else {
            return `
                <a class="nav-link" href="/login" data-link>登录</a>
                <a class="nav-link" href="/register" data-link>注册</a>
            `;
        }
    }

    attachEventListeners() {
        if (!this.element) return;
        
        this.element.addEventListener('click', (e) => {
            if (e.target.closest('[data-link]')) {
                e.preventDefault();
                const href = e.target.closest('[data-link]').getAttribute('href');
                if (typeof router !== 'undefined' && router.navigate) {
                    router.navigate(href);
                }
            }

            if (e.target.id === 'logoutBtn' || e.target.closest('#logoutBtn')) {
                e.preventDefault();
                if (typeof authManager !== 'undefined' && authManager.logout) {
                    authManager.logout();
                }
            }
        });
    }
}

// 初始化应用
document.addEventListener('DOMContentLoaded', async () => {
    try {
        window.fitnessApp = new FitnessApp();
        await fitnessApp.init();

        // 渲染导航栏
        const navbarElement = document.createElement('div');
        document.body.insertBefore(navbarElement, document.getElementById('app'));
        new NavbarComponent(navbarElement);
    } catch (error) {
        console.error('应用初始化过程出错:', error);
        // 即使出错也要确保基本功能可用
        if (typeof Utils !== 'undefined' && Utils.showAlert) {
            Utils.showAlert('页面初始化遇到问题: ' + error.message, 'warning');
        }
    }
});