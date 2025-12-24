// 健康星球主应用程序逻辑
class HealthPlanetApp {
    constructor() {
        this.currentTheme = 'christmas';
        this.userData = {};
        this.badges = [];
        this.carousel = null;
        this.init();
    }

    init() {
        this.loadUserData();
        this.loadBadgesFromServer();
        this.initTheme();
        this.initEventListeners();
        this.initSnowEffect();
        this.updateStats();
        this.initCarousel();
        
        // 检查并显示新年活动横幅
        checkNewYearBanner();
        
        // 标签页切换功能
        const tabs = document.querySelectorAll('.hero-tab');
        tabs.forEach(tab => {
            tab.addEventListener('click', function() {
                // 移除所有激活状态
                tabs.forEach(t => t.classList.remove('active'));
                document.querySelectorAll('.hero-tab-content').forEach(content => {
                    content.classList.remove('active');
                });
                
                // 激活当前标签
                this.classList.add('active');
                const tabId = this.getAttribute('data-tab');
                document.getElementById(`${tabId}-tab`).classList.add('active');
            });
        });
    }

    loadUserData() {
        const savedData = localStorage.getItem('healthPlanetData');
        this.userData = savedData ? JSON.parse(savedData) : {
            collectedBadges: 2,
            totalBadges: 15,
            streakDays: 7,
            totalDuration: 1260,
            theme: 'christmas',
            lastLogin: new Date().toISOString(),
            level: 3,
            points: 450
        };
        this.saveUserData();
    }

    saveUserData() {
        localStorage.setItem('healthPlanetData', JSON.stringify(this.userData));
    }

    // 从服务器加载徽章数据
    async loadBadgesFromServer() {
        try {
            const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
            if (!token) {
                // 如果没有token，使用默认数据
                this.loadDefaultBadges();
                return;
            }

            const response = await fetch('/api/user/badges', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                if (result.code === 200) {
                    // 转换服务器数据为本地格式
                    this.badges = result.data.map((badge, index) => ({
                        id: badge.badgeId,
                        name: badge.badgeName,
                        description: badge.description,
                        icon: this.getBadgeIconClass(badge.iconUrl),
                        color: this.getBadgeColor(badge.level),
                        level: badge.level || 1,
                        position: (index % 5) + 1,
                        earned: badge.achieved || false,
                        earnedDate: badge.achieveTime || null,
                        rarity: this.getRarityByLevel(badge.level)
                    }));
                    this.displayBadges();
                } else {
                    // 如果获取失败，使用默认数据
                    this.loadDefaultBadges();
                }
            } else {
                // 如果请求失败，使用默认数据
                this.loadDefaultBadges();
            }
        } catch (error) {
            console.error('加载徽章数据失败:', error);
            // 出错时使用默认徽章数据
            this.loadDefaultBadges();
        }
    }

    // 默认徽章数据（用于服务器不可用时）
    loadDefaultBadges() {
        this.badges = [
            {
                id: 1,
                name: "运动新人",
                description: "完成首次运动记录",
                icon: "fas fa-star",
                color: "#FFD700",
                level: 1,
                position: 1,
                earned: true,
                earnedDate: "2024-12-20",
                rarity: "common"
            },
            {
                id: 2,
                name: "坚持不懈",
                description: "连续运动7天",
                icon: "fas fa-fire",
                color: "#FF4500",
                level: 3,
                position: 2,
                earned: true,
                earnedDate: "2024-12-22",
                rarity: "rare"
            },
            {
                id: 3,
                name: "马拉松选手",
                description: "累计跑步100公里",
                icon: "fas fa-running",
                color: "#1E90FF",
                level: 4,
                position: 1,
                earned: false,
                rarity: "epic"
            },
            {
                id: 4,
                name: "瑜伽大师",
                description: "完成100小时瑜伽",
                icon: "fas fa-spa",
                color: "#32CD32",
                level: 2,
                position: 3,
                earned: false,
                rarity: "rare"
            },
            {
                id: 5,
                name: "游泳健将",
                description: "游泳50公里",
                icon: "fas fa-swimmer",
                color: "#00BFFF",
                level: 3,
                position: 4,
                earned: false,
                rarity: "epic"
            },
            {
                id: 6,
                name: "圣诞老人",
                description: "圣诞期间完成特别挑战",
                icon: "fas fa-sleigh",
                color: "#FF0000",
                level: 5,
                position: 3,
                earned: false,
                rarity: "legendary"
            },
            {
                id: 7,
                name: "早起鸟儿",
                description: "连续7天早晨运动",
                icon: "fas fa-sun",
                color: "#FFA500",
                level: 2,
                position: 2,
                earned: true,
                earnedDate: "2024-12-18",
                rarity: "rare"
            },
            {
                id: 8,
                name: "夜猫子",
                description: "连续7天晚上运动",
                icon: "fas fa-moon",
                color: "#4B0082",
                level: 2,
                position: 1,
                earned: false,
                rarity: "rare"
            },
            {
                id: 9,
                name: "力量训练者",
                description: "完成50次力量训练",
                icon: "fas fa-dumbbell",
                color: "#8B4513",
                level: 3,
                position: 5,
                earned: false,
                rarity: "common"
            },
            {
                id: 10,
                name: "有氧达人",
                description: "完成100小时有氧运动",
                icon: "fas fa-heart",
                color: "#DC143C",
                level: 4,
                position: 2,
                earned: false,
                rarity: "epic"
            }
        ];
        this.displayBadges();
    }

