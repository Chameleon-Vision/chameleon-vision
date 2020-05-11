<template>
    <!--        TODO FIX IMAGE SIZING NOT WORKING CORRECTLY -->
    <img id="CameraStream" style="display: block;margin: auto; width:auto; height: auto"
         :src="address" @click="e => $emit('click', e)"
         crossorigin="Anonymous" alt=""/>
</template>

<script>
    export default {
        name: "cv-image",
        props: ['address']
    }
</script>