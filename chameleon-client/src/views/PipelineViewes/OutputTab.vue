<template>
    <div>
        <CVselect name="Sort Mode" v-model="value.sortMode"
                  :list="['Largest','Smallest','Highest','Lowest','Rightmost','Leftmost','Centermost']"
                  @input="handleData('sortMode')"
                  @rollback="rollback('sortMode',e)"/>

        <CVselect v-model="value.targetRegion" name="Target Region" :list="['Center','Top','Bottom','Left','Right']"
                  @input="handleData('targetRegion')"
                  @rollback="e=> rollback('targetRegion',e)"/>

        <CVselect name="Target Orientation" :list="['Portrait', 'Landscape']" v-model="value.targetOrientation"
                  @input="handleData('targetOrientation')"
                  @rollback="e=> rollback('targetOrientation',e)"/>

        <CVswitch name="Output multiple" v-model="value.multiple" @input="handleData('multiple')"
                  @rollback="e=> rollback('multiple',e)"/>
        <span>Calibrate:</span>
        <v-divider dark color="white"/>
        <CVselect name="Calibration Mode" v-model="value.calibrationMode" :list="['None','Single point','Dual point']"
                  @input="handleData('calibrationMode')"
                  @rollback="e=> rollback('calibrationMode',e)"/>
        <component :raw-point="rawPoint" :is="selectedComponent" @update="doUpdate" @snackbar="showSnackbar"/>
        <v-snackbar :timeout="3000" v-model="snackbar" top color="error">
            <span style="color:#000">{{snackbarText}}</span>
            <v-btn color="black" text @click="snackbar = false">Close</v-btn>
        </v-snackbar>
    </div>
</template>

<script>
    import CVselect from '../../components/common/cv-select'
    import CVswitch from '../../components/common/cv-switch'
    import DualCalibration from "../../components/pipeline/OutputTab/DualCalibration";
    import SingleCalibration from "../../components/pipeline/OutputTab/SingleCalibration";


    export default {
        name: 'Output',
        props: ['value'],
        components: {
            CVselect,
            CVswitch,
            SingleCalibration,
            DualCalibration,

        },
        methods: {
            doUpdate() {
                this.$emit('update')
            },
            showSnackbar(message) {
                this.snackbarText = message;
                this.snackbar = true;
            },
        },

        data() {
            return {
                snackbar: false,
                snackbarText: ""
            }
        },
        computed: {
            selectedComponent: {
                get() {
                    switch (this.value.calibrationMode) {
                        case 0:
                            return "";
                        case 1:
                            return "SingleCalibration";
                        case 2:
                            return "DualCalibration"
                    }
                    return ""
                }
            },
            rawPoint: {
                get() {
                    return this.$store.state.point.rawPoint;
                }
            }
        }
    }
</script>

<style scoped>
</style>