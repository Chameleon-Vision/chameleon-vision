import Vue from 'vue'
import Vuex from 'vuex'

import pipeline from "./modules/pipeline";
import generalSettings from "./modules/generalSettings";
import cameraSettings from "./modules/cameraSettings";

Vue.use(Vuex);

const set = key => (state, val) => {
    Vue.set(state, key, val);
};

export default new Vuex.Store({
    modules: {
        pipeline: pipeline,
        settings: generalSettings,
        cameraSettings: cameraSettings
    },
    state: {
        resolutionList: [],
        port: 1181,
        currentCameraIndex: 0,
        currentPipelineIndex: 0,
        cameraList: [],
        pipelineList: [],
        point: {},
        saveBar: false
    },
    mutations: {
        settings: set('settings'),
        pipeline: set('pipeline'),
        cameraSettings: set('cameraSettings'),
        resolutionList: set('resolutionList'),
        port: set('port'),
        currentCameraIndex: set('currentCameraIndex'),
        currentPipelineIndex: set('currentPipelineIndex'),
        cameraList: set('cameraList'),
        pipelineList: set('pipelineList'),
        point: set('point'),
        setPipeValues(state, obj) {
            for (let i in obj) {
                if (obj.hasOwnProperty(i)) {
                    Vue.set(state.pipeline, i, obj[i]);
                }
            }
        },
        driverMode: set('driverMode'),
        saveBar: set("saveBar")
    },
    getters: {
        streamAddress: state => {
            return "http://" + location.hostname + ":" + state.port + "/stream.mjpg";
        }
    }
})
