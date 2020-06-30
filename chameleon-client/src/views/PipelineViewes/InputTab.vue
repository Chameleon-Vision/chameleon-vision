<template>
  <div>
    <CVslider
      v-model="value.cameraExposure"
      name="Exposure"
      :min="0"
      :max="100"
      @input="handleData('cameraExposure')"
      @rollback="e => rollback('cameraExposure', e)"
    />
    <CVslider
      v-model="value.cameraBrightness"
      name="Brightness"
      :min="0"
      :max="100"
      @input="handleData('cameraBrightness')"
      @rollback="e => rollback('cameraBrightness', e)"
    />
    <CVslider
      v-if="value.cameraGain !== -1"
      v-model="value.cameraGain"
      name="Gain"
      :min="0"
      :max="100"
      @input="handleData('gain')"
      @rollback="e => rollback('gain', e)"
    />
    <CVselect
      v-model="value.inputImageRotationMode"
      name="Orientation"
      :list="['Normal','90° CW','180°','90° CCW']"
      @input="handleData('inputImageRotationMode')"
      @rollback="e => e => rollback('inputImageRotationMode',e)"
    />
    <CVselect
      v-model="value.cameraVideoModeIndex"
      name="Resolution"
      :list="resolutionList"
      @input="handleData('cameraVideoModeIndex')"
      @rollback="e => rollback('cameraVideoModeIndex', e)"
    />
    <CVselect
      v-model="value.outputFrameDivisor"
      name="Stream Resolution"
      :list="streamResolutionList"
      @input="handleData('outputFrameDivisor')"
      @rollback="e => rollback('outputFrameDivisor', e)"
    />
  </div>
</template>

<script>
    import CVslider from '../../components/common/cv-slider'
    import CVselect from '../../components/common/cv-select'

    export default {
        name: 'Input',
        components: {
            CVslider,
            CVselect,
        },
      // eslint-disable-next-line vue/require-prop-types
        props: ['value'],
        data() {
            return {}
        },
        computed: {
            resolutionList: {
                get() {
                    let tmp_list = [];
                    for (let i of this.$store.state.cameraSettings.resolutionList) {
                        tmp_list.push(`${i['width']} X ${i['height']} at ${i['fps']} FPS, ${i['pixelFormat']}`)
                    }
                    return tmp_list;
                }
            },
            streamResolutionList: {
                get() {
                    let cam_res = this.$store.state.cameraSettings.resolutionList[this.value.cameraVideoModeIndex];
                    let tmp_list = [];
                    tmp_list.push(`${Math.floor(cam_res['width'])} X ${Math.floor(cam_res['height'])}`);
                    for (let x = 2; x <= 6; x += 2) {
                        tmp_list.push(`${Math.floor(cam_res['width'] / x)} X ${Math.floor(cam_res['height'] / x)}`);
                    }
                    return tmp_list;
                }
            }
        },
        methods: {}
    }
</script>

<style scoped>

</style>