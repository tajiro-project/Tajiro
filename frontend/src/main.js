import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import './assets/base.css';
import 'simplebar-vue/dist/simplebar.min.css'; // Simplebar 기본 CSS 추가

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
