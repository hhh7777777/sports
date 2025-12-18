document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordStrength = document.getElementById('passwordStrength');
    const captchaImg = document.getElementById('captchaImg');

    // 初始化验证码
    refreshCaptcha();

    // 密码强度检查
    passwordInput.addEventListener('input', checkPasswordStrength);

    // 确认密码验证
    confirmPasswordInput.addEventListener('input', validateConfirmPassword);

    // 验证码点击刷新
    captchaImg.addEventListener('click', refreshCaptcha);

    // 表单提交处理
    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        if (!validateForm()) return;

        const formData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: passwordInput.value,
            captcha: document.getElementById('captcha').value
        };

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (response.ok) {
                showAlert('注册成功！正在跳转...', 'success');
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

        passwordStrength.className = 'password-strength';

        if (password.length === 0) {
            passwordStrength.style.width = '0%';
        } else if (strength <= 1) {
            passwordStrength.className += ' strength-weak';
            passwordStrength.style.width = '25%';
        } else if (strength === 2) {
            passwordStrength.className += ' strength-medium';
            passwordStrength.style.width = '50%';
        } else if (strength === 3) {
            passwordStrength.className += ' strength-strong';
            passwordStrength.style.width = '75%';
        } else {
            passwordStrength.className += ' strength-very-strong';
            passwordStrength.style.width = '100%';
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
    document.getElementById('captchaImage').addEventListener('click', refreshCaptcha);

    // 页面加载时初始化验证码
    window.addEventListener('load', refreshCaptcha);

    function showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} mt-3`;
        alertDiv.textContent = message;

        const existingAlert = registerForm.querySelector('.alert');
        if (existingAlert) {
            existingAlert.replaceWith(alertDiv);
        } else {
            registerForm.appendChild(alertDiv);
        }

        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    }
});
