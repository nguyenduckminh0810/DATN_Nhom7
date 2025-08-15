// ✅ POLYFILL CHO SOCKJS-CLIENT
// Fix lỗi "global is not defined" trong browser environment

if (typeof global === 'undefined') {
    window.global = window;
}

if (typeof process === 'undefined') {
    window.process = { env: {} };
}

// ✅ POLYFILL CHO BUFFER (nếu cần)
if (typeof Buffer === 'undefined') {
    window.Buffer = {
        from: function(data) {
            return new Uint8Array(data);
        }
    };
}

console.log('✅ Polyfills loaded for sockjs-client');
