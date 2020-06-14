export const dataHandleMixin = {
    methods: {
        handleInput(key, value) {
            let msg = this.$msgPack.encode({[key]: value});
            this.$socket.send(msg);
        },
        handleData(val) {
            this.handleInput(val, this.value[val]);
            this.$emit('update')
        },
        rollback(val) {
            console.log(`rollback on ${val}`)
            this.$store.commit('updatePipeline', {[val]: this.$store.getters.pipeline[val]})
        }
    }
};
