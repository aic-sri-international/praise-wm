<template>
  <div class="query-results-panel">
    <div class="query-results-border" v-for="r in queryResults">
      {{formatResult(r)}}
    </div>
  </div>
</template>

<script>
  // @flow
  export default {
    name: 'QueryResults',
    props: {
      results: {
        type: Array,
      },
    },
    computed: {
      queryResults() {
        // unpack,
        return this.results.reduce((accum, value) => [...accum, ...value], []);
      },
    },
    methods: {
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
    },
  };
</script>

<style scoped>
  .query-results-panel {
    border: thin double lightgrey;
    padding: 4px;
    width: 500px;
    height: 100px;
    overflow: auto
  }
  .query-results-border {
    text-align: left;
    border: thin double lightgrey;
    padding: 4px;
    font-size: .8em;
    font-weight: bold;
  }
</style>