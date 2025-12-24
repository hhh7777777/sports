// 圣诞活动专用JavaScript类
class ChristmasActivity {
    constructor() {
        this.activities = [];
        this.userProgress = {};
        this.christmasBadges = [];
        this.dailyGifts = [];
        this.countdownDate = new Date('2025-12-25T23:59:59');
        this.currentDate = new Date();
        this.init();
    }

    init() {
        this.loadChristmasData();
        this.initCountdown();
        this.initSnowEffect();
        this.initEventListeners();
        this.displayActivities();
        this.displayChristmasBadges();
        this.checkDailyGift();
        this.updateProgressBars();
        this.initGiftAnimation();
    }

    loadChristmasData() {
        // 加载圣诞活动数据
        this.activities = [
            {
                id: 'santa_challenge_2024',
                name: '🎅 圣诞老人挑战',
                description: '连续7天每天运动30分钟以上，赢取限量版圣诞老人徽章',
                type: 'premium',
                category: 'endurance',
                duration: 7,
                currentProgress: 4,
                requiredProgress: 7,
                icon: 'fa-sleigh',
                color: '#dc3545',
                difficulty: 'medium',
                rewards: [
                    { name: '圣诞老人徽章', type: 'badge', rarity: 'legendary' },
                    { name: '100积分', type: 'points', value: 100 },
                    { name: '金色边框', type: 'cosmetic' }
                ],
                requirements: ['每天运动30分钟', '连续7天不间断', '任意运动类型'],
                startDate: '2024-12-01',
                endDate: '2024-12-25',
                active: true
            },
            {
                id: 'daily_gift_2024',
                name: '🎁 每日礼物任务',
                description: '每天完成任意运动即可开启当日圣诞惊喜礼物',
                type: 'daily',
                category: 'engagement',
                duration: 1,
                currentProgress: 1,
                requiredProgress: 1,
                icon: 'fa-gift',
                color: '#ffc107',
                difficulty: 'easy',
                rewards: [
                    { name: '随机积分', type: 'points', min: 5, max: 25 },
                    { name: '圣诞装饰', type: 'cosmetic' },
                    { name: '小礼物', type: 'item' }
                ],
                active: true,
                repeatable: true
            },
            {
                id: 'tree_decorator_2024',
                name: '🎄 圣诞树装饰家',
                description: '收集5枚不同的圣诞主题徽章，点亮你的专属圣诞树',
                type: 'collection',
                category: 'achievement',
                duration: 25,
                currentProgress: 2,
                requiredProgress: 5,
                icon: 'fa-tree',
                color: '#198754',
                difficulty: 'hard',
                rewards: [
                    { name: '圣诞树大师徽章', type: 'badge', rarity: 'epic' },
                    { name: '200积分', type: 'points', value: 200 },
                    { name: '特殊动画效果', type: 'cosmetic' }
                ],
                active: true
            },
            {
                id: 'snow_warrior_2024',
                name: '❄️ 雪地战士',
                description: '在雪天完成户外运动5次，证明你的勇气',
                type: 'weather',
                category: 'special',
                duration: 25,
                currentProgress: 1,
                requiredProgress: 5,
                icon: 'fa-snowflake',
                color: '#0dcaf0',
                difficulty: 'medium',
                rewards: [
                    { name: '雪地战士徽章', type: 'badge', rarity: 'rare' },
                    { name: '150积分', type: 'points', value: 150 }
                ],
                active: false, // 等待下雪天
                weatherDependent: true
            },
            {
                id: 'gift_exchange_2024',
                name: '🤝 礼物交换',
                description: '与朋友互相赠送5次健康祝福，传播圣诞快乐',
                type: 'social',
                category: 'community',
                duration: 25,
                currentProgress: 0,
                requiredProgress: 5,
                icon: 'fa-hands-helping',
                color: '#e83e8c',
                difficulty: 'easy',
                rewards: [
                    { name: '爱心使者徽章', type: 'badge', rarity: 'rare' },
                    { name: '友情积分', type: 'points', value: 50 }
                ],
                active: true,
                social: true
            }
        ];

        // 加载圣诞徽章数据
        this.christmasBadges = [
            {
                id: 101,
                name: "圣诞老人",
                description: "完成圣诞老人挑战获得的荣誉徽章",
                fullDescription: "这枚徽章证明您在圣诞期间展现了非凡的毅力和坚持，连续7天不间断运动，是真正的健康守护者！",
                icon: "fas fa-sleigh",
                color: "#dc3545",
                rarity: "legendary",
                obtained: false,
                obtainMethod: "完成圣诞老人挑战",
                specialEffects: ["动态雪花环绕", "徽章发光效果", "特殊音效"],
                unlockDate: "",
                animation: "bounce"
            },
            {
                id: 102,
                name: "圣诞树装饰家",
                description: "收集5枚圣诞徽章的成就证明",
                fullDescription: "您成功收集了多种圣诞主题徽章，展现了全面的运动能力和多样化的健康生活方式！",
                icon: "fas fa-tree",
                color: "#198754",
                rarity: "epic",
                obtained: false,
                obtainMethod: "收集5枚圣诞徽章",
                specialEffects: ["圣诞树生长动画", "星光闪烁"],
                unlockDate: "",
                animation: "grow"
            },
            {
                id: 103,
                name: "雪地战士",
                description: "在雪天坚持运动的勇气徽章",
                fullDescription: "不畏严寒，在雪天依然坚持运动，展现了真正的战士精神和健康决心！",
                icon: "fas fa-snowflake",
                color: "#0dcaf0",
                rarity: "rare",
                obtained: false,
                obtainMethod: "雪天运动5次",
                specialEffects: ["雪花飘落", "冰晶效果"],
                unlockDate: "",
                animation: "spin"
            },
            {
                id: 104,
                name: "礼物收集者",
                description: "开启7个每日礼物的幸运徽章",
                fullDescription: "您坚持每天运动，开启了7个圣诞礼物，是真正的礼物收集大师！",
                icon: "fas fa-gifts",
                color: "#ffc107",
                rarity: "rare",
                obtained: true,
                obtainMethod: "开启7个每日礼物",
                specialEffects: ["礼物盒闪烁", "彩带效果"],
                unlockDate: "2024-12-20",
                animation: "pulse"
            },
            {
                id: 105,
                name: "圣诞之星",
                description: "完成所有圣诞挑战的终极荣誉",
                fullDescription: "您完成了所有圣诞挑战，是本次圣诞活动中最耀眼的明星！这份荣誉属于真正的健康冠军！",
                icon: "fas fa-star",
                color: "#ffd700",
                rarity: "legendary",
                obtained: false,
                obtainMethod: "完成所有圣诞挑战",
                specialEffects: ["星光闪耀", "金色光环", "特殊称号"],
                unlockDate: "",
                animation: "shine"
            }
        ];

        // 加载每日礼物数据
        this.loadDailyGiftsData();

        // 加载用户进度
        this.loadUserProgress();
    }

