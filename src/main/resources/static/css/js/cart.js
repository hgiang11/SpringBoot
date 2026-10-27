(function () {
    'use strict';

    // Đánh dấu JavaScript đang chạy, để CSS ẩn nút "Cập nhật"
    document.documentElement.classList.add('js');

    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const headerName = document.querySelector('meta[name="_csrf_header"]')?.content;

    // Câu chữ đã được server dịch sẵn theo ngôn ngữ đang chọn (nằm trong thẻ meta)
    const t = {
        error: document.querySelector('meta[name="i18n-error"]')?.content || 'Error',
        qtyRange: document.querySelector('meta[name="i18n-qty-range"]')?.content || 'Invalid quantity'
    };

    function toast(message, isError) {
        const el = document.createElement('div');
        el.className = 'js-toast' + (isError ? ' error' : '');
        el.textContent = message;
        document.body.appendChild(el);
        requestAnimationFrame(() => el.classList.add('show'));
        setTimeout(() => {
            el.classList.remove('show');
            setTimeout(() => el.remove(), 300);
        }, 2500);
    }

    async function api(url, method, body) {
        const headers = { 'Accept': 'application/json' };
        if (body !== undefined) headers['Content-Type'] = 'application/json';
        if (token && headerName) headers[headerName] = token;

        const res = await fetch(url, {
            method: method,
            headers: headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });

        if (res.redirected) {
            window.location.href = res.url;
            return null;
        }
        const isJson = (res.headers.get('content-type') || '').includes('application/json');
        const data = isJson ? await res.json() : {};
        if (!res.ok) {
            throw new Error(data.message || t.error);
        }
        return data;
    }

    function updateSummary(data) {
        document.querySelectorAll('[data-cart-count]').forEach(el => {
            el.textContent = data.cartLabel;
        });
        document.querySelectorAll('[data-cart-total]').forEach(el => {
            el.textContent = data.cartTotal;
        });
    }

    // ===== Trang menu: thêm món vào giỏ =====
    document.querySelectorAll('form.js-add-to-cart').forEach(form => {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const button = form.querySelector('button[type="submit"]');
            button.disabled = true;
            try {
                const productId = Number(form.querySelector('[name="productId"]').value);
                const data = await api(form.dataset.url, 'POST', { productId: productId, quantity: 1 });
                if (data) {
                    updateSummary(data);
                    toast(data.message, false);
                }
            } catch (err) {
                toast(err.message, true);
            } finally {
                button.disabled = false;
            }
        });
    });

    // ===== Trang giỏ hàng: số lượng tự cập nhật khi thay đổi =====
    document.querySelectorAll('form.js-qty-form').forEach(form => {
        const input = form.querySelector('input[name="quantity"]');
        let lastValid = Number(input.value);
        let timer = null;
        let requestId = 0;

        async function save() {
            if (input.value === '') return;
            const quantity = Number(input.value);
            if (!Number.isInteger(quantity) || quantity < 1 || quantity > 20) {
                toast(t.qtyRange, true);
                input.value = lastValid;
                return;
            }
            if (quantity === lastValid) return;

            const myId = ++requestId;
            try {
                const data = await api(form.dataset.url, 'PUT', { quantity: quantity });
                if (data && myId === requestId) {
                    lastValid = quantity;
                    form.closest('tr').querySelector('.js-subtotal').textContent = data.itemSubtotal;
                    updateSummary(data);
                }
            } catch (err) {
                toast(err.message, true);
                input.value = lastValid;
            }
        }

        input.addEventListener('input', () => {
            clearTimeout(timer);
            timer = setTimeout(save, 500);
        });

        input.addEventListener('blur', () => {
            if (input.value === '') input.value = lastValid;
        });

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            clearTimeout(timer);
            save();
        });
    });

    // ===== Trang giỏ hàng: xóa món =====
    document.querySelectorAll('form.js-remove-form').forEach(form => {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const row = form.closest('tr');
            try {
                const data = await api(form.dataset.url, 'DELETE');
                if (data) {
                    row.remove();
                    updateSummary(data);
                    toast(data.message, false);
                    if (document.querySelectorAll('tbody tr').length === 0) {
                        window.location.reload();
                    }
                }
            } catch (err) {
                toast(err.message, true);
            }
        });
    });
})();