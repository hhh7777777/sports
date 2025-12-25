class ApiService {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...authManager.getAuthHeaders(),
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    // 用户相关API
    async getUserProfile() {
        return this.request('/api/user/profile');
    }

    async getUserActivities(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/user/activities?${queryString}`);
    }

    async addActivity(activityData) {
        return this.request('/api/user/activities', {
            method: 'POST',
            body: JSON.stringify(activityData)
        });
    }

    async deleteActivity(activityId) {
        return this.request(`/api/user/activities/${activityId}`, {
            method: 'DELETE'
        });
    }

    // 成就相关API
    async getUserAchievements() {
        return this.request('/api/user/achievements');
    }

    // 管理员API
    async getAdminStats() {
        return this.request('/api/admin/stats');
    }

    async getAdminUsers(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/api/admin/users?${queryString}`);
    }
}

// 全局API实例
window.apiService = new ApiService();