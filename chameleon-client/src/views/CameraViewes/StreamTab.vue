<template>
    <div>
        <span>Frame rate:</span>
        <v-tabs v-model="value.streamFpsMode" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62" @change="handleData('streamFpsMode')">
            <v-tab>High</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>Low</v-tab>
        </v-tabs>
        <span>Stream Resolution:</span>
        <v-tabs v-model="value.streamDivisor" style="padding-bottom:5%" fixed-tabs background-color="#252525" dark height="48" slider-color="#4baf62" @change="handleData('streamDivisor')">
            <v-tab>Original</v-tab>
            <v-tab>High</v-tab>
            <v-tab>Medium</v-tab>
            <v-tab>Low</v-tab>
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
                this.handleInput(val, this.value[val]);
                this.$emit('update')
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