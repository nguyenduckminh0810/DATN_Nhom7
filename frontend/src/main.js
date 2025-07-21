import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import { createPinia } from 'pinia'

import 'bootstrap/dist/css/bootstrap.min.css'
<<<<<<< HEAD
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
=======
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap'
>>>>>>> 49598303905d5152a96eeba8beaad1a5b8511c4c

const app = createApp(App)
app.use(createPinia())
app.use(router)

app.mount('#app')
