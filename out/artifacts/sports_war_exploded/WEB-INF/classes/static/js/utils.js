/**
 * 工具类，扩展CommonUtils的功能
 */
class Utils extends CommonUtils {
    // 可以在这里添加项目特定的工具方法
    
    /**
     * 格式化运动时长（分钟转为小时和分钟）
     * @param {number} minutes - 分钟数
     * @returns {string} 格式化后的时长字符串
     */
    static formatDuration(minutes) {
        if (!minutes) return '0分钟';
        
        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;
        
        if (hours > 0) {
            return `${hours}小时${remainingMinutes}分钟`;
        } else {
            return `${remainingMinutes}分钟`;
        }
    }
    
    /**
     * 计算两个日期之间的天数差
     * @param {Date} startDate - 开始日期
     * @param {Date} endDate - 结束日期
     * @returns {number} 天数差
     */
    static daysBetween(startDate, endDate) {
        const oneDay = 24 * 60 * 60 * 1000; // 小时*分钟*秒*毫秒
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        // 设置时间为00:00:00，避免时间影响计算
        start.setHours(0, 0, 0, 0);
        end.setHours(0, 0, 0, 0);
        
        return Math.round(Math.abs((start - end) / oneDay));
    }
    
    /**
     * 生成随机颜色
     * @returns {string} 随机颜色的HEX值
     */
    static getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
}

// 保持向后兼容性
window.Utils = Utils;

class Utils {
    static formatDate(date, format = 'YYYY-MM-DD') {
        if (!date) return '';
        
        const d = new Date(date);
        if (isNaN(d.getTime())) return ''; // 检查日期是否有效
        
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const hour = String(d.getHours()).padStart(2, '0');
        const minute = String(d.getMinutes()).padStart(2, '0');

        return format
            .replace('YYYY', year)
            .replace('MM', month)
            .replace('DD', day)
            .replace('HH', hour)
            .replace('mm', minute);
    }

    static getQueryParam(name) {
        try {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get(name);
        } catch (error) {
            console.error('Error getting query param:', error);
            return null;
        }
    }

    static debounce(func, wait) {
        if (typeof func !== 'function') return;
        
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    static throttle(func, limit) {
        if (typeof func !== 'function') return;
        
        let inThrottle;
        return function() {
            const args = arguments;
            const context = this;
            if (!inThrottle) {
                func.apply(context, args);
                inThrottle = true;
                setTimeout(() => inThrottle = false, limit);
            }
        };
    }

    static isValidEmail(email) {
        if (!email) return false;
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    static isValidPhone(phone) {
        if (!phone) return false;
        const re = /^1[3-9]\d{9}$/;
        return re.test(phone);
    }

    static showAlert(message, type = 'info', duration = 5000) {
        // 检查必要的参数
        if (!message) return;
        
        const alertId = 'alert-' + Date.now();
        const iconMap = {
            success: 'check-circle',
            error: 'exclamation-triangle',
            warning: 'exclamation-circle',
            info: 'info-circle'
        };

        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
                <i class="fas fa-${iconMap[type] || 'info-circle'} me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        const container = document.getElementById('alert-container');
        if (container) {
            container.insertAdjacentHTML('beforeend', alertHtml);

            if (duration > 0) {
                setTimeout(() => {
                    const alertElement = document.getElementById(alertId);
                    if (alertElement && alertElement.parentNode) {
                        alertElement.parentNode.removeChild(alertElement);
                    }
                }, duration);
            }
        } else {
            // 如果找不到容器，直接在控制台输出信息
            console.log(`[${type}] ${message}`);
        }
    }

    static storage = {
        set: (key, value) => {
            try {
                if (!key) return;
                localStorage.setItem(key, JSON.stringify(value));
            } catch (e) {
                console.error('LocalStorage set error:', e);
            }
        },

        get: (key) => {
            try {
                if (!key) return null;
                const item = localStorage.getItem(key);
                return item ? JSON.parse(item) : null;
            } catch (e) {
                console.error('LocalStorage get error:', e);
                return null;
            }
        },

        remove: (key) => {
            try {
                if (!key) return;
                localStorage.removeItem(key);
            } catch (e) {
                console.error('LocalStorage remove error:', e);
            }
        }
    };
}