document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordStrength = document.getElementById('passwordStrengthMeter');
    const captchaImage = document.getElementById('captchaImage');

    // 初始化验证码
    refreshCaptcha();

    // 密码强度检查
    passwordInput.addEventListener('input', checkPasswordStrength);

    // 确认密码验证
    confirmPasswordInput.addEventListener('input', validateConfirmPassword);

    // 验证码点击刷新
    captchaImage && captchaImage.addEventListener('click', refreshCaptcha);

    // 表单提交处理
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        if (!validateForm()) return;

        const formData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: passwordInput.value,
            confirmPassword: document.getElementById('confirmPassword').value,
            captcha: document.getElementById('captcha').value
        };

        try {
            const response = await fetch('/api/user/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok && result.code === 200) {
                showAlert('注册成功！正在跳转...', 'success');
                
                // 触发注册成功事件，以便其他页面更新UI
                const registerEvent = new CustomEvent('userLoginSuccess', {
                    detail: { message: '注册成功' }
                });
                document.dispatchEvent(registerEvent);
                
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            } else {
                showAlert(result.message || '注册失败', 'danger');
                refreshCaptcha();
            }
        } catch (error) {
            console.error('注册错误:', error);
            showAlert('网络错误，请稍后重试', 'danger');
        }
    });

    function checkPasswordStrength() {
        const password = passwordInput.value;
        let strength = 0;

        if (password.length >= 8) strength++;
        if (/[A-Z]/.test(password)) strength++;
        if (/[0-9]/.test(password)) strength++;
        if (/[^A-Za-z0-9]/.test(password)) strength++;

        const strengthMeter = passwordStrength;
        strengthMeter.className = 'strength-meter';

        if (password.length === 0) {
            strengthMeter.style.width = '0%';
        } else if (strength <= 1) {
            strengthMeter.className += ' strength-weak';
            strengthMeter.style.width = '25%';
        } else if (strength === 2) {
            strengthMeter.className += ' strength-medium';
            strengthMeter.style.width = '50%';
        } else if (strength === 3) {
            strengthMeter.className += ' strength-strong';
            strengthMeter.style.width = '75%';
        } else {
            strengthMeter.className += ' strength-very-strong';
            strengthMeter.style.width = '100%';
        }
    }

    function validateConfirmPassword() {
        if (confirmPasswordInput.value !== passwordInput.value) {
            confirmPasswordInput.classList.add('is-invalid');
        } else {
            confirmPasswordInput.classList.remove('is-invalid');
        }
    }

    function validateForm() {
        let isValid = true;

        // 验证用户名
        const username = document.getElementById('username').value;
        if (username.length < 4 || username.length > 20) {
            showFieldError('username', '用户名长度应在4-20个字符之间');
            isValid = false;
        }

        // 验证邮箱
        const email = document.getElementById('email').value;
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            showFieldError('email', '请输入有效的邮箱地址');
            isValid = false;
        }

        // 验证密码
        if (passwordInput.value.length < 8) {
            showFieldError('password', '密码长度至少8位');
            isValid = false;
        }

        // 验证确认密码
        if (confirmPasswordInput.value !== passwordInput.value) {
            showFieldError('confirmPassword', '两次输入的密码不一致');
            isValid = false;
        }

        // 验证验证码
        if (!document.getElementById('captcha').value) {
            showFieldError('captcha', '请输入验证码');
            isValid = false;
        }

        // 验证条款同意
        if (!document.getElementById('agreeTerms').checked) {
            showAlert('请同意服务条款和隐私政策', 'danger');
            isValid = false;
        }

        return isValid;
    }

    function showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        field.classList.add('is-invalid');

        let errorDiv = field.nextElementSibling;
        if (!errorDiv || !errorDiv.classList.contains('invalid-feedback')) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            field.parentNode.appendChild(errorDiv);
        }

        errorDiv.textContent = message;
    }

    // 验证码刷新功能
    function refreshCaptcha() {
        const captchaImage = document.getElementById('captchaImage');
        if (captchaImage) {
            captchaImage.src = '/api/common/captcha?t=' + new Date().getTime();
        }
    }

    // 初始化验证码点击事件
    captchaImage && captchaImage.addEventListener('click', refreshCaptcha);

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