    // 根据图标URL获取图标类名
    getBadgeIconClass(iconUrl) {
        // 根据图标URL映射到Font Awesome图标
        const iconMap = {
            "/images/icons/new.png": "fas fa-star",
            "/images/icons/consist.png": "fas fa-fire",
            "/images/icons/marathon.png": "fas fa-running",
            "/images/icons/professor.png": "fas fa-graduation-cap",
            "/images/icons/anniversary.png": "fas fa-birthday-cake",
            "/images/icons/birthday.png": "fas fa-birthday-cake",
            "/images/icons/holiday.png": "fas fa-gift",
            "/images/icons/share.png": "fas fa-share-alt",
            "/images/icons/team-leader.png": "fas fa-users",
            "/images/icons/social.png": "fas fa-comments",
            "/images/icons/platinum.png": "fas fa-award",
            "/images/icons/silver.png": "fas fa-medal",
            "/images/icons/diamond.png": "fas fa-gem",
            "/images/icons/bronze.png": "fas fa-shield-alt",
            "/images/icons/gold.png": "fas fa-trophy",
            "/images/icons/weekend-warrior.png": "fas fa-calendar-week",
            "/images/icons/night-owl.png": "fas fa-moon",
            "/images/icons/all-rounder.png": "fas fa-star-of-life",
            "/images/icons/endurance.png": "fas fa-battery-full",
            "/images/icons/1000min.png": "fas fa-clock",
            "/images/icons/100km.png": "fas fa-route",
            "/images/icons/iron-will.png": "fas fa-fist-raised",
            "/images/icons/early-bird.png": "fas fa-sun",
            "/images/icons/export.png": "fas fa-user-graduate",
            "default": "fas fa-award"
        };
        
        return iconMap[iconUrl] || iconMap["default"];
    }

    // 根据等级获取颜色
    getBadgeColor(level) {
        const colors = {
            1: "#FFD700", // 金色
            2: "#C0C0C0", // 银色
            3: "#CD7F32", // 青铜色
            4: "#B9F5FF", // 钻石色
            5: "#FF0000"  // 红色
        };
        return colors[level] || "#808080"; // 默认灰色
    }

    // 根据等级获取稀有度
    getRarityByLevel(level) {
        const rarityMap = {
            1: "common",
            2: "uncommon",
            3: "rare",
            4: "epic",
            5: "legendary"
        };
        return rarityMap[level] || "common";
    }

    initTheme() {
        const savedTheme = localStorage.getItem('healthPlanetTheme') || 'christmas';
        this.switchTheme(savedTheme);
    }

    switchTheme(theme) {
        this.currentTheme = theme;
        document.body.className = theme + '-theme';
        localStorage.setItem('healthPlanetTheme', theme);

        this.updateThemeIndicator();

        if (theme === 'christmas') {
            this.initSnowEffect();
            const banner = document.getElementById('christmasEventBanner');
            if (banner) {
                banner.classList.remove('d-none');
            }
        } else {
            // 非圣诞主题不显示雪花效果
            const snowflakes = document.getElementById('snowflakes');
            if (snowflakes) {
                snowflakes.innerHTML = '';
            }
            const banner = document.getElementById('christmasEventBanner');
            if (banner) {
                banner.classList.add('d-none');
            }
        }
        
        // 重新渲染徽章库以适应主题变化
        if (this.badges && this.badges.length > 0) {
            this.displayBadges();
        }
    }

