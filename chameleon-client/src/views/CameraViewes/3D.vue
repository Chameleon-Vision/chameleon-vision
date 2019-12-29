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
        <mini-map class="miniMapClass" :translation="point.pose.translation"
                  :radians="point.pose.rotation.radians" :horizontal-f-o-v="horizontalFOV"/>

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

            },
            horizontalFOV: {
                get() {
                    let index = this.$store.state.cameraSettings.resolution;
                    let FOV = this.$store.state.cameraSettings.fov;
                    let resolution = this.$store.state.resolutionList[index];
                    let diagonalView = FOV * (Math.PI / 180);
                    let diagonalAspect = Math.hypot(resolution.width, resolution.height);
                    let horizontalFOV = Math.atan(Math.tan(diagonalView / 2) * (resolution.width / diagonalAspect)) * 2 * (180/ Math.PI);
                    return horizontalFOV
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