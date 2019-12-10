<template>
    <div>
        <span>Compression:</span>
        <v-tabs v-model="value.streamCompression" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62">
            <v-tab>None</v-tab>
            <v-tab>Low</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>High</v-tab>
        </v-tabs>
        <span>Frame rate:</span>
        <v-tabs v-model="value.streamFpsMode" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62">
            <v-tab>Low</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>High</v-tab>
        </v-tabs>
        <span>Stream Resolution:</span>
        <v-tabs v-model="value.streamDivisor" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62" @change="handleData('streamDivisor')">
            <v-tab>Low</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>High</v-tab>
            <v-tab>Original</v-tab>
        </v-tabs>
    </div>
</template>

<script>


    export default {
        name: 'Stream',
        props: ['value'],
        components: {
        },
        methods: {
            handleData(val) {
                let a = this.value[val];
                this.handleInput(val, this.value[val]);
                // this.$emit('update')
            },

        },

        data() {
            return {};
        },
        computed: {
            selectedComponent: {
                get(){return "";}
                }
            },
        
    }
</script>

<style scoped>
</style>