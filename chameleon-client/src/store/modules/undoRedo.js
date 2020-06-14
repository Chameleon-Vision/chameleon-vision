export default {
    state: {
        done: [],
        undone: [],
        newMutation: true
    },
    mutations: {
        updatePipeline: (state, val) => {
            state.done.push(val)
            if (state.newMutation) {
                state.undone = []
            }
        },
        addUndone: (state, val) => {
            state.undone.push(val);
        },
        removeLastDone: state => {
            state.done.pop()
        },
        removeLastUnDone: state => {
            state.undone.pop()
        },
        updateStatus: (state, bool) => {
            state.newMutation = bool;
        },
    },
    actions: {
        undo: (context) => {
            let commit = context.getters.lastDone;
            context.commit('removeLastDone')
            context.commit('addUndone', commit);
            context.commit('updateStatus', false)
            for (let key in commit) {
                if (commit.hasOwnProperty(key)) {
                    context.commit('mutatePipeline', {'key': key, 'value': commit[key]});
                }
            }
            context.commit('updateStatus', true)
        },
        redo: (context) => {
            let commit = context.getters.lastUnDone;
            context.commit('removeLastUnDone');
            context.commit('updateStatus', false)
            for (let key in commit) {
                if (commit.hasOwnProperty(key)) {
                    context.commit('mutatePipeline', {'key': key, 'value': commit[key]});
                }
            }
            context.commit('updateStatus', true)
        }
    },
    getters: {
        lastDone: state => {
            return state.done.pop()
        },
        lastUnDone: state => {
            return state.undone.pop()
        },
        canUndo: state => {
            return state.done.length
        },
        canRedo: state => {
            return state.undone.length
        }
    }
};