    updateThemeIndicator() {
        const badge = document.getElementById('themeBadge');
        const toggleBtn = document.getElementById('themeToggle');

        if (badge) {
            badge.textContent = this.currentTheme === 'newyear' ? '新年' : '普通';
        }

        if (toggleBtn) {
            const icon = toggleBtn.querySelector('i');
            if (icon) {
                icon.className = this.currentTheme === 'newyear' ? 'fas fa-fire' : 'fas fa-sun';
            }
        }
    }

    checkNewYearBanner() {
        const today = new Date();
        const month = today.getMonth() + 1; // 月份从0开始，所以需要+1
        const day = today.getDate();
        
        // 在1月1日到1月15日期间显示新年活动横幅
        if (month === 1 && day >= 1 && day <= 15) {
            const banner = document.getElementById('newyearEventBanner');
            if (banner) {
                banner.classList.remove('d-none');
            }
        }
    }

    initEventListeners() {
        // 主题切换按钮
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                this.toggleTheme();
            });
        }

        // 新年活动横幅
        const newyearEventBanner = document.getElementById('newyearEventBanner');
        if (newyearEventBanner) {
            newyearEventBanner.addEventListener('click', () => {
                this.enterNewYearEvent();
            });
        }

        // 键盘导航
        document.addEventListener('keydown', (e) => {
            switch(e.key) {
                case '1':
                    this.switchTheme('christmas');
                    break;
                case '2':
                    this.switchTheme('normal');
                    break;
                case 'Escape':
                    this.closeAllModals();
                    break;
            }
        });
    }

    toggleTheme() {
        const newTheme = this.currentTheme === 'newyear' ? 'normal' : 'newyear';
        this.switchTheme(newTheme);
        this.showNotification(`已切换到${newTheme === 'newyear' ? '新年' : '普通'}主题`);
    }

    initSnowEffect() {
        // 只在新年主题下显示雪花效果
        if (this.currentTheme !== 'newyear') return;

        const container = document.getElementById('snowflakes');
        if (!container) return;
        
        // 清空现有雪花
        container.innerHTML = '';

        const createSnowflake = () => {
            const snowflake = document.createElement('div');
            snowflake.className = 'snowflake';
            snowflake.innerHTML = '❄';

            const size = Math.random() * 20 + 10;
            const startX = Math.random() * 100;
            const duration = Math.random() * 5 + 5;
            const delay = Math.random() * 5;
            const opacity = Math.random() * 0.5 + 0.3;

            snowflake.style.left = `${startX}vw`;
            snowflake.style.fontSize = `${size}px`;
            snowflake.style.opacity = opacity;
            snowflake.style.animationDuration = `${duration}s`;
            snowflake.style.animationDelay = `${delay}s`;
            snowflake.style.pointerEvents = 'none';
            snowflake.style.position = 'absolute';
            snowflake.style.top = '-50px';
            snowflake.style.zIndex = '9998';
            snowflake.style.userSelect = 'none';

            container.appendChild(snowflake);

            // 添加动画关键帧
            const style = document.createElement('style');
            style.textContent = `
                @keyframes fall {
                    to {
                        transform: translateY(100vh) rotate(360deg);
                    }
                }
            `;
            document.head.appendChild(style);
            
            snowflake.style.animation = `fall ${duration}s linear forwards`;

            setTimeout(() => {
                if (snowflake.parentNode) {
                    snowflake.parentNode.removeChild(snowflake);
                }
            }, duration * 1000);
        };

        for (let i = 0; i < 30; i++) {
            setTimeout(createSnowflake, i * 100);
        }

        setInterval(createSnowflake, 500);
    }

    initCarousel() {
        const carouselElement = document.getElementById('heroCarousel');
        if (carouselElement) {
            // 初始化Bootstrap轮播
            this.carousel = new bootstrap.Carousel(carouselElement, {
                interval: 5000,
                pause: 'hover',
                wrap: true
            });

            // 添加键盘控制
            document.addEventListener('keydown', (e) => {
                if (this.carousel) {
                    if (e.key === 'ArrowLeft') {
                        this.carousel.prev();
                    } else if (e.key === 'ArrowRight') {
                        this.carousel.next();
                    }
                }
            });
        }
    }

    displayBadges() {
        const container = document.getElementById('badgeCollection');
        if (!container) return;

        const earnedBadges = this.badges.filter(badge => badge.earned);
        this.userData.collectedBadges = earnedBadges.length;

        // 根据当前主题显示不同样式的徽章库
        if (this.currentTheme === 'newyear') {
            // 新年主题：新年树挂徽章形式
            this.displayBadgesNewYearTheme(container);
        } else {
            // 正常主题：网格布局形式
            this.displayBadgesNormalTheme(container);
        }
    }

    displayBadgesNewYearTheme(container) {
        // 创建新年树挂徽章的布局
        let newyearHTML = `
            <div class="christmas-tree-container">
                <div class="tree">
                    <div class="tree-layer layer-5"></div>
                    <div class="tree-layer layer-4"></div>
                    <div class="tree-layer layer-3"></div>
                    <div class="tree-layer layer-2"></div>
                    <div class="tree-layer layer-1"></div>
                    <div class="tree-trunk"></div>
                    <div class="tree-star">
                        <i class="fas fa-star"></i>
                    </div>
        `;

        // 为每个徽章创建悬挂的装饰
        this.badges.forEach((badge, index) => {
            // 计算徽章在树上的位置
            const level = badge.level || 1;
            const position = badge.position || 1;
            
            const badgeIcon = badge.iconUrl ? 
                `<img src="${badge.iconUrl}" alt="${badge.name}" class="badge-img">` :
                `<i class="${badge.icon} fa-2x"></i>`;
            
            newyearHTML += `
                <div class="badge-hanger" data-level="${level}" data-position="${position}">
                    <div class="badge-card ${badge.earned ? 'owned' : 'locked'}" onclick="app.showBadgeDetails(${badge.id})">
                        <div class="badge-icon" style="background: ${badge.color};">
                            ${badgeIcon}
                        </div>
                        <div class="badge-name">${badge.name}</div>
                    </div>
                </div>
            `;
        });

        newyearHTML += `
                </div>
            </div>
        `;

        container.innerHTML = newyearHTML;
    }

    displayBadgesNormalTheme(container) {
        // 正常网格布局
        container.innerHTML = this.badges.map(badge => `
            <div class="col-6 col-md-4 col-lg-3 mb-4">
                <div class="badge-card ${badge.earned ? 'owned' : 'locked'} badge-hover-effect" 
                     onclick="app.showBadgeDetails(${badge.id})">
                    <div class="badge-icon mx-auto mb-3" style="background: ${badge.color};">
                        ${badge.iconUrl ? 
                          `<img src="${badge.iconUrl}" alt="${badge.name}" class="badge-img">` :
                          `<i class="${badge.icon} fa-2x"></i>`
                        }
                    </div>
                    <h6 class="mb-2">${badge.name}</h6>
                    <p class="small text-muted mb-2">${badge.description}</p>
                    <div class="badge-status">
                        ${badge.earned ?
            `<span class="badge bg-success"><i class="fas fa-check me-1"></i>已获得</span>
                             ${badge.earnedDate ? `<small class="d-block text-muted mt-1">${badge.earnedDate}</small>` : ''}` :
            `<span class="badge bg-secondary"><i class="fas fa-lock me-1"></i>未获得</span>`
        }
                    </div>
                    <div class="rarity-badge mt-2">
                        <span class="badge bg-${this.getRarityClass(badge.rarity)}">
                            ${this.getRarityText(badge.rarity)}
                        </span>
                    </div>
                </div>
            </div>
        `).join('');
    }

    getRarityClass(rarity) {
        const rarityMap = {
            'common': 'secondary',
            'uncommon': 'primary',
            'rare': 'info',
            'epic': 'warning',
            'legendary': 'danger'
        };
        return rarityMap[rarity] || 'secondary';
    }

    getRarityText(rarity) {
        const rarityMap = {
            'common': '普通',
            'uncommon': '不凡',
            'rare': '稀有',
            'epic': '史诗',
            'legendary': '传说'
        };
        return rarityMap[rarity] || '普通';
    }

    updateStats() {
        if (document.getElementById('collectedBadges')) {
            document.getElementById('collectedBadges').textContent =
                `${this.userData.collectedBadges}/${this.userData.totalBadges}`;
            document.getElementById('badgeProgress').style.width =
                `${(this.userData.collectedBadges / this.userData.totalBadges) * 100}%`;
        }
        
        if (document.getElementById('streakDays')) {
            document.getElementById('streakDays').textContent = `${this.userData.streakDays}天`;
        }
        
        if (document.getElementById('totalDuration')) {
            document.getElementById('totalDuration').textContent = `${this.userData.totalDuration}分钟`;
        }
    }

    showBadgeDetails(badgeId) {
        const badge = this.badges.find(b => b.id === badgeId);
        if (!badge) return;

        const modalHtml = `
            <div class="modal fade" id="badgeDetailModal" tabindex="-1">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content newyear-modal">
                        <div class="modal-header border-0 pb-0">
                            <h5 class="modal-title">徽章详情</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body text-center">
                            <div class="badge-icon-lg mx-auto mb-3" style="background: ${badge.color};">
                                ${badge.iconUrl ? 
                                  `<img src="${badge.iconUrl}" alt="${badge.name}" style="width: 100%; height: 100%; object-fit: contain;">` :
                                  `<i class="${badge.icon} fa-3x"></i>`
                                }
                            </div>
                            <h4 class="text-warning">${badge.name}</h4>
                            <p class="text-muted">${badge.description}</p>
                            <div class="badge-info">
                                <p><strong>稀有度:</strong> <span class="badge bg-${this.getRarityClass(badge.rarity)}">${this.getRarityText(badge.rarity)}</span></p>
                                ${badge.earned ?
            `<p><strong>获得时间:</strong> ${badge.earnedDate || '未知'}</p>` :
            '<p class="text-info">完成挑战即可获得此徽章</p>'
        }
                            </div>
                        </div>
                        <div class="modal-footer border-0">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                            ${!badge.earned ?
            `<button type="button" class="btn btn-warning" onclick="app.showChallengeForBadge(${badge.id})">开始挑战</button>` :
            '<button type="button" class="btn btn-success" onclick="app.shareBadge(${badge.id})">分享</button>'
        }
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.showModal(modalHtml, 'badgeDetailModal');
    }

    // 添加缺失的方法
    showChallengeForBadge(badgeId) {
        // 这里可以添加开始挑战的逻辑
        this.showNotification(`开始挑战徽章 ID: ${badgeId}`, 'info');
        // 关闭模态框
        const modal = bootstrap.Modal.getInstance(document.getElementById('badgeDetailModal'));
        if (modal) modal.hide();
    }

    enterNewYearEvent() {
        window.location.href = '/newyear-event';
    }

    startNewYearChallenge() {
        this.showNotification('🎉 新年挑战开始！连续7天运动赢取特别徽章', 'success');
        // 这里可以添加更复杂的挑战开始逻辑
    }

    shareTree() {
        if (navigator.share) {
            navigator.share({
                title: '我的健康新年树',
                text: `来看看我的运动成就！已收集 ${this.userData.collectedBadges} 枚徽章，连续运动 ${this.userData.streakDays} 天！`,
                url: window.location.href
            });
        } else {
            navigator.clipboard.writeText(window.location.href).then(() => {
                this.showNotification('链接已复制到剪贴板，快去分享给朋友吧！', 'success');
            });
        }
    }

    startNewRecord() {
        this.showNotification('开始记录新的运动...', 'info');
        // 这里可以跳转到记录页面
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} position-fixed`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 1060; min-width: 300px;';
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${this.getNotificationIcon(type)} me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 3000);
    }

    getNotificationIcon(type) {
        const icons = {
            'success': 'check-circle',
            'danger': 'exclamation-circle',
            'warning': 'exclamation-triangle',
            'info': 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    showModal(html, modalId) {
        // 移除现有模态框
        const existingModal = document.getElementById(modalId);
        if (existingModal) {
            existingModal.remove();
        }

        // 添加新模态框
        document.body.insertAdjacentHTML('beforeend', html);

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById(modalId));
        modal.show();

        // 模态框关闭时清理
        document.getElementById(modalId).addEventListener('hidden.bs.modal', function() {
            this.remove();
        });
    }

    closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal => {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        });
    }
}

