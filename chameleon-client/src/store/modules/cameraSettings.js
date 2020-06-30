export default {
    state: {
        calibration: [],
        resolutionList: [],
        port: 1181,
        fov: 0,
        resolution: 0,
        streamDivisor: 0,
        tilt: 0
    },
    getters: {
        cameraSettings: state => {
            return state
        },
        streamAddress: state => {
            return "http://" + location.hostname + ":" + state.port + "/stream.mjpg";
        },
    }
}