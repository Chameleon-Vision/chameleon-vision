<template>
    <div>
        <v-row align="center" justify="start" dense>
            <v-col :cols="6">
                <CVswitch v-model="value.is3D" name="Enable 3D" @input="handleData('is3D')"/>
            </v-col>
            <v-col>
                <input type="file" ref="file" style="display: none" accept=".csv" @change="readFile">
                <v-btn @click="$refs.file.click()" small>
                    upload model
                </v-btn>
            </v-col>
        </v-row>
        <mini-map class="miniMapClass" :location="point.pose.translation"
                  :angle="(point.pose.rotation.radians * (180/Math.PI))"/>
    </div>
</template>

<script>
    import miniMap from '../../components/3D/MiniMap';
    import CVswitch from '../../components/cv-switch';
    import Papa from 'papaparse';

    export default {
        name: "solvePNP",
        props: ['value'],
        components: {
            CVswitch,
            miniMap
        },
        data() {
            return {
                is3D: false,
                isPNPCalibration: false,
            }
        },
        methods: {
            handleData(val) {
                this.handleInput(val, this.value[val]);
                this.$emit('update')
            },
            readFile(event) {
                let file = event.target.files[0];
                Papa.parse(file, {
                    complete: this.onParse,
                    skipEmptyLines: true
                });
            },
            onParse(result) {
                // console.log(result.data);
                this.axios.post("http://" + this.$address + "/api/vision/pnpModel", result.data);
            },
        },
        computed: {
            point: {
                get() {
                    return this.$store.state.point.calculated;
                }
            }
        }
    }
</script>

<style scoped>
    .miniMapClass {
        width: 50% !important;
        height: 50% !important;
    }
</style>