// 全局函数
function joinNewYearChallenge() {
    if (window.app) {
        app.startNewYearChallenge();
    }
}

function enterNewYearEvent() {
    if (window.app) {
        app.enterNewYearEvent();
    }
}

function shareTree() {
    if (window.app) {
        app.shareTree();
    }
}

function startNewRecord() {
    if (window.app) {
        app.startNewRecord();
    }
}

// 页面加载完成后初始化应用
document.addEventListener('DOMContentLoaded', function() {
    // 初始化主页应用
    if (typeof HealthPlanetApp !== 'undefined') {
        window.app = new HealthPlanetApp();
    }
    
    // 检查并显示新年活动横幅
    checkNewYearBanner();
    
    // 标签页切换功能
    const tabs = document.querySelectorAll('.hero-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // 移除所有激活状态
            tabs.forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.hero-tab-content').forEach(content => {
                content.classList.remove('active');
            });
            
            // 激活当前标签
            this.classList.add('active');
            const tabId = this.getAttribute('data-tab');
            document.getElementById(`${tabId}-tab`).classList.add('active');
            
            // 如果点击的是新年活动标签，则加载新年活动数据
            if (tabId === 'newyear') {
                loadNewYearEventDataInTab();
            }
        });
    });
    
    // 检查用户登录状态
    checkUserAuth();
    
    // 绑定返回顶部按钮事件
    const backToTopButton = document.getElementById('backToTop');
    if (backToTopButton) {
        // 滚动时检查是否显示返回顶部按钮
        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                backToTopButton.classList.add('visible');
            } else {
                backToTopButton.classList.remove('visible');
            }
        });
        
        // 点击返回顶部
        backToTopButton.addEventListener('click', function() {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }
});

