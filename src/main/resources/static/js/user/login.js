document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const captchaImg = document.getElementById('captchaImg');

    // 初始化验证码
    refreshCaptcha();

    // 验证码点击刷新
    if (captchaImg) {
        captchaImg.addEventListener('click', refreshCaptcha);
    }

    // 表单提交处理
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const captcha = document.getElementById('captcha').value;
            const rememberMe = document.getElementById('rememberMe').checked;

            // 简单前端验证
            if (!username || !password || !captcha) {
                showAlert('请填写完整信息', 'danger');
                return;
            }

            try {
                const response = await fetch('/api/user/login', {
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
                            localStorage.setItem('accessToken', result.data.token);
                        } else {
                            sessionStorage.setItem('accessToken', result.data.token);
                        }
                    }

                    // 根据项目规范，登录成功后应返回首页而不是仪表板
                    window.location.href = '/';
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
        const captchaImage = document.getElementById('captchaImg');
        if (captchaImage) {
            captchaImage.src = '/api/common/captcha?t=' + new Date().getTime();
        }
    }

    function showAlert(message, type) {
        // 移除现有的提示
        const existingAlert = document.querySelector('.alert');
        if (existingAlert) {
            existingAlert.remove();
        }

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} mt-3`;
        alertDiv.textContent = message;

        const container = document.querySelector('.login-body');
        const form = document.getElementById('loginForm');
        container.insertBefore(alertDiv, form);

        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    }
});