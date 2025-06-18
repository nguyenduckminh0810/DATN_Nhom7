import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Import Bootstrap CSS và JS
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import BootstrapVueNext from 'bootstrap-vue-next'
const app = createApp(App)

app.use(createPinia())
app.use(BootstrapVueNext)
app.use(router)

app.mount('#app')
