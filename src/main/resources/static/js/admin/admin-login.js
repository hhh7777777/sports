document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('adminForm');
    const captchaImage = document.getElementById('captchaImage');

    // 初始化验证码
    refreshCaptcha();

    // 验证码点击刷新
    if (captchaImage) {
        captchaImage.addEventListener('click', refreshCaptcha);
    }

    // 表单提交处理
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('adminUsername').value;
            const password = document.getElementById('password').value;
            const captcha = document.getElementById('captcha').value;
            const rememberMe = document.getElementById('adminRememberMe') ? document.getElementById('adminRememberMe').checked : false;

            // 简单前端验证
            if (!username || !password || !captcha) {
                showAlert('请填写完整信息', 'danger');
                return;
            }

            try {
                const response = await fetch('/api/admin/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username,
                        password,
                        captcha
                    })
                });

                const result = await response.json();

                if (result.code === 200) {
                    // 存储token
                    if (result.data && result.data.token) {
                        if (rememberMe) {
                            localStorage.setItem('adminToken', result.data.token);
                        } else {
                            sessionStorage.setItem('adminToken', result.data.token);
                        }
                    }

                    // 跳转到管理员仪表板
                    window.location.href = '/admin/dashboard';
                } else {
                    showAlert(result.message || '登录失败', 'danger');
                    refreshCaptcha();
                }
            } catch (error) {
                console.error('登录错误:', error);
                showAlert('网络错误，请稍后重试', 'danger');
            }
        });
    }

    function refreshCaptcha() {
        const captchaImage = document.getElementById('captchaImage');
        if (captchaImage) {
            captchaImage.src = '/api/common/captcha?t=' + new Date().getTime();
        }
    }

    function showAlert(message, type) {
        const successMessage = document.getElementById('successMessage');
        const errorMessage = document.getElementById('errorMessage');
        const messageArea = document.getElementById('messageArea');
        
        // 隐藏所有消息
        if (successMessage) successMessage.style.display = 'none';
        if (errorMessage) errorMessage.style.display = 'none';
        
        if (type === 'success') {
            if (successMessage) {
                document.getElementById('successText').textContent = message;
                successMessage.style.display = 'block';
            }
        } else {
            if (errorMessage) {
                document.getElementById('errorText').textContent = message;
                errorMessage.style.display = 'block';
            }
        }
        
        if (messageArea) {
            messageArea.style.display = 'block';
        }
        
        // 3秒后自动隐藏消息
        setTimeout(() => {
            if (messageArea) messageArea.style.display = 'none';
        }, 3000);
    }
});