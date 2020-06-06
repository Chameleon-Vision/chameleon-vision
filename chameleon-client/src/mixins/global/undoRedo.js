import { cloneDeep } from "lodash"
export const undoRedoMixin = {
    data() {
        return {
            done: [],
            undone: [],
            newMutation: true,
        };
    },
    computed: {
        canRedo() {
            return this.undone.length;
        },
        canUndo() {
            return this.done.length;
        }
    },
    methods: {
        updateList(val) {

            this.done.push(cloneDeep(val));
            if (this.newMutation) {
                this.undone = []
            }
        },
        redo() {
            let commit = this.undone.pop();
            this.newMutation = false;
            this.$store.commit('pipeline', commit)
            this.newMutation = true;
        },
        undo() {
            let commit = this.done.pop();
            this.undone.push(commit);
            this.newMutation = false;
            
            // this.done.forEach(mutation => {
            //     switch (typeof mutation.payload) {
            //         case 'object':
            //             this.$store.commit(`${mutation.type}`, Object.assign({}, mutation.payload));
            //             break;
            //         default:
            //             this.$store.commit(`${mutation.type}`, mutation.payload);
            //     }
            // this.done.pop();
            // });
            this.$store.commit('pipeline', commit);
            this.newMutation = true;
        }
    }
}
