<template>
  <div>
    <CVselect
      v-model="value.contourSortMode"
      name="Sort Mode"
      :list="['Largest','Smallest','Highest','Lowest','Rightmost','Leftmost','Centermost']"
      @input="handleData('contourSortMode')"
      @rollback="rollback('contourSortMode',e)"
    />

    <CVselect
      v-model="value.contourTargetOffsetPointEdge"
      name="Target Region"
      :list="['Center','Top','Bottom','Left','Right']"
      @input="handleData('contourTargetOffsetPointEdge')"
      @rollback="e=> rollback('contourTargetOffsetPointEdge',e)"
    />

    <CVselect
      v-model="value.contourTargetOrientation"
      name="Target Orientation"
      :list="['Portrait', 'Landscape']"
      @input="handleData('contourTargetOrientation')"
      @rollback="e=> rollback('contourTargetOrientation',e)"
    />

    <CVswitch
      v-model="value.outputMultiple"
      name="Output multiple"
      @input="handleData('outputMultiple')"
      @rollback="e=> rollback('outputMultiple',e)"
    />
    <span>Calibrate:</span>
    <v-divider
      dark
      color="white"
    />
    <CVselect
      v-model="value.offsetRobotOffsetMode"
      name="Calibration Mode"
      :list="['None','Single point','Dual point']"
      @input="handleData('offsetRobotOffsetMode')"
      @rollback="e=> rollback('offsetRobotOffsetMode',e)"
    />
    <component
      :is="selectedComponent"
      :raw-point="rawPoint"
      @update="doUpdate"
      @snackbar="showSnackbar"
    />
    <v-snackbar
      v-model="snackbar"
      :timeout="3000"
      top
      color="error"
    >
      <span style="color:#000">{{ snackbarText }}</span>
      <v-btn
        color="black"
        text
        @click="snackbar = false"
      >
        Close
      </v-btn>
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
        components: {
            CVselect,
            CVswitch,
            SingleCalibration,
            DualCalibration,

        },
        props: ['value'],

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
        },
        methods: {
            doUpdate() {
                this.$emit('update')
            },
            showSnackbar(message) {
                this.snackbarText = message;
                this.snackbar = true;
            },
        }
    }
</script>

<style scoped>
</style>