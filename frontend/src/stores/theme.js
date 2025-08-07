import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // State
  const isDarkMode = ref(false)

  // Actions
  const toggleTheme = () => {
    isDarkMode.value = !isDarkMode.value
    localStorage.setItem('app-dark-mode', isDarkMode.value)
    applyTheme()
  }

  const setTheme = (dark) => {
    isDarkMode.value = dark
    localStorage.setItem('app-dark-mode', isDarkMode.value)
    applyTheme()
  }

  const initTheme = () => {
    // Check localStorage first
    const savedTheme = localStorage.getItem('app-dark-mode')
    if (savedTheme !== null) {
      isDarkMode.value = JSON.parse(savedTheme)
    } else {
      // Check system preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      isDarkMode.value = prefersDark
    }
    applyTheme()
  }

  const applyTheme = () => {
    const root = document.documentElement
    if (isDarkMode.value) {
      root.classList.add('dark-theme')
      root.setAttribute('data-theme', 'dark')
    } else {
      root.classList.remove('dark-theme')
      root.setAttribute('data-theme', 'light')
    }
  }

  // Watch for system theme changes
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', (e) => {
    // Only auto-switch if user hasn't manually set a preference
    if (localStorage.getItem('app-dark-mode') === null) {
      isDarkMode.value = e.matches
      applyTheme()
    }
  })

  return {
    isDarkMode,
    toggleTheme,
    setTheme,
    initTheme,
    applyTheme
  }
}) 