    loadDailyGiftsData() {
        this.dailyGifts = [];
        const startDate = new Date('2024-12-01');
        const endDate = new Date('2024-12-25');

        for (let date = new Date(startDate); date <= endDate; date.setDate(date.getDate() + 1)) {
            const gift = {
                day: date.getDate(),
                date: date.toISOString().split('T')[0],
                claimed: date < this.currentDate,
                reward: this.generateDailyReward(date),
                special: date.getDate() % 7 === 0, // 每周日有特殊奖励
                animation: this.getGiftAnimation ? this.getGiftAnimation(date) : null // 添加检查
            };
            this.dailyGifts.push(gift);
        }
    }

    generateDailyReward(date) {
        const day = date.getDate();
        const rewards = [
            { type: 'points', value: Math.floor(day * 1.5) },
            { type: 'cosmetic', name: '圣诞装饰' },
            { type: 'badge', name: '每日之星' },
            { type: 'boost', name: '双倍积分' }
        ];

        if (day % 7 === 0) {
            return { type: 'special', name: '神秘大礼包', value: 100 };
        }

        return rewards[day % rewards.length];
    }

    loadUserProgress() {
        const savedProgress = localStorage.getItem('christmasProgress2024');
        this.userProgress = savedProgress ? JSON.parse(savedProgress) : {};

        // 初始化活动进度
        this.activities.forEach(activity => {
            if (!this.userProgress[activity.id]) {
                this.userProgress[activity.id] = {
                    progress: activity.currentProgress,
                    completed: false,
                    claimed: false,
                    startDate: new Date().toISOString(),
                    lastUpdate: new Date().toISOString(),
                    history: []
                };
            }
        });

        this.saveProgress();
    }

    initCountdown() {
        const updateCountdown = () => {
            const now = new Date();
            const diff = this.countdownDate - now;

            if (diff <= 0) {
                this.handleChristmasArrival();
                return;
            }

            const days = Math.floor(diff / (1000 * 60 * 60 * 24));
            const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((diff % (1000 * 60)) / 1000);

            this.updateCountdownDisplay(days, hours, minutes, seconds);
        };

        updateCountdown();
        this.countdownInterval = setInterval(updateCountdown, 1000);
    }

    updateCountdownDisplay(days, hours, minutes, seconds) {
        const elements = {
            days: document.getElementById('countdownDays'),
            hours: document.getElementById('countdownHours'),
            minutes: document.getElementById('countdownMinutes'),
            seconds: document.getElementById('countdownSeconds')
        };

        Object.keys(elements).forEach(key => {
            if (elements[key]) {
                elements[key].textContent = this.formatTime(eval(key));
                elements[key].style.animation = 'pulse 1s ease-in-out';

                // 添加节日特效
                if (days <= 3) {
                    elements[key].classList.add('text-danger', 'fw-bold');
                    elements[key].style.animation = 'heartbeat 1s infinite';
                }
            }
        });

        // 更新倒计时标题
        this.updateCountdownTitle(days);
    }

    updateCountdownTitle(days) {
        const titleElement = document.getElementById('countdownTitle');
        if (!titleElement) return;

        const messages = {
            0: '🎄 圣诞快乐！ 🎄',
            1: '🌟 明天就是圣诞节！ 🌟',
            2: '⏰ 只剩2天！准备收礼物啦！ ⏰',
            3: '🎁 圣诞倒计时3天！ 🎁',
            7: '📅 圣诞周开始啦！ 📅',
            14: '🗓️ 还有2周，坚持就是胜利！ 🗓️'
        };

        titleElement.textContent = messages[days] || `距离圣诞节还有 ${days} 天`;

        // 添加节日表情
        if (days <= 7) {
            titleElement.classList.add('text-warning', 'fw-bold');
        }
    }

    initSnowEffect() {
        const container = document.getElementById('snowContainer');
        if (!container) return;

        const createSnowflake = () => {
            const snowflake = document.createElement('div');
            snowflake.className = 'snowflake';
            snowflake.innerHTML = this.getRandomSnowflake();

            // 随机属性
            const size = Math.random() * 20 + 8;
            const startX = Math.random() * 100;
            const duration = Math.random() * 8 + 3;
            const delay = Math.random() * 5;
            const opacity = Math.random() * 0.6 + 0.2;
            const spin = Math.random() * 360;

            snowflake.style.cssText = `
                left: ${startX}vw;
                font-size: ${size}px;
                opacity: ${opacity};
                animation-duration: ${duration}s;
                animation-delay: ${delay}s;
                transform: rotate(${spin}deg);
                z-index: 9999;
                pointer-events: none;
            `;

            container.appendChild(snowflake);

            // 自动清理
            setTimeout(() => {
                if (snowflake.parentNode) {
                    snowflake.parentNode.removeChild(snowflake);
                }
            }, duration * 1000);
        };

        // 创建初始雪花
        for (let i = 0; i < 40; i++) {
            setTimeout(() => createSnowflake(), i * 100);
        }

        // 持续创建雪花
        this.snowInterval = setInterval(createSnowflake, 300);
    }

    getRandomSnowflake() {
        const snowflakes = ['❄', '❅', '❆', '★', '☆'];
        return snowflakes[Math.floor(Math.random() * snowflakes.length)];
    }

