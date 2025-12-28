class ThemeManager {
    constructor() {
        this.themes = {
            'normal': { className: 'normal-theme' },
            'newyear': { className: 'newyear-theme' }
        };
        this.currentTheme = localStorage.getItem('healthPlanetTheme') || 'normal';
        this.init();
    }

    // 初始化主题
    init() {
        this.applyTheme(this.currentTheme);
    }

    // 应用主题
    applyTheme(theme) {
        if (!this.themes[theme]) return;

        this.currentTheme = theme;

        // 移除所有主题类
        document.body.classList.remove('normal-theme', 'newyear-theme');

        // 添加当前主题类
        document.body.classList.add(this.themes[theme].className);

        // 更新本地存储
        localStorage.setItem('healthPlanetTheme', theme);

        // 触发主题切换事件
        this.onThemeChanged(theme);
    }

    // 主题切换回调
    onThemeChanged(theme) {
        // 触发自定义事件，供其他模块监听
        const event = new CustomEvent('themeChanged', { detail: { theme } });
        document.dispatchEvent(event);
    }

    // 获取当前主题
    getCurrentTheme() {
        return this.currentTheme;
    }

    // 切换到新年主题
    enableNewYearTheme() {
        this.applyTheme('newyear');
    }

    // 切换到默认主题
    enableNormalTheme() {
        this.applyTheme('normal');
    }
}

// 初始化主题管理器
window.themeManager = new ThemeManager();