function checkNewYearBanner() {
    const today = new Date();
    const month = today.getMonth() + 1; // 月份从0开始，所以需要+1
    const day = today.getDate();
    
    // 在1月1日到1月15日期间显示新年活动横幅
    if (month === 1 && day >= 1 && day <= 15) {
        const banner = document.getElementById('newyearEventBanner');
        if (banner) {
            banner.classList.remove('d-none');
        }
    }
}

// 在新年标签页中加载数据
async function loadNewYearEventDataInTab() {
    // 检查是否已经加载过数据
    const container = document.getElementById('newyear-tab');
    if (container && container.getAttribute('data-loaded') === 'true') {
        return;
    }
    
    try {
        // 获取认证令牌
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            // 如果未登录，显示提示信息
            document.querySelector('#newyear-tab .bg-dark').innerHTML = `
                <h3><i class="fas fa-fire me-2"></i>新年特别活动</h3>
                <p class="mb-3">登录后参与新年活动，赢取限量版徽章和丰厚奖励</p>
                <a href="/login" class="btn btn-warning">立即登录</a>
            `;
            return;
        }
        
        // 设置加载状态
        if (container) {
            container.setAttribute('data-loaded', 'true');
        }
        
        // 加载活动数据
        const response = await fetch('/api/newyear/activities', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
                // 更新标签内容
                document.querySelector('#newyear-tab .bg-dark').innerHTML = `
                    <h3><i class="fas fa-fire me-2"></i>新年特别活动</h3>
                    <p class="mb-3">参与新年活动，赢取限量版徽章和丰厚奖励</p>
                    <a href="/newyear-event" class="btn btn-warning">立即参与</a>
                `;
            }
        }
    } catch (error) {
        console.error('加载新年活动数据失败:', error);
    }
}

