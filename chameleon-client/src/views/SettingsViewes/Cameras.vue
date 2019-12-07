<template>
    <div>
        <div>
            <CVselect name="Camera" :list="cameraList" v-model="currentCameraIndex"/>
            <CVnumberinput name="Diagonal FOV" v-model="cameraSettings.fov"/>
            <v-btn style="margin-top:10px" small color="#4baf62" @click="sendCameraSettings">Save Camera Settings
            </v-btn>
        </div>
        <div style="margin-top: 15px">
            <span>3D Calibration</span>
            <v-divider color="white" style="margin-bottom: 10px"></v-divider>
            <v-row>
                <v-col>
                    <v-btn small :color="calibrationModeButton.color" @click="sendCalibrationMode">{{calibrationModeButton.text}}</v-btn>
                </v-col>
                <v-col>
                    <v-btn small :color="cancellationModeButton.color" @click="sendCalibrationFinish">{{cancellationModeButton.text}}</v-btn>
                </v-col>
            </v-row>
        </div>
    </div>
</template>

<script>
    import CVselect from '../../components/cv-select'
    import CVnumberinput from '../../components/cv-number-input'

    export default {
        name: 'CameraSettings',
        components: {
            CVselect,
            CVnumberinput
        },
        data() {
            return {
                calibrationMode: false,
                calibrationModeButton: {
                    text: "Start Calibration",
                    color: "green"
                },
                cancellationModeButton: {
                    text: "Cancel Calibration",
                    color: "red"
                },
                snapshotAmount: 0
            }
        },
        methods: {
            sendCameraSettings() {
                const self = this;
                this.axios.post("http://" + this.$address + "/api/settings/camera", this.cameraSettings).then(
                    function (response) {
                        if (response.status === 200) {
                            self.$store.state.saveBar = true;
                        }
                    }
                )
            },
            sendCalibrationMode() {
                const self = this;
                let connection_string = "/api/settings/";
                if (this.calibrationMode === true) {
                    connection_string += "snapshot"
                } else {
                    connection_string += "startCalibration"
                }
                this.axios.post("http://" + this.$address + connection_string).then(
                    function (response) {
                        if (response.status === 200) {
                            if (self.calibrationMode) {
                                self.snapshotAmount = response.data;
                                if (self.snapshotAmount > 12) {
                                    self.cancellationModeButton.text = "Finish Calibration";
                                    self.cancellationModeButton.color = "green";
                                }
                            } else {
                                self.calibrationModeButton.text = "Take Snapshot";
                                self.calibrationMode = true;
                            }
                        }
                    }
                );
            },
            sendCalibrationFinish() {
                const self = this;
                let connection_string = "/api/settings/";
                if (this.snapshotAmount > 12) {
                    connection_string += "finishCalibration"
                } else {
                    connection_string += "cancelCalibration"
                }
                this.axios.post("http://" + this.$address + connection_string).then(
                    function (response) {
                        if (response.status === 200) {
                            self.calibrationMode = false;
                            self.snapshotAmount = 0;
                            self.calibrationModeButton.text = "Start Calibration";
                            self.cancellationModeButton.text = "Cancel Calibration";
                            self.cancellationModeButton.color = "red";
                        }
                    }
                );
            }
        },
        computed: {
            currentCameraIndex: {
                get() {
                    return this.$store.state.currentCameraIndex;
                },
                set(value) {
                    this.$store.commit('currentCameraIndex', value);
                }
            },
            cameraList: {
                get() {
                    return this.$store.state.cameraList;
                },
                set(value) {
                    this.$store.commit('cameraList', value);
                }
            },
            cameraSettings: {
                get() {
                    return this.$store.state.cameraSettings;
                },
                set(value) {
                    this.$store.commit('cameraSettings', value);
                }
            },

        }
    }
</script>

<style lang="" scoped>

</style>