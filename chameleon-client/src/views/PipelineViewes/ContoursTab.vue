<template>
  <div>
    <CVrangeSlider
      v-model="value.contourArea"
      name="Area"
      :min="0"
      :max="100"
      :step="0.1"
      @input="handleData('contourArea')"
      @rollback="e=> rollback('contourArea',e)"
    />
    <CVrangeSlider
      v-model="value.contourRatio"
      name="Ratio (W/H)"
      :min="0"
      :max="100"
      :step="0.1"
      @input="handleData('contourRatio')"
      @rollback="e=> rollback('contourRatio',e)"
    />
    <CVrangeSlider
      v-model="value.contourExtent"
      name="Extent"
      :min="0"
      :max="100"
      @input="handleData('contourExtent')"
      @rollback="e=> rollback('contourExtent',e)"
    />
    <CVslider
      v-model="value.contourSpecklePercentage"
      name="Speckle Rejection"
      :min="0"
      :max="100"
      @input="handleData('contourSpecklePercentage')"
      @rollback="e=> rollback('contourSpecklePercentage',e)"
    />
    <CVselect
      v-model="value.contourGroupingMode"
      name="Target Group"
      :list="['Single','Dual']"
      @input="handleData('contourGroupingMode')"
      @rollback="e=> rollback('contourGroupingMode',e)"
    />
    <CVselect
      v-model="value.contourIntersection"
      name="Target Intersection"
      :list="['None','Up','Down','Left','Right']"
      :disabled="isDisabled"
      @input="handleData('contourIntersection')"
      @rollback="e=> rollback('contourIntersection',e)"
    />
  </div>
</template>

<script>
    import CVrangeSlider from '../../components/common/cv-range-slider'
    import CVselect from '../../components/common/cv-select'
    import CVslider from '../../components/common/cv-slider'

    export default {
        name: 'Contours',
        components: {
            CVrangeSlider,
            CVselect,
            CVslider
        },
        props: ['value'],

        data() {
            return {}
        },
        computed: {
            isDisabled() {
                return this.value.targetGroup === 0;

            }
        },
        methods: {},
    }
</script>

<style lang="" scoped>

</style>