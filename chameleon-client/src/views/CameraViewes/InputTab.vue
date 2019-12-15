<template>
    <div>
        <CVslider name="Exposure" v-model="value.exposure" :min="0" :max="100" @input="handleData('exposure')"/>
        <CVslider name="Brightness" v-model="value.brightness" :min="0" :max="100" @input="handleData('brightness')"/>
        <CVselect name="Orientation" v-model="value.rotationMode" :list="['Normal','90° CW','180°','90° CCW']"
                  @input="handleData('rotationMode')"/>
        <CVselect name="Resolution" v-model="value.videoModeIndex" :list="resolutionList" @input="handleData('videoModeIndex')"/>        
        <span>Stream Resolution:</span>
        <v-tabs v-model="value.streamDivisor" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62" @change="handleData('streamDivisor')">
            <v-tab>Original</v-tab>
            <v-tab>High</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>Low</v-tab>
        </v-tabs>
        <span>Stream Frame Rate:</span>
        <v-tabs v-model="value.streamFpsMode" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62" @change="handleData('streamFpsMode')">
            <v-tab>30</v-tab>
            <v-tab>20</v-tab>
            <v-tab>10</v-tab>
        </v-tabs>
    </div>
</template>

<script>
    import CVslider from '../../components/cv-slider'
    import CVselect from '../../components/cv-select'

    export default {
        name: 'Input',
        props: ['value'],
        components: {
            CVslider,
            CVselect,
        },
        methods: {
            handleData(val) {
                this.handleInput(val, this.value[val]);
                this.$emit('update')
            }
        },
        data() {
            return {
                t: 0,
                a: 1
            }
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
                    for (let x = 2; x <= 6; x+=2) {
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