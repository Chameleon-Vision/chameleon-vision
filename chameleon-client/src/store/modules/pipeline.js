import Vue from 'vue'

export default {
    state: {
        outputShowThresholded: 0,

        cameraExposure: 0,
        cameraBrightness: 0,
        cameraGain: 0,
        inputImageRotationMode: 0,
        cameraVideoModeIndex: 0,
        outputFrameDivisor: 0,

        hsvHue: [0, 15],
        hsvSaturation: [0, 15],
        hsvValue: [0, 25],
        erode: false,
        dilate: false,

        contourArea: [0, 12],
        contourRatio: [0, 12],
        contourExtent: [0, 12],
        contourSpecklePercentage: 5,
        contourGroupingMode: 0,
        contourIntersection: 0,

        contourSortMode: 0,
        contourTargetOrientation: 1,
        outputShowMultipleTargets: false,
        offsetRobotOffsetMode: 0,
        contourTargetOffsetPointEdge: 0,

        solvePNPEnabled: false,
        cornerDetectionAccuracyPercentage: 10
    },
    mutations: {
        isBinary: (state, value) => {
            state.outputShowThresholded = value
        },
        mutatePipeline: (state, {key, value}) => {
            Vue.set(state, key, value)
        }

    },
    actions: {},
    getters: {
        pipeline: state => {
            return state
        }
    }
};