<template>
  <div class="query-results-panel">
    <div class="query-results-border" v-for="(item, index) in items">
      <b-btn size="sm" @click.stop="onItemClicked(item, index)"
             :variant="item.selected ? 'success' : 'outline-secondary'"
             style="width: 100%">{{formatResult(item)}}</b-btn>
    </div>
  </div>
</template>

<script>
  // @flow
  import moment from 'moment';
  import type { ExpressionResultDto } from './types';


  export default {
    name: 'QueryResults',
    props: {
      results: {
        type: Array,
      },
      selectedIx: {
        type: Number,
        required: true,
      },
    },
    data() {
      return {
        items: this.updateItems(this.selectedIx),
      };
    },
    computed: {
      queryResults() : ExpressionResultDto[] {
        return this.results.reduce((accum, value, index) => {
          accum.push(Object.assign({ selected: index === 0 }, value));
          return accum;
        }, []);
      },
    },
    watch: {
      results() {
        this.updateItems(this.selectedIx);
      },
      selectedIx() {
        this.updateItems(this.selectedIx);
      },
    },
    methods: {
      onItemClicked(item: Object, index: number) {
        if (item.selected) {
          return;
        }
  
        this.$emit('selectionChanged', index);
      },
      updateItems(selected: number) {
        this.$nextTick(() => {
          this.items = this.results.reduce((accum, value, index) => {
            accum.push(Object.assign({ selected: index === selected }, value));
            return accum;
          }, []);
        });
      },
      formatResult(r: ExpressionResultDto) {
        let answer;
        // only use the 1st entry
        if (Array.isArray(r.answers)) {
          [answer] = r.answers;
        }

        if (answer) {
          if (answer.startsWith('Error:')) {
            return answer;
          }
          const time = moment(r.completionDate).format('h:mm:ss a');
          return `Prob. of ${r.query}: ${answer} (${r.queryDuration} ms, ${time})`;
        }
        return r;
      },
    },
  };
</script>

<style scoped>
</style>