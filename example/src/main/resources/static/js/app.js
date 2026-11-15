/* 轻笔记前端：所有交互都打到 /api/*，页面本身服务端渲染 */

let loginMode = 'code';
let noteId = '';

function el(id) {
    return document.getElementById(id);
}

function msg(text, isError) {
    const box = el('msg');
    if (!box) return;
    box.textContent = text;
    box.classList.toggle('error', !!isError);
}

/** 统一请求：JSON 返回就解析，错误抛出去交给调用方提示 */
async function request(url, options = {}) {
    const res = await fetch(url, {credentials: 'same-origin', ...options});
    const text = await res.text();
    if (!res.ok) {
        let detail = text;
        try {
            const parsed = JSON.parse(text);
            detail = parsed.message || parsed.error || text;
        } catch (ignored) {
            // 非 JSON 错误体原样抛出
        }
        throw new Error(detail);
    }
    const type = res.headers.get('content-type') || '';
    return type.includes('json') ? JSON.parse(text) : text;
}

function postForm(url, form) {
    return request(url, {method: 'POST', body: new URLSearchParams(new FormData(form))});
}

function postJson(url, data) {
    return request(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data ?? {})
    });
}

function formOf(event) {
    event.preventDefault();
    return event.target;
}

/* ---------- 认证 ---------- */

document.addEventListener('DOMContentLoaded', () => {
    noteId = document.body.dataset.noteId || '';
    connectSse();
    // 详情页打开即记一次阅读量（PostgreSQL 计数 + Redis 最近浏览）
    if (document.body.dataset.viewOnce) {
        postJson('/api/notes/' + noteId + '/view', {});
    }

    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            loginMode = tab.dataset.tab;
            document.querySelectorAll('.tab').forEach(t => t.classList.toggle('on', t === tab));
            document.querySelectorAll('[data-pane]').forEach(p => {
                const shown = p.dataset.pane === loginMode;
                p.hidden = !shown;
                if (!shown) p.querySelectorAll('input').forEach(i => { i.value = ''; });
            });
            msg('');
            if (loginMode === 'code' && !el('captcha-uuid').value) loadCaptcha();
        });
    });
});

async function sendCode(button) {
    const form = el('login-form');
    const email = form.elements.email.value;
    if (!email) return msg('先填邮箱', true);
    if (!form.elements.captchaCode.value) return msg('先填图形验证码', true);
    try {
        await postForm('/api/auth/code', form);
        loadCaptcha();
        button.disabled = true;
        let left = 60;
        const timer = setInterval(() => {
            button.textContent = left + 's';
            if (--left < 0) {
                clearInterval(timer);
                button.disabled = false;
                button.textContent = '获取验证码';
            }
        }, 1000);
        msg('验证码已发送');
    } catch (e) {
        msg(e.message, true);
        loadCaptcha();
    }
}

async function login(event) {
    const form = formOf(event);
    const body = new FormData(form);
    if (loginMode === 'password') body.delete('code');
    if (loginMode === 'code') body.delete('password');
    try {
        const user = await request('/api/auth/login', {method: 'POST', body: new URLSearchParams(body)});
        location.href = user.role === 'ADMIN' ? '/admin' : '/notes';
    } catch (e) {
        msg(e.message, true);
    }
}

async function logout() {
    await request('/api/auth/logout', {method: 'POST'});
    location.href = '/login';
}

async function loadCaptcha() {
    const captcha = await request('/api/auth/captcha');
    el('captcha-img').src = captcha.base64;
    el('captcha-uuid').value = captcha.uuid;
}

async function register(event) {
    const form = formOf(event);
    try {
        await postForm('/api/auth/register', form);
        location.href = '/login';
    } catch (e) {
        msg(e.message, true);
        loadCaptcha();
    }
}

/* ---------- 笔记 ---------- */

async function createNote(event) {
    const form = formOf(event);
    try {
        const note = await postForm('/api/notes', form);
        location.href = '/notes/' + note.id;
    } catch (e) {
        msg(e.message, true);
    }
}

async function searchNotes(event) {
    event.preventDefault();
    const keyword = event.target.elements.keyword.value;
    location.href = '/notes?keyword=' + encodeURIComponent(keyword);
}

async function saveNote(event, id) {
    const form = formOf(event);
    try {
        await postForm('/api/notes/' + (id || noteId) + '/update', form);
        msg('已保存');
    } catch (e) {
        msg(e.message, true);
    }
}

async function deleteNote() {
    if (!confirm('删除这篇笔记？')) return;
    try {
        await request('/api/notes/' + noteId, {method: 'DELETE'});
        location.href = '/notes';
    } catch (e) {
        msg(e.message, true);
    }
}

async function uploadAttachment(event) {
    const form = formOf(event);
    try {
        await request('/api/notes/' + noteId + '/attachments', {method: 'POST', body: new FormData(form)});
        location.reload();
    } catch (e) {
        msg(e.message, true);
    }
}

async function removeAttachment(id) {
    try {
        await request('/api/attachments/' + id, {method: 'DELETE'});
        location.reload();
    } catch (e) {
        msg(e.message, true);
    }
}

/* ---------- 分享与开放接口 ---------- */

async function createShare(event) {
    const form = formOf(event);
    const data = new FormData(form);
    const body = new URLSearchParams({days: data.get('days') || '0', mail: data.get('mail') ? 'true' : 'false'});
    try {
        const share = await request('/api/notes/' + noteId + '/share',
            {method: 'POST', body});
        el('share-box').hidden = false;
        const link = el('share-url');
        link.href = share.url;
        link.textContent = share.url;
        el('share-qr').src = share.qr;
        el('share-qr').dataset.token = share.url.split('/').pop();
    } catch (e) {
        msg(e.message, true);
    }
}

/** 页面当一次第三方：带签名打开放接口，响应是密文，由服务端解密后回显 */
async function callOpenApi() {
    const token = el('share-qr').dataset.token;
    const log = el('open-api-log');
    log.textContent = '';
    try {
        const result = await request('/api/demo/open-call', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams({token})
        });
        log.textContent = JSON.stringify(result, null, 2);
    } catch (e) {
        log.textContent = e.message;
    }
}

/* ---------- 实时通道 ---------- */

function realtimeLog(text) {
    const box = el('realtime-log') || el('sse-strip');
    if (!box) return;
    box.textContent = text + '\n' + box.textContent;
}

function connectSse() {
    if (!window.EventSource) return;
    const source = new EventSource('/sse');
    source.onmessage = e => realtimeLog('SSE: ' + e.data);
    source.onerror = () => {
        source.close();
        setTimeout(connectSse, 3000);
    };
}

async function pushRealtime(channel) {
    try {
        await request('/api/realtime/' + channel, {method: 'POST'});
    } catch (e) {
        realtimeLog(e.message);
    }
}
