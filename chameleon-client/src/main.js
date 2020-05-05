import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify';
import msgPack from 'msgpack5';
import axios from 'axios';
import VueAxios from "vue-axios";

Vue.config.productionTip = false;

if (process.env.NODE_ENV === "production") {
    Vue.prototype.$address = location.host;
} else if (process.env.NODE_ENV === "development") {
    Vue.prototype.$address = location.hostname + ":5800";
}

const wsURL = 'ws://' + Vue.prototype.$address + '/websocket';

const ws = new WebSocket(wsURL);
ws.binaryType = "arraybuffer";

import VueNativeSock from 'vue-native-websocket';
Vue.use(VueNativeSock, wsURL, {
    WebSocket: ws
});
Vue.use(VueAxios, axios);
Vue.prototype.$msgPack = msgPack(true);

import {handleInputMixin} from './mixins/global/handleInput'

Vue.mixin(handleInputMixin);
new Vue({
    router,
    store,
    vuetify,
    render: h => h(App)
}).$mount('#app');
