<template>
    <div>
        <v-row align="center" justify="start" dense>
            <v-col :cols="6">
                <CVswitch v-model="value" name="Enable 3D" @input="handleData('is3D')"/>
            </v-col>
            <v-col>
                <input type="file" ref="file" style="display: none" accept=".csv" @change="readFile">
                <v-btn @click="$refs.file.click()" small>
                    upload model
                </v-btn>
            </v-col>
            <v-col :cols="6">
                <CVswitch v-model="value" name="Enable 3D Calibration" @input="handleData('isPNPCalibration')"/>
            </v-col>
            <v-btn style="margin: 20px;" tile color="#4baf62" @click="handleSnapshot()" small>
                <v-icon>camera</v-icon>
                Take Calibration
            </v-btn>
        </v-row>
        <mini-map class="miniMapClass" :location="lData"></mini-map>
    </div>
</template>

<script>
    import miniMap from '../../components/3D/MiniMap';
    import CVswitch from '../../components/cv-switch';
    import Papa from 'papaparse';

    export default {
        name: "solvePNP",
        components: {
            CVswitch,
            miniMap
        },
        data() {
            return {
                is3D: false,
                isPNPCalibration: false,
                lData: {
                    x: 0,
                    y: 0,
                    a: 0
                }
            }
        },
        methods: {
            handleData(val) {
                console.log("setting " + val + " to " + this.value)
                this.handleInput(val, this.value);
                this.$emit('update')
            },
            readFile(event) {
                let file = event.target.files[0];
                Papa.parse(file, {
                    complete: this.onParse,
                    skipEmptyLines: true
                });
            },
            handleSnapshot() {
                let msg = this.$msgPack.encode({
                    'takeCalibrationSnapsnot': true
                });
                this.$socket.send(msg);
                this.$emit('update');
            },
            onParse(result) {
                console.log(result.data);
            },
        },
    }
</script>

<style scoped>
    .miniMapClass {
        width: 50% !important;
        height: 50% !important;
    }
</style>