    initEventListeners() {
        // 活动卡片交互
        document.addEventListener('click', (e) => {
            const activityCard = e.target.closest('[data-activity-id]');
            if (activityCard) {
                const activityId = activityCard.dataset.activityId;
                this.showActivityDetails(activityId);
            }

            const badgeCard = e.target.closest('[data-badge-id]');
            if (badgeCard) {
                const badgeId = badgeCard.dataset.badgeId;
                this.showBadgeDetails(badgeId);
            }

            const giftElement = e.target.closest('[data-gift-day]');
            if (giftElement) {
                const giftDay = giftElement.dataset.giftDay;
                this.openDailyGift(giftDay);
            }
        });

        // 键盘快捷键
        document.addEventListener('keydown', (e) => {
            if (e.ctrlKey) {
                switch(e.key) {
                    case '1': this.showActivityOverview(); break;
                    case '2': this.showBadgeCollection(); break;
                    case '3': this.showGiftCalendar(); break;
                    case 'g': e.preventDefault(); this.claimAllAvailableGifts(); break;
                }
            }

            if (e.key === 'Escape') {
                this.closeAllModals();
            }
        });

        // 页面可见性变化
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden) {
                this.checkDailyGift();
                this.updateAllProgress();
            }
        });

        // 滚动动画
        this.initScrollAnimations();
    }

    displayActivities() {
        const container = document.getElementById('christmasActivities');
        if (!container) return;

        container.innerHTML = this.activities.map(activity => `
            <div class="col-lg-6 col-xl-4 mb-4">
                <div class="activity-card ${this.getActivityCardClass(activity)}" 
                     data-activity-id="${activity.id}">
                    ${activity.type === 'premium' ?
            '<div class="premium-ribbon">限量活动</div>' : ''}
                    
                    <div class="card-body position-relative">
                        <div class="activity-header mb-3">
                            <div class="d-flex align-items-center">
                                <div class="activity-icon me-3" style="color: ${activity.color};">
                                    <i class="fas ${activity.icon} fa-2x"></i>
                                </div>
                                <div class="flex-grow-1">
                                    <h5 class="card-title mb-1">${activity.name}</h5>
                                    <div class="activity-meta">
                                        <span class="badge bg-${this.getDifficultyColor(activity.difficulty)} me-2">
                                            ${this.getDifficultyText(activity.difficulty)}
                                        </span>
                                        <small class="text-muted">
                                            <i class="fas fa-clock me-1"></i>${activity.duration}天
                                        </small>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <p class="card-text text-muted small mb-3">${activity.description}</p>
                        
                        <!-- 进度条 -->
                        <div class="progress-section mb-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <small class="text-muted">进度</small>
                                <small class="text-warning fw-bold">
                                    ${activity.currentProgress}/${activity.requiredProgress}
                                </small>
                            </div>
                            <div class="progress" style="height: 8px;">
                                <div class="progress-bar" 
                                     style="width: ${this.calculateProgress(activity)}%;
                                            background: ${activity.color};
                                            animation: gradient 2s ease infinite;">
                                </div>
                            </div>
                        </div>
                        
                        <!-- 奖励预览 -->
                        <div class="rewards-preview mb-3">
                            <h6 class="small text-muted mb-2">奖励预览</h6>
                            <div class="d-flex flex-wrap gap-2">
                                ${activity.rewards.slice(0, 3).map(reward => `
                                    <span class="badge bg-warning text-dark small">
                                        ${reward.name}
                                    </span>
                                `).join('')}
                                ${activity.rewards.length > 3 ?
            '<span class="badge bg-secondary small">+' + (activity.rewards.length - 3) + '</span>' : ''}
                            </div>
                        </div>
                        
                        <!-- 操作按钮 -->
                        <div class="activity-actions">
                            <button class="btn btn-${this.getActivityButtonVariant(activity)} w-100"
                                    onclick="christmasActivity.startActivity('${activity.id}')"
                                    ${!activity.active ? 'disabled' : ''}>
                                <i class="fas ${this.getActivityButtonIcon(activity)} me-2"></i>
                                ${this.getActivityButtonText(activity)}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    displayChristmasBadges() {
        const container = document.getElementById('christmasBadges');
        if (!container) return;

        const earnedCount = this.christmasBadges.filter(badge => badge.obtained).length;
        const totalCount = this.christmasBadges.length;
        const progress = (earnedCount / totalCount) * 100;

        container.innerHTML = `
            <div class="row">
                <div class="col-12">
                    <div class="collection-header text-center mb-4 p-4 rounded" 
                         style="background: linear-gradient(135deg, rgba(220, 53, 69, 0.1), rgba(255, 193, 7, 0.1));">
                        <h3 class="text-warning mb-2">🎄 圣诞徽章收藏 🎄</h3>
                        <p class="text-muted mb-3">收集全部徽章，解锁终极奖励！</p>
                        
                        <div class="progress mb-3" style="height: 20px;">
                            <div class="progress-bar bg-success" 
                                 style="width: ${progress}%">
                                ${earnedCount}/${totalCount} 枚徽章
                            </div>
                        </div>
                        
                        <div class="row text-center">
                            <div class="col-4">
                                <div class="stat-item">
                                    <div class="stat-value text-warning">${earnedCount}</div>
                                    <div class="stat-label small">已获得</div>
                                </div>
                            </div>
                            <div class="col-4">
                                <div class="stat-item">
                                    <div class="stat-value text-info">${totalCount - earnedCount}</div>
                                    <div class="stat-label small">待获得</div>
                                </div>
                            </div>
                            <div class="col-4">
                                <div class="stat-item">
                                    <div class="stat-value text-success">${Math.round(progress)}%</div>
                                    <div class="stat-label small">完成度</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row g-3">
                ${this.christmasBadges.map(badge => `
                    <div class="col-6 col-md-4 col-lg-3">
                        <div class="badge-card ${badge.obtained ? 'obtained' : 'locked'} ${badge.animation}" 
                             data-badge-id="${badge.id}">
                            <div class="badge-icon-container">
                                <div class="badge-icon" style="background: ${badge.color};">
                                    <i class="${badge.icon}"></i>
                                </div>
                                ${badge.obtained ?
            '<div class="obtained-badge"><i class="fas fa-check"></i></div>' :
            '<div class="locked-badge"><i class="fas fa-lock"></i></div>'
        }
                            </div>
                            
                            <div class="badge-content">
                                <h6 class="badge-name">${badge.name}</h6>
                                <p class="badge-description small text-muted">${badge.description}</p>
                                
                                <div class="badge-meta">
                                    <span class="badge bg-${this.getRarityColor(badge.rarity)} me-1">
                                        ${this.getRarityText(badge.rarity)}
                                    </span>
                                    ${badge.obtained ?
            `<span class="badge bg-success">已获得</span>` :
            `<span class="badge bg-secondary">${badge.obtainMethod}</span>`
        }
                                </div>
                                
                                ${badge.obtained && badge.unlockDate ?
            `<small class="text-muted d-block mt-1">
                                        <i class="fas fa-calendar me-1"></i>${badge.unlockDate}
                                    </small>` : ''
        }
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    // 辅助方法
    calculateProgress(activity) {
        return (activity.currentProgress / activity.requiredProgress) * 100;
    }

    getActivityCardClass(activity) {
        let classes = [];
        if (!activity.active) classes.push('inactive');
        if (this.userProgress[activity.id]?.completed) classes.push('completed');
        if (activity.type === 'premium') classes.push('premium');
        return classes.join(' ');
    }

    getDifficultyColor(difficulty) {
        const colors = {
            'easy': 'success',
            'medium': 'warning',
            'hard': 'danger'
        };
        return colors[difficulty] || 'secondary';
    }

    getDifficultyText(difficulty) {
        const textMap = {
            'easy': '简单',
            'medium': '中等',
            'hard': '困难'
        };
        return textMap[difficulty] || '未知';
    }

    getActivityButtonVariant(activity) {
        if (this.userProgress[activity.id]?.completed) return 'success';
        if (!activity.active) return 'secondary';
        return 'warning';
    }

    getActivityButtonIcon(activity) {
        if (this.userProgress[activity.id]?.completed) return 'fa-check';
        if (!activity.active) return 'fa-lock';
        return 'fa-play';
    }

    getActivityButtonText(activity) {
        if (this.userProgress[activity.id]?.completed) return '已完成';
        if (!activity.active) return '即将开始';
        return '开始挑战';
    }

    getRarityColor(rarity) {
        const colors = {
            'common': 'secondary',
            'uncommon': 'primary',
            'rare': 'info',
            'epic': 'warning',
            'legendary': 'danger'
        };
        return colors[rarity] || 'secondary';
    }

    getRarityText(rarity) {
        const textMap = {
            'common': '普通',
            'uncommon': '不凡',
            'rare': '稀有',
            'epic': '史诗',
            'legendary': '传说'
        };
        return textMap[rarity] || '普通';
    }

    // 格式化时间显示
    formatTime(time) {
        return time.toString().padStart(2, '0');
    }

    // 保存进度
    saveProgress() {
        localStorage.setItem('christmasProgress2024', JSON.stringify(this.userProgress));
    }

    // 显示通知
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show`;
        notification.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 1060; min-width: 300px;';
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${this.getNotificationIcon(type)} me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
            </div>
        `;

        document.body.appendChild(notification);

        // 自动消失
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
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

    showActivityDetails(activityId) {
        const activity = this.activities.find(a => a.id === activityId);
        if (!activity) return;

        const userProgress = this.userProgress[activity.id];
        const progressPercent = this.calculateProgress(activity);

        const modalHtml = `
            <div class="modal fade" id="activityDetailModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content christmas-modal">
                        <div class="modal-header border-0">
                            <div class="d-flex align-items-center w-100">
                                <div class="activity-icon-lg me-3" style="color: ${activity.color};">
                                    <i class="fas ${activity.icon} fa-2x"></i>
                                </div>
                                <div>
                                    <h5 class="modal-title mb-1">${activity.name}</h5>
                                    <div class="activity-subtitle">
                                        <span class="badge bg-${this.getDifficultyColor(activity.difficulty)} me-2">
                                            ${this.getDifficultyText(activity.difficulty)}
                                        </span>
                                        <span class="text-muted small">
                                            <i class="fas fa-clock me-1"></i>${activity.duration}天
                                        </span>
                                    </div>
                                </div>
                                <button type="button" class="btn-close btn-close-white ms-auto" data-bs-dismiss="modal"></button>
                            </div>
                        </div>
                        
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-lg-7">
                                    <!-- 活动描述 -->
                                    <div class="activity-description mb-4">
                                        <h6 class="text-warning mb-2">📋 活动介绍</h6>
                                        <p class="mb-3">${activity.description}</p>
                                        
                                        <div class="requirements-section mb-4">
                                            <h6 class="text-warning mb-2">🎯 挑战要求</h6>
                                            <ul class="list-unstyled">
                                                ${activity.requirements ? activity.requirements.map(req => `
                                                    <li class="mb-2">
                                                        <i class="fas fa-check-circle text-success me-2"></i>
                                                        ${req}
                                                    </li>
                                                `).join('') : ''}
                                            </ul>
                                        </div>
                                    </div>
                                    
                                    <!-- 进度详情 -->
                                    <div class="progress-details mb-4">
                                        <h6 class="text-warning mb-2">📈 当前进度</h6>
                                        <div class="progress mb-2" style="height: 20px;">
                                            <div class="progress-bar" 
                                                 style="width: ${progressPercent}%;
                                                        background: linear-gradient(45deg, ${activity.color}, #ffd700);">
                                                ${activity.currentProgress}/${activity.requiredProgress}
                                            </div>
                                        </div>
                                        <div class="d-flex justify-content-between">
                                            <small class="text-muted">开始日期: ${userProgress?.startDate?.split('T')[0] || '未开始'}</small>
                                            <small class="text-muted">剩余天数: ${activity.duration - activity.currentProgress}</small>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="col-lg-5">
                                    <!-- 奖励详情 -->
                                    <div class="rewards-section mb-4">
                                        <h6 class="text-warning mb-3">🎁 活动奖励</h6>
                                        <div class="rewards-list">
                                            ${activity.rewards.map((reward, index) => `
                                                <div class="reward-item d-flex align-items-center mb-3 p-3 rounded" 
                                                     style="background: rgba(255,255,255,0.05);">
                                                    <div class="reward-icon me-3">
                                                        <i class="fas ${this.getRewardIcon(reward.type)} fa-2x text-${this.getRewardColor(reward.type)}"></i>
                                                    </div>
                                                    <div>
                                                        <h6 class="mb-1">${reward.name}</h6>
                                                        <small class="text-muted">${this.getRewardDescription(reward)}</small>
                                                    </div>
                                                    ${userProgress?.claimed ?
                '<span class="badge bg-success ms-auto">已领取</span>' :
                '<span class="badge bg-warning text-dark ms-auto">待领取</span>'
            }
                                                </div>
                                            `).join('')}
                                        </div>
                                    </div>
                                    
                                    <!-- 统计信息 -->
                                    <div class="activity-stats p-3 rounded" style="background: rgba(255,255,255,0.05);">
                                        <h6 class="text-warning mb-3">📊 活动统计</h6>
                                        <div class="row text-center">
                                            <div class="col-6 mb-3">
                                                <div class="stat-item">
                                                    <div class="stat-value text-warning">${activity.currentProgress}</div>
                                                    <div class="stat-label small">已进行天数</div>
                                                </div>
                                            </div>
                                            <div class="col-6 mb-3">
                                                <div class="stat-item">
                                                    <div class="stat-value text-info">${activity.requiredProgress - activity.currentProgress}</div>
                                                    <div class="stat-label small">剩余天数</div>
                                                </div>
                                            </div>
                                            <div class="col-6">
                                                <div class="stat-item">
                                                    <div class="stat-value text-success">${Math.round(progressPercent)}%</div>
                                                    <div class="stat-label small">完成度</div>
                                                </div>
                                            </div>
                                            <div class="col-6">
                                                <div class="stat-item">
                                                    <div class="stat-value text-danger">${this.calculateDailyGoal(activity)}分钟</div>
                                                    <div class="stat-label small">每日目标</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="modal-footer border-0">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                            
                            ${activity.active && !userProgress?.completed ? `
                                <button type="button" class="btn btn-warning" 
                                        onclick="christmasActivity.startActivityNow('${activity.id}')">
                                    <i class="fas fa-play me-2"></i>立即开始
                                </button>
                            ` : ''}
                            
                            ${userProgress?.completed && !userProgress?.claimed ? `
                                <button type="button" class="btn btn-success" 
                                        onclick="christmasActivity.claimRewards('${activity.id}')">
                                    <i class="fas fa-gift me-2"></i>领取奖励
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.showModal(modalHtml, 'activityDetailModal');
    }

    getRewardIcon(rewardType) {
        const icons = {
            'badge': 'fa-award',
            'points': 'fa-coins',
            'cosmetic': 'fa-palette',
            'item': 'fa-box-open',
            'boost': 'fa-bolt',
            'special': 'fa-gem'
        };
        return icons[rewardType] || 'fa-gift';
    }

    getRewardColor(rewardType) {
        const colors = {
            'badge': 'warning',
            'points': 'info',
            'cosmetic': 'primary',
            'item': 'success',
            'boost': 'danger',
            'special': 'danger'
        };
        return colors[rewardType] || 'secondary';
    }

    getRewardDescription(reward) {
        if (reward.type === 'points') {
            return `价值 ${reward.value || '随机'} 积分`;
        } else if (reward.type === 'badge') {
            return `稀有度: ${reward.rarity || '普通'}`;
        } else if (reward.type === 'boost') {
            return '有效期: 24小时';
        }
        return '圣诞特别奖励';
    }

    calculateDailyGoal(activity) {
        // 根据挑战类型计算每日目标
        if (activity.id.includes('santa_challenge')) {
            return 30; // 每天30分钟
        } else if (activity.id.includes('marathon')) {
            return Math.ceil((1000 - activity.currentProgress) / (activity.requiredProgress - activity.currentProgress));
        }
        return 20; // 默认20分钟
    }

    showBadgeDetails(badgeId) {
        const badge = this.christmasBadges.find(b => b.id == badgeId);
        if (!badge) return;

        const modalHtml = `
            <div class="modal fade" id="badgeDetailModal" tabindex="-1">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content christmas-modal text-center">
                        <div class="modal-header border-0 justify-content-center">
                            <h5 class="modal-title">徽章详情</h5>
                        </div>
                        
                        <div class="modal-body">
                            <!-- 徽章展示区域 -->
                            <div class="badge-display mb-4">
                                <div class="badge-icon-lg mx-auto mb-3 ${badge.animation}" 
                                     style="background: linear-gradient(135deg, ${badge.color}, ${this.getLightColor(badge.color)});">
                                    <i class="${badge.icon} fa-3x"></i>
                                    ${badge.obtained ?
                '<div class="shine-effect"></div>' :
                '<div class="lock-overlay"><i class="fas fa-lock fa-2x"></i></div>'
            }
                                </div>
                                
                                <h4 class="text-warning mb-2">${badge.name}</h4>
                                <p class="text-muted mb-3">${badge.description}</p>
                                
                                <!-- 稀有度显示 -->
                                <div class="rarity-display mb-3">
                                    <span class="badge bg-${this.getRarityColor(badge.rarity)} px-3 py-2">
                                        <i class="fas fa-gem me-2"></i>${this.getRarityText(badge.rarity)}品质
                                    </span>
                                </div>
                            </div>
                            
                            <!-- 详细信息 -->
                            <div class="badge-info">
                                <div class="info-section mb-3">
                                    <h6 class="text-warning mb-2">详细信息</h6>
                                    <p class="small text-muted mb-1">${badge.fullDescription || badge.description}</p>
                                </div>
                                
                                <div class="info-section mb-3">
                                    <h6 class="text-warning mb-2">获取方式</h6>
                                    <p class="small text-muted">
                                        <i class="fas fa-trophy me-2"></i>${badge.obtainMethod}
                                    </p>
                                    ${badge.obtained && badge.unlockDate ?
                `<p class="small text-success">
                                            <i class="fas fa-calendar-check me-2"></i>获得时间: ${badge.unlockDate}
                                        </p>` : ''
            }
                                </div>
                                
                                <div class="info-section mb-3">
                                    <h6 class="text-warning mb-2">特别效果</h6>
                                    <div class="d-flex flex-wrap justify-content-center gap-1">
                                        ${badge.specialEffects.map(effect => `
                                            <span class="badge bg-info text-dark small">${effect}</span>
                                        `).join('')}
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="modal-footer border-0 justify-content-center">
                            ${badge.obtained ? `
                                <button type="button" class="btn btn-success" onclick="christmasActivity.equipBadge(${badge.id})">
                                    <i class="fas fa-check me-2"></i>已获得
                                </button>
                                <button type="button" class="btn btn-outline-light" onclick="christmasActivity.shareBadge(${badge.id})">
                                    <i class="fas fa-share-alt me-2"></i>分享
                                </button>
                            ` : `
                                <button type="button" class="btn btn-warning" onclick="christmasActivity.gotoObtainMethod(${badge.id})">
                                    <i class="fas fa-flag me-2"></i>获取此徽章
                                </button>
                            `}
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.showModal(modalHtml, 'badgeDetailModal');
    }

    getLightColor(color) {
        // 生成颜色变亮版本
        const hex = color.replace('#', '');
        const r = parseInt(hex.substr(0, 2), 16);
        const g = parseInt(hex.substr(2, 2), 16);
        const b = parseInt(hex.substr(4, 2), 16);

        const lighten = (c) => Math.min(255, c + 60);

        return `rgb(${lighten(r)}, ${lighten(g)}, ${lighten(b)})`;
    }

    // 圣诞活动功能方法
    startActivity(activityId) {
        const activity = this.activities.find(a => a.id === activityId);
        if (!activity) {
            this.showNotification('活动不存在', 'danger');
            return;
        }

        if (!activity.active) {
            this.showNotification('此活动暂未开启', 'warning');
            return;
        }

        if (this.userProgress[activityId]?.completed) {
            this.showNotification('您已经完成这个活动了！', 'info');
            return;
        }

        // 开始活动
        this.userProgress[activityId] = {
            ...this.userProgress[activityId],
            startDate: new Date().toISOString(),
            lastUpdate: new Date().toISOString(),
            status: 'in_progress'
        };

        this.saveProgress();

        this.showNotification(`🎯 已开始 "${activity.name}" 挑战！`, 'success');
        this.updateProgressBars();
        this.displayActivities();

        // 显示活动提示
        this.showActivityTip(activity);
    }

    startActivityNow(activityId) {
        this.startActivity(activityId);

        // 关闭模态框
        const modal = bootstrap.Modal.getInstance(document.getElementById('activityDetailModal'));
        if (modal) modal.hide();
    }

    showActivityTip(activity) {
        const tipHtml = `
            <div class="toast show position-fixed" style="bottom: 80px; right: 20px; z-index: 1055;">
                <div class="toast-header bg-warning text-dark">
                    <strong class="me-auto">🎯 挑战开始！</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
                </div>
                <div class="toast-body">
                    <p><strong>${activity.name}</strong> 已开始！</p>
                    <p class="small text-muted">${activity.description}</p>
                    <div class="d-grid gap-2">
                        <button class="btn btn-sm btn-outline-warning" onclick="christmasActivity.recordProgress('${activity.id}')">
                            记录今日进度
                        </button>
                    </div>
                </div>
            </div>
        `;

        const container = document.createElement('div');
        container.innerHTML = tipHtml;
        document.body.appendChild(container);

        // 5秒后自动消失
        setTimeout(() => {
            if (container.parentNode) {
                container.parentNode.removeChild(container);
            }
        }, 5000);
    }

    recordProgress(activityId) {
        const activity = this.activities.find(a => a.id === activityId);
        if (!activity) return;

        const progress = this.userProgress[activityId];
        if (!progress || progress.completed) return;

        // 更新进度
        if (activity.currentProgress < activity.requiredProgress) {
            activity.currentProgress++;
            progress.progress = activity.currentProgress;
            progress.lastUpdate = new Date().toISOString();
            progress.history = [...(progress.history || []), {
                date: new Date().toISOString(),
                progress: activity.currentProgress
            }];

            // 检查是否完成
            if (activity.currentProgress >= activity.requiredProgress) {
                progress.completed = true;
                this.showChallengeCompleted(activity);
            }

            this.saveProgress();
            this.updateProgressBars();
            this.displayActivities();

            this.showNotification(`🎉 进度更新！当前进度: ${activity.currentProgress}/${activity.requiredProgress}`, 'success');
        }
    }

    showChallengeCompleted(activity) {
        const completedHtml = `
            <div class="modal fade" id="challengeCompletedModal" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content christmas-modal text-center">
                        <div class="modal-header border-0 justify-content-center">
                            <h4 class="modal-title text-warning">🎉 挑战完成！ 🎉</h4>
                        </div>
                        
                        <div class="modal-body">
                            <div class="celebration-animation mb-4">
                                <i class="fas fa-trophy fa-5x text-warning mb-3"></i>
                                <div class="confetti"></div>
                            </div>
                            
                            <h5 class="mb-3">恭喜完成 ${activity.name}！</h5>
                            <p class="text-muted mb-4">您展现了非凡的毅力和坚持，成功完成了这个挑战！</p>
                            
                            <div class="rewards-earned mb-4">
                                <h6 class="text-warning mb-2">获得的奖励</h6>
                                <div class="d-flex justify-content-center flex-wrap gap-2">
                                    ${activity.rewards.map(reward => `
                                        <span class="badge bg-warning text-dark px-3 py-2">
                                            ${reward.name}
                                        </span>
                                    `).join('')}
                                </div>
                            </div>
                        </div>
                        
                        <div class="modal-footer border-0 justify-content-center">
                            <button type="button" class="btn btn-success btn-lg" 
                                    onclick="christmasActivity.claimRewards('${activity.id}')">
                                <i class="fas fa-gift me-2"></i>领取奖励
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.showModal(completedHtml, 'challengeCompletedModal');
    }

    claimRewards(activityId) {
        const activity = this.activities.find(a => a.id === activityId);
        if (!activity) return;

        const progress = this.userProgress[activityId];
        if (!progress || !progress.completed || progress.claimed) {
            this.showNotification('无法领取奖励', 'warning');
            return;
        }

        // 标记为已领取
        progress.claimed = true;
        progress.claimDate = new Date().toISOString();
        this.saveProgress();

        // 发放奖励
        this.grantRewards(activity.rewards);

        // 关闭模态框
        this.closeAllModals();

        // 显示奖励领取成功
        this.showRewardClaimed(activity);
    }

    grantRewards(rewards) {
        rewards.forEach(reward => {
            switch(reward.type) {
                case 'points':
                    this.addPoints(reward.value || 50);
                    break;
                case 'badge':
                    this.unlockBadge(reward.name);
                    break;
                default:
                    console.log(`获得奖励: ${reward.name}`);
            }
        });

        this.showNotification('🎁 副励已发放到您的账户！', 'success');
    }

    addPoints(points) {
        // 更新积分
        const currentPoints = parseInt(localStorage.getItem('userPoints') || '0');
        localStorage.setItem('userPoints', (currentPoints + points).toString());

        this.showNotification(`💰 获得 ${points} 积分！`, 'success');
    }

    unlockBadge(badgeName) {
        const badge = this.christmasBadges.find(b => b.name === badgeName);
        if (badge && !badge.obtained) {
            badge.obtained = true;
            badge.unlockDate = new Date().toISOString().split('T')[0];
            this.saveProgress();
            this.displayChristmasBadges();

            this.showNotification(`🏅 获得新徽章: ${badgeName}！`, 'success');
        }
    }

    showRewardClaimed(activity) {
        const html = `
            <div class="position-fixed top-50 start-50 translate-middle" style="z-index: 1060;">
                <div class="card christmas-modal" style="width: 300px;">
                    <div class="card-body text-center">
                        <i class="fas fa-gift fa-4x text-warning mb-3"></i>
                        <h4 class="text-success mb-3">奖励领取成功！</h4>
                        <p class="mb-3">您已成功领取 ${activity.name} 的所有奖励！</p>
                        <button class="btn btn-warning w-100" onclick="this.parentElement.parentElement.parentElement.remove()">
                            确定
                        </button>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', html);

        setTimeout(() => {
            const element = document.querySelector('.position-fixed.top-50.start-50');
            if (element) element.remove();
        }, 5000);
    }

    checkDailyGift() {
        const today = new Date().toISOString().split('T')[0];
        const todayGift = this.dailyGifts.find(g => g.date === today);

        if (todayGift && !todayGift.claimed) {
            this.showDailyGiftReminder();
        }
    }

    showDailyGiftReminder() {
        const reminder = `
            <div class="alert alert-warning alert-dismissible fade show position-fixed" 
                 style="top: 100px; right: 20px; z-index: 1055; min-width: 300px;">
                <div class="d-flex align-items-center">
                    <i class="fas fa-gift fa-2x me-3"></i>
                    <div>
                        <h6 class="mb-1">🎁 今日礼物待领取！</h6>
                        <p class="mb-0 small">完成任意运动即可开启今日圣诞礼物</p>
                    </div>
                    <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
                </div>
                <div class="d-grid gap-2 mt-2">
                    <button class="btn btn-sm btn-outline-warning" onclick="openDailyGift()">
                        <i class="fas fa-box-open me-2"></i>开启礼物
                    </button>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', reminder);
    }

    openDailyGift(day = new Date().getDate()) {
        const gift = this.dailyGifts.find(g => g.day === day);
        if (!gift) {
            this.showNotification('今天没有礼物可开启', 'info');
            return;
        }

        if (gift.claimed) {
            this.showNotification('今天的礼物已经开启过了', 'warning');
            return;
        }

        // 标记为已领取
        gift.claimed = true;
        this.saveProgress();

        // 显示开礼物动画
        this.playGiftAnimation(gift);
    }

    playGiftAnimation(gift) {
        const container = document.createElement('div');
        container.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.8);
            z-index: 9999;
            display: flex;
            align-items: center;
            justify-content: center;
        `;

        container.innerHTML = `
            <div class="gift-animation text-center">
                <div class="gift-box mb-4">
                    <i class="fas fa-gift fa-6x text-warning"></i>
                    <div class="ribbon"></div>
                </div>
                
                <h2 class="text-light mb-4">开启圣诞礼物！</h2>
                
                <div id="giftReveal" class="reveal-animation">
                    <i class="fas fa-spinner fa-spin fa-3x"></i>
                </div>
            </div>
        `;

        document.body.appendChild(container);

        // 延迟显示礼物内容
        setTimeout(() => {
            const reveal = document.getElementById('giftReveal');
            if (reveal) {
                reveal.innerHTML = `
                    <div class="reward-display">
                        <i class="fas fa-${this.getGiftIcon(gift.reward)} fa-4x text-warning mb-3"></i>
                        <h3 class="text-warning mb-2">恭喜获得！</h3>
                        <h4 class="text-light mb-3">${this.getGiftRewardText(gift.reward)}</h4>
                        <button class="btn btn-warning btn-lg" onclick="this.closest('.gift-animation').parentElement.remove()">
                            领取奖励
                        </button>
                    </div>
                `;

                // 发放奖励
                this.grantGiftReward(gift.reward);
            }
        }, 2000);
    }

    getGiftIcon(reward) {
        const icons = {
            'points': 'fa-coins',
            'cosmetic': 'fa-palette',
            'badge': 'fa-award',
            'boost': 'fa-bolt',
            'special': 'fa-gem'
        };
        return icons[reward.type] || 'fa-gift';
    }

    getGiftRewardText(reward) {
        if (reward.type === 'points') {
            return `${reward.value || 10} 积分`;
        } else if (reward.type === 'special') {
            return '神秘大礼包！';
        }
        return reward.name || '圣诞礼物';
    }

    grantGiftReward(reward) {
        if (reward.type === 'points') {
            this.addPoints(reward.value || 10);
        } else if (reward.type === 'badge') {
            this.unlockBadge(reward.name);
        } else {
            this.showNotification(`🎁 获得: ${reward.name || '圣诞礼物'}`, 'success');
        }
    }

    shareProgress() {
        const earnedBadges = this.christmasBadges.filter(b => b.obtained).length;
        const totalBadges = this.christmasBadges.length;
        const completedActivities = this.activities.filter(a =>
            this.userProgress[a.id]?.completed
        ).length;

        const shareText = `🎄 我在健康星球圣诞活动中取得了不错的成绩！\n` +
            `🏅 已获得 ${earnedBadges}/${totalBadges} 枚徽章\n` +
            `🏃‍♂️ 完成了 ${completedActivities}/${this.activities.length} 个挑战\n` +
            `🌟 一起加入圣诞健康挑战吧！`;

        if (navigator.share) {
            navigator.share({
                title: '我的圣诞健康成就',
                text: shareText,
                url: window.location.href
            });
        } else {
            navigator.clipboard.writeText(shareText + '\n' + window.location.href)
                .then(() => this.showNotification('成就已复制到剪贴板，快去分享给朋友吧！', 'success'));
        }
    }

    shareBadge(badgeId) {
        const badge = this.christmasBadges.find(b => b.id == badgeId);
        if (!badge) return;

        const shareText = `🏅 我在健康星球获得了 "${badge.name}" 徽章！\n` +
            `🎄 这是圣诞活动的专属奖励\n` +
            `🌟 稀有度: ${this.getRarityText(badge.rarity)}\n` +
            `💪 一起加入健康挑战吧！`;

        if (navigator.share) {
            navigator.share({
                title: `获得徽章: ${badge.name}`,
                text: shareText,
                url: window.location.href
            });
        } else {
            navigator.clipboard.writeText(shareText + '\n' + window.location.href)
                .then(() => this.showNotification('徽章信息已复制到剪贴板', 'success'));
        }
    }

    equipBadge(badgeId) {
        localStorage.setItem('equippedBadge', badgeId);
        this.showNotification('徽章已装备！', 'success');
    }

    gotoObtainMethod(badgeId) {
        const badge = this.christmasBadges.find(b => b.id == badgeId);
        if (!badge) return;

        // 根据徽章获取方式跳转到对应活动
        if (badge.obtainMethod.includes('圣诞老人挑战')) {
            this.showActivityDetails('santa_challenge_2024');
        } else if (badge.obtainMethod.includes('收集')) {
            this.showActivityDetails('tree_decorator_2024');
        } else {
            this.showNotification('完成相关挑战即可获得此徽章', 'info');
        }
    }

    updateProgressBars() {
        // 更新所有活动进度条
        document.querySelectorAll('[data-activity-id]').forEach(element => {
            const activityId = element.dataset.activityId;
            const activity = this.activities.find(a => a.id === activityId);
            if (activity) {
                const progressPercent = this.calculateProgress(activity);
                const progressBar = element.querySelector('.progress-bar');
                if (progressBar) {
                    progressBar.style.width = `${progressPercent}%`;
                    progressBar.textContent = `${activity.currentProgress}/${activity.requiredProgress}`;
                }
            }
        });
    }

    updateAllProgress() {
        this.displayActivities();
        this.displayChristmasBadges();
        this.updateProgressBars();
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
        const modalElement = document.getElementById(modalId);
        if (modalElement) {
            const modal = new bootstrap.Modal(modalElement);
            modal.show();

            // 模态框关闭时清理
            modalElement.addEventListener('hidden.bs.modal', function() {
                this.remove();
            });
        }
    }

    closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal => {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        });
    }

    handleChristmasArrival() {
        clearInterval(this.countdownInterval);

        // 显示圣诞快乐特效
        this.showChristmasCelebration();

        // 检查并发放圣诞特别奖励
        this.grantChristmasSpecialRewards();
    }

    showChristmasCelebration() {
        const celebration = `
            <div class="position-fixed top-0 left-0 w-100 h-100" style="z-index: 9999; pointer-events: none;">
                <div class="d-flex flex-column align-items-center justify-content-center h-100">
                    <h1 class="display-1 text-warning mb-4">🎄 圣诞快乐！ 🎄</h1>
                    <h2 class="text-light mb-4">🎁 祝您和您的家人节日快乐！ 🎁</h2>
                    <div class="confetti-show"></div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', celebration);

        // 5秒后移除
        setTimeout(() => {
            const element = document.querySelector('.position-fixed.top-0.left-0');
            if (element) element.remove();
        }, 5000);
    }

    grantChristmasSpecialRewards() {
        // 发放圣诞特别奖励
        const earnedBadges = this.christmasBadges.filter(b => b.obtained).length;
        if (earnedBadges >= 5) {
            this.showNotification('🎁 恭喜您获得圣诞终极奖励！', 'success');
            this.unlockBadge('圣诞之星');
        }
    }

    initGiftAnimation() {
        // 初始化礼物动画效果
        const style = document.createElement('style');
        style.textContent = `
            @keyframes bounce {
                0%, 100% { transform: translateY(0); }
                50% { transform: translateY(-20px); }
            }
            
            @keyframes pulse {
                0%, 100% { opacity: 1; }
                50% { opacity: 0.7; }
            }
            
            @keyframes shine {
                0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
                100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
            }
            
            @keyframes heartbeat {
                0%, 100% { transform: scale(1); }
                50% { transform: scale(1.1); }
            }
            
            .gift-animation {
                animation: bounce 1s infinite;
            }
            
            .premium-ribbon {
                position: absolute;
                top: 10px;
                right: -25px;
                background: linear-gradient(45deg, #ffd700, #ff6b6b);
                color: #000;
                padding: 5px 30px;
                transform: rotate(45deg);
                font-weight: bold;
                font-size: 0.8rem;
                box-shadow: 0 2px 5px rgba(0,0,0,0.2);
            }
            
            .shine-effect {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
                animation: shine 2s infinite;
            }
            
            .confetti {
                position: absolute;
                width: 10px;
                height: 10px;
                background: var(--christmas-red);
                border-radius: 50%;
                animation: fall 5s linear infinite;
            }
        `;
        document.head.appendChild(style);
    }

    // 添加缺失的 getGiftAnimation 方法
    getGiftAnimation(date) {
        // 根据日期返回相应的礼物动画
        const day = date.getDate();
        const animations = ['bounce', 'pulse', 'shake', 'swing'];
        return animations[day % animations.length];
    }

    initScrollAnimations() {
        // 滚动时触发动画
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate__animated', 'animate__fadeInUp');
                }
            });
        }, { threshold: 0.1 });

        // 观察所有活动卡片和徽章卡片
        document.querySelectorAll('.activity-card, .badge-card').forEach(card => {
            observer.observe(card);
        });
    }
}

// 全局函数
function startChristmasChallenge() {
    if (window.christmasActivity) {
        window.christmasActivity.startActivity('santa_challenge_2024');
    }
}

function openDailyGift() {
    if (window.christmasActivity) {
        const today = new Date().getDate();
        window.christmasActivity.openDailyGift(today);
    }
}

function shareChristmasProgress() {
    if (window.christmasActivity) {
        window.christmasActivity.shareProgress();
    }
}

function showActivityDetails(activityId) {
    if (window.christmasActivity) {
        window.christmasActivity.showActivityDetails(activityId);
    }
}

function showBadgeDetails(badgeId) {
    if (window.christmasActivity) {
        window.christmasActivity.showBadgeDetails(badgeId);
    }
}

// 初始化圣诞活动
document.addEventListener('DOMContentLoaded', function() {
    window.christmasActivity = new ChristmasActivity();
});

// 页面卸载时清理
window.addEventListener('beforeunload', function() {
    if (window.christmasActivity) {
        window.christmasActivity.saveProgress();
        clearInterval(window.christmasActivity.snowInterval);
        clearInterval(window.christmasActivity.countdownInterval);
    }
});

// 导出全局变量
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ChristmasActivity;
}