// 加载新年活动详情
async function loadNewYearEventDetails() {
    try {
        // 显示加载指示器
        showLoadingIndicator();
        
        // 获取认证令牌
        const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken');
        if (!token) {
            // 如果未登录，跳转到登录页面
            window.location.href = '/login';
            return;
        }
        
        // 并行加载活动和徽章数据
        const [activitiesResponse, badgesResponse] = await Promise.all([
            fetch('/api/newyear/activities', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }),
            fetch('/api/newyear/badges', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
        ]);
        
        if (activitiesResponse.ok && badgesResponse.ok) {
            const activitiesResult = await activitiesResponse.json();
            const badgesResult = await badgesResponse.json();
            
            if (activitiesResult.code === 200 && badgesResult.code === 200) {
                // 显示活动详情模态框
                showNewYearEventModal(activitiesResult.data, badgesResult.data);
            } else {
                hideLoadingIndicator();
                alert('获取活动数据失败');
            }
        } else {
            hideLoadingIndicator();
            alert('网络错误，请稍后重试');
        }
    } catch (error) {
        hideLoadingIndicator();
        console.error('加载新年活动详情失败:', error);
        alert('加载失败，请检查网络连接');
    }
}

// 显示加载指示器
function showLoadingIndicator() {
    // 创建或显示加载指示器
    let loadingElement = document.getElementById('loadingIndicator');
    if (!loadingElement) {
        loadingElement = document.createElement('div');
        loadingElement.id = 'loadingIndicator';
        loadingElement.innerHTML = `
            <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 9999; display: flex; justify-content: center; align-items: center;">
                <div style="background: white; padding: 20px; border-radius: 10px; text-align: center;">
                    <div class="spinner-border text-primary" role="status"></div>
                    <div style="margin-top: 10px;">加载中...</div>
                </div>
            </div>
        `;
        document.body.appendChild(loadingElement);
    } else {
        loadingElement.style.display = 'flex';
    }
}

