<template>
        <img id="CameraStream" style="display: block;margin: auto; width:auto; height: auto"
             :src="address" @click="e => $emit('click', e)"
             crossorigin="Anonymous" :alt="require('../../assets/chessboard.png')"/>
</template>

<script>
    export default {
        name: "cv-image",
        props: ['address']
    }
</script>

<style scoped>

</style>