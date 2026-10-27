(function () {
    'use strict';

    // Đánh dấu JavaScript đang chạy, để CSS ẩn nút "Cập nhật"
    document.documentElement.classList.add('js');

    // Spring Security yêu cầu token CSRF trong mọi request POST/PUT/DELETE
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const headerName = document.querySelector('meta[name="_csrf_header"]')?.content;

    function toast(message, isError) {
        const el = document.createElement('div');
        el.className = 'js-toast' + (isError ? ' error' : '');
        el.textContent = message;               // textContent: không chèn HTML, tránh lỗ hổng XSS
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

        if (res.redirected) {                   // chưa đăng nhập hoặc phiên hết hạn: về trang đăng nhập
            window.location.href = res.url;
            return null;
        }
        const isJson = (res.headers.get('content-type') || '').includes('application/json');
        const data = isJson ? await res.json() : {};
        if (!res.ok) {
            throw new Error(data.message || 'Có lỗi xảy ra, vui lòng thử lại');
        }
        return data;
    }

    function updateSummary(data) {
        document.querySelectorAll('[data-cart-count]').forEach(el => {
            el.textContent = data.cartCount + ' món';
        });
        document.querySelectorAll('[data-cart-total]').forEach(el => {
            el.textContent = data.cartTotal;
        });
    }

    // ===== Trang menu: thêm món vào giỏ =====
    document.querySelectorAll('form.js-add-to-cart').forEach(form => {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();                 // chặn việc gửi form kiểu cũ (tải lại trang)
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
        let lastValid = Number(input.value);    // số lượng hợp lệ gần nhất, dùng để khôi phục khi lỗi
        let timer = null;
        let requestId = 0;

        async function save() {
            if (input.value === '') return;     // đang xóa để gõ số mới: chưa gửi
            const quantity = Number(input.value);
            if (!Number.isInteger(quantity) || quantity < 1 || quantity > 20) {
                toast('Số lượng phải từ 1 đến 20', true);
                input.value = lastValid;
                return;
            }
            if (quantity === lastValid) return; // không đổi thì khỏi gửi

            const myId = ++requestId;
            try {
                const data = await api(form.dataset.url, 'PUT', { quantity: quantity });
                if (data && myId === requestId) {   // bỏ qua phản hồi cũ nếu đã có lần gửi mới hơn
                    lastValid = quantity;
                    form.closest('tr').querySelector('.js-subtotal').textContent = data.itemSubtotal;
                    updateSummary(data);
                }
            } catch (err) {
                toast(err.message, true);
                input.value = lastValid;
            }
        }

        // Mỗi lần số đổi (bấm mũi tên hoặc gõ), đợi 0,5 giây không đổi nữa mới gửi
        input.addEventListener('input', () => {
            clearTimeout(timer);
            timer = setTimeout(save, 500);
        });

        // Rời khỏi ô mà còn trống thì trả về số cũ
        input.addEventListener('blur', () => {
            if (input.value === '') input.value = lastValid;
        });

        // Nhấn Enter cũng gửi ngay
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
                        window.location.reload();   // hết món: tải lại để hiện "Giỏ hàng trống"
                    }
                }
            } catch (err) {
                toast(err.message, true);
            }
        });
    });
})();