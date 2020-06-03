<template>
    <v-app>
        <v-app-bar app dense clipped-left dark>
            <img class="imgClass" src="./assets/logo.png">
            <v-toolbar-title id="title">Chameleon Vision</v-toolbar-title>
            <div class="flex-grow-1"></div>
            <v-toolbar-items>
                <v-tabs background-color="#272727" dark height="48" slider-color="#4baf62">
                    <v-tab to="vision">Vision</v-tab>
                    <v-tab to="settings">Settings</v-tab>
                </v-tabs>
            </v-toolbar-items>
        </v-app-bar>
        <v-content>
            <v-container fluid fill-height>
                <v-layout>
                    <v-flex>
                        <router-view @save="startTimer"/>
                        <button @click="logMessage({message:Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 5), level:Math.floor(Math.random() *4)  })">click</button>
                        <v-snackbar :timeout="1000" v-model="saveSnackbar" top color="#4baf62">
                            <div style="text-align: center;width: 100%;">
                                <h4>Saved All changes</h4>
                            </div>
                        </v-snackbar>
                        <div v-if="isLogger">
                            <keep-alive>
                                <log-view class="loggerClass" :log="log"></log-view>
                            </keep-alive>
                        </div>
                    </v-flex>
                </v-layout>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    import logView from '@femessage/log-viewer'

    export default {
        name: 'App',
        components: {
            logView
        },
        methods: {
            handleMessage(key, value) {
                if (this.$store.state.hasOwnProperty(key)) {
                    this.$store.commit(key, value);
                } else if (this.$store.state.pipeline.hasOwnProperty(key)) {
                    this.$store.commit('mutatePipeline', {'key': key, 'value': value});
                } else {
                    switch (key) {
                        default: {
                            console.log(key + " : " + value);
                        }
                    }
                }
            },
            saveSettings() {
                clearInterval(this.timer);
                this.saveSnackbar = true;
                this.handleInput("command", "save");
            },
            startTimer() {
                if (this.timer !== undefined) {
                    clearInterval(this.timer);
                }
                this.timer = setInterval(this.saveSettings, 4000);
            },
            logMessage({message, level}) {
                const colors = ["\u001b[31m", "\u001b[32m", "\u001b[33m", "\u001b[34m"]
                const reset = "\u001b[0m"
                this.log += `${colors[level]}${message}${reset}\n`
            }
        },
        data: () => ({
            timer: undefined,
            isLogger: true,
            log: ""
        }),
        created() {
            document.addEventListener("keydown", e => {
                if (e.key === "`") {
                    this.isLogger = !this.isLogger;
                    console.log(this.isLogger)
                } else if (e.key === "z" && e.ctrlKey) {
                    console.log("undo")
                }
            });
            this.$options.sockets.onmessage = (data) => {
                try {
                    let message = this.$msgPack.decode(data.data);
                    for (let prop in message) {
                        if (message.hasOwnProperty(prop)) {
                            this.handleMessage(prop, message[prop]);
                        }
                    }
                } catch (error) {
                    console.error('error: ' + data.data + " , " + error);
                }
            }
        },
        computed: {
            saveSnackbar: {
                get() {
                    return this.$store.state.saveBar;
                },
                set(value) {
                    this.$store.commit("saveBar", value);
                }
            }
        }
    };
</script>

<style>
    html {
        overflow-y: hidden !important;
    }

    .imgClass {
        width: auto;
        height: 45px;
        vertical-align: middle;
        padding-right: 5px;
    }

    .loggerClass {
        position: absolute;
        bottom: 0;
        height: 25% !important;
        left: 0;
        right: 0;
        background-color: #2b2b2b;
    }
    /*TODO SCROLLBAR CLASS and npm update*/
    .container {
        background-color: #212121;
        padding: 0 !important;
    }

    #title {
        color: #4baf62;
    }

    span {
        color: white;
    }
</style>