// 隐藏加载指示器
function hideLoadingIndicator() {
    const loadingElement = document.getElementById('loadingIndicator');
    if (loadingElement) {
        loadingElement.style.display = 'none';
    }
}

// 显示新年活动详情模态框
function showNewYearEventModal(activities, badges) {
    hideLoadingIndicator();
    
    // 构造模态框HTML
    const modalHtml = `
        <div class="modal fade" id="newyearEventModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title"><i class="fas fa-fire me-2"></i>新年特别活动</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="container-fluid">
                            <div class="row">
                                <div class="col-12">
                                    <h4>🎉 活动任务</h4>
                                    <div class="row" id="activitiesContainer">
                                        ${activities.map(activity => `
                                            <div class="col-md-6 col-lg-4 mb-3">
                                                <div class="card">
                                                    <div class="card-body">
                                                        <h5 class="card-title">
                                                            <i class="fas ${activity.icon} me-2" style="color: ${activity.color};"></i>
                                                            ${activity.name}
                                                        </h5>
                                                        <p class="card-text">${activity.description}</p>
                                                        ${activity.currentProgress !== undefined && activity.requiredProgress !== undefined ? `
                                                        <div class="progress">
                                                            <div class="progress-bar" role="progressbar" 
                                                                 style="width: ${(activity.currentProgress / activity.requiredProgress) * 100}%;">
                                                                ${activity.currentProgress}/${activity.requiredProgress}
                                                            </div>
                                                        </div>
                                                        ` : ''}
                                                    </div>
                                                </div>
                                            </div>
                                        `).join('')}
                                    </div>
                                </div>
                                
                                <div class="col-12 mt-4">
                                    <h4>🏅 新年徽章</h4>
                                    <div class="row" id="badgesContainer">
                                        ${badges.map(badge => `
                                            <div class="col-md-6 col-lg-3 mb-3">
                                                <div class="card text-center">
                                                    <div class="card-body">
                                                        <i class="fas ${badge.icon} fa-3x mb-2" style="color: ${badge.color};"></i>
                                                        <h6 class="card-title">${badge.name}</h6>
                                                        <p class="card-text small text-muted">${badge.description}</p>
                                                        ${badge.achieved ? 
                                                            '<span class="badge bg-success">已获得</span>' : 
                                                            '<span class="badge bg-secondary">未获得</span>'}
                                                    </div>
                                                </div>
                                            </div>
                                        `).join('')}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // 添加到页面
    let modalElement = document.getElementById('newyearEventModal');
    if (modalElement) {
        modalElement.remove();
    }
    
    const modalContainer = document.createElement('div');
    modalContainer.innerHTML = modalHtml;
    document.body.appendChild(modalContainer);
    
    // 显示模态框
    const modal = new bootstrap.Modal(document.getElementById('newyearEventModal'));
    modal.show();
    
    // 模态框关闭时清理
    document.getElementById('newyearEventModal').addEventListener('hidden.bs.modal', function () {
        this.remove();
    });
}

// 检查并显示新年活动横幅
function checkNewYearBanner() {
    // 总是显示新年活动横幅（根据实际需求可以添加日期判断）
    const banner = document.getElementById('newyearEventBanner');
    if (banner) {
        banner.classList.remove('d-none');
    }
}
