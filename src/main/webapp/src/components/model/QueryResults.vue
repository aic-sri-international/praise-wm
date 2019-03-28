<template>
  <div id="queryResultsId" class="query-results-panel">
    <div class="query-results-border" v-for="(item, index) in queryResults">
      <b-btn size="sm" @click.stop="setQueryResultsIx(index)"
             :variant="index === queryResultsIx ? 'success' : 'outline-secondary'"
             class="msg-text">{{formatResult(item, index)}}</b-btn>
    </div>
    <b-popover :show="showHelp" target="queryResultsId" placement="right" triggers="">
      <div class="help-title">Query results messages</div>
      After running a second query you can click on a prior result message to
      see its result in the visualization panel.
    </b-popover>
  </div>
</template>

<script>
  // @flow
  import moment from 'moment';
  import { mapState, mapGetters, mapMutations } from 'vuex';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import type { QueryResultWrapper } from '@/store/model/types';

  export default {
    name: 'QueryResults',
    computed: {
      ...mapState(MODEL.MODULE, [
        'queryResults',
        'queryResultsIx',
      ]),
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
    },
    methods: {
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.QUERY_RESULTS_IX,
      ]),
      formatResult(r: QueryResultWrapper, index: number) {
        const expr = r.expressionResult;
        let answer;
        if (Array.isArray(expr.answers)) {
          answer = expr.answers.join('\n');
        }

        if (answer) {
          if (answer.startsWith('Error:')) {
            return answer;
          }
          const time = moment(expr.completionDate).format('h:mm:ss a');
          return `[${this.queryResults.length - index}] ${answer} (${expr.queryDuration} ms, ${time})`;
        }
        return r;
      },
    },
  };
</script>

<style scoped>
  .msg-text {
    width: 100%;
    white-space: normal;
    text-align: left;
  }
</style>
