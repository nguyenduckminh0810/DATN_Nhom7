/**
 *  useToast Composable
 * Thay thế cho toast libraries, sử dụng console.log tạm thời
 */
export function useToast() {

  const showToast = (message, type = 'info') => {
    // Tạm thời dùng console.log
    const emoji = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    }

    console.log(`${emoji[type] || 'ℹ️'} TOAST [${type.toUpperCase()}]: ${message}`)

    // TODO: Có thể thay bằng toast library thực tế sau
    // như vue-toastification hoặc tự implement
  }

  const success = (message) => showToast(message, 'success')
  const error = (message) => showToast(message, 'error')
  const warning = (message) => showToast(message, 'warning')
  const info = (message) => showToast(message, 'info')

  return {
    showToast,
    success,
    error,
    warning,
    info
  }
}