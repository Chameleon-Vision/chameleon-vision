<template>
    <div>
        <CVslider name="Exposure" v-model="value.exposure" :min="0" :max="100" @input="handleData('exposure')"
                  @rollback="e => rollback('exposure', e)"/>
        <CVslider name="Brightness" v-model="value.brightness" :min="0" :max="100" @input="handleData('brightness')"
                  @rollback="e => rollback('brightness', e)"/>
        <CVslider name="Gain" v-if="value.gain !== -1" v-model="value.gain" :min="0" :max="100"
                  @input="handleData('gain')"
                  @rollback="e => rollback('gain', e)"/>
        <CVselect name="Orientation" v-model="value.rotationMode" :list="['Normal','90° CW','180°','90° CCW']"
                  @input="handleData('rotationMode')"
                  @rollback="e => e => rollback('rotationMode',e)"/>
        <CVselect name="Resolution" v-model="value.videoModeIndex" :list="resolutionList"
                  @input="handleData('videoModeIndex')"
                  @rollback="e => rollback('videoModeIndex', e)"/>
        <CVselect name="Stream Resolution" v-model="value.streamDivisor"
                  :list="streamResolutionList" @input="handleData('streamDivisor')"
                  @rollback="e => rollback('streamDivisor', e)"/>
    </div>
</template>

<script>
    import CVslider from '../../components/common/cv-slider'
    import CVselect from '../../components/common/cv-select'

    export default {
        name: 'Input',
        props: ['value'],
        components: {
            CVslider,
            CVselect,
        },
        methods: {},
        data() {
            return {}
        },
        computed: {
            resolutionList: {
                get() {
                    let tmp_list = [];
                    for (let i of this.$store.state.resolutionList) {
                        tmp_list.push(`${i['width']} X ${i['height']} at ${i['fps']} FPS, ${i['pixelFormat']}`)
                    }
                    return tmp_list;
                }
            },
            streamResolutionList: {
                get() {
                    let cam_res = this.$store.state.resolutionList[this.value.videoModeIndex];
                    let tmp_list = [];
                    tmp_list.push(`${Math.floor(cam_res['width'])} X ${Math.floor(cam_res['height'])}`);
                    for (let x = 2; x <= 6; x += 2) {
                        tmp_list.push(`${Math.floor(cam_res['width'] / x)} X ${Math.floor(cam_res['height'] / x)}`);
                    }
                    return tmp_list;
                }
            }
        }
    }
</script>

<style scoped>

</style>