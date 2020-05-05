export const handleInputMixin = {
    methods:{
        handleInput(key, value) {
            let msg = this.$msgPack.encode({[key]: value});
            this.$socket.send(msg);
        }
    }
}