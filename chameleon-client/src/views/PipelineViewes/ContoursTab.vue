<template>
    <div>
        <CVrangeSlider v-model="value.area" name="Area" :min="0" :max="100" :step="0.1"
                       @input="handleData('area')"
                       @rollback="e=> rollback('area',e)"/>
        <CVrangeSlider v-model="value.ratio" name="Ratio (W/H)" :min="0" :max="100" :step="0.1"
                       @input="handleData('ratio')"
                       @rollback="e=> rollback('ratio',e)"/>
        <CVrangeSlider v-model="value.extent" name="Extent" :min="0" :max="100"
                       @input="handleData('extent')"
                       @rollback="e=> rollback('extent',e)"/>
        <CVslider name="Speckle Rejection" :min="0" :max="100" v-model="value.speckle"
                  @input="handleData('speckle')"
                  @rollback="e=> rollback('speckle',e)"/>
        <CVselect name="Target Group" :list="['Single','Dual']" v-model="value.targetGroup"
                  @input="handleData('targetGroup')"
                  @rollback="e=> rollback('targetGroup',e)"/>
        <CVselect name="Target Intersection" :list="['None','Up','Down','Left','Right']" :disabled="isDisabled"
                  v-model="value.targetIntersection" @input="handleData('targetIntersection')"
                  @rollback="e=> rollback('targetIntersection',e)"/>

    </div>
</template>

<script>
    import CVrangeSlider from '../../components/common/cv-range-slider'
    import CVselect from '../../components/common/cv-select'
    import CVslider from '../../components/common/cv-slider'

    export default {
        name: 'Contours',
        props: ['value'],
        components: {
            CVrangeSlider,
            CVselect,
            CVslider
        },
        methods: {},

        data() {
            return {}
        },
        computed: {
            isDisabled() {
                return this.value.targetGroup === 0;

            }
        },
    }
</script>

<style lang="" scoped>

</style>