<template>
  <div>
    <b-form-input v-model="newQuery"
                  autocomplete="off"
                  type="text"
                  size="sm"
                  placeholder="Enter a new query">
    </b-form-input>
    <div class="query-editor-panel">
      <div v-for="q in queryEntries">
        <b-form-input v-model="q.text" type="text" size="sm" autocomplete="off">
        </b-form-input>
      </div>
    </div>
    <b-btn class="mt-1" size="sm" variant="primary" @click="onApply">Apply</b-btn>
  </div>
</template>

<script>
  // @flow
  export default {
    name: 'QueryEditor',
    props: {
      queries: {
        type: Array,
      },
    },
    data() {
      return {
        newQuery: '',
        newQueryFieldCleared: false,
        queryEntries: [],
      };
    },
    methods: {
      initQueryEntries() {
        this.queryEntries
            = this.queries.reduce((accum: { text: string }[], value: string)
            : { text: string }[] => {
            if (value) {
              accum.push({ text: value });
            }
            return accum;
          }, []);
      },
      formatResult(r: Object) {
        let answer;
        // only use the 1st entry
        if (Array.isArray(r.answers)) {
          [answer] = r.answers;
        }

        if (answer) {
          if (answer.startsWith('Error:')) {
            return answer;
          }
          return `Prob. of ${r.query}: ${answer} (${r.queryDuration} ms)`;
        }
        return r;
      },
      onApply() {
        const queries = this.queryEntries.reduce((accum, value) => {
          if (value.text) {
            accum.push(value.text);
          }
          return accum;
        }, []);
        if (this.newQuery) {
          queries.unshift(this.newQuery);
        }
        this.newQuery = '';
        this.$emit('queriesChanged', queries);
      },
    },
    watch: {
      queries() {
        this.initQueryEntries();
      },
    },
    created() {
      this.initQueryEntries();
    },
  };
</script>

<style scoped>
  .query-editor-panel {
    height: 93px;
    overflow: auto
  }
</style>