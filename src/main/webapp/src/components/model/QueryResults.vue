<template>
  <!-- eslint-disable vue/require-v-for-key -->
  <div
    id="queryResultsId"
    class="query-results-panel"
  >
    <div
      v-for="(item, index) in queryResults"
      class="query-results-border"
    >
      <b-btn
        :variant="index === queryResultsIx ? 'success' : 'outline-secondary'"
        class="msg-text"
        size="sm"
        @click.stop="setQueryResultsIx(index)"
      >
        {{ formatResult(item, index) }}
      </b-btn>
    </div>
    <b-popover
      :show="showHelp"
      placement="right"
      target="queryResultsId"
      triggers=""
    >
      <div class="help-title">
        Query results messages
      </div>
      After running a second query you can click on a prior result message to
      see its result in the visualization panel.
    </b-popover>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import moment from 'moment';
  import { QueryResultWrapper } from '@/store/model/types';
  import { HELP_MODULE_NAME } from '@/store/help/constants';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';

  const helpModule = namespace(HELP_MODULE_NAME);
  const modelModule = namespace(MODEL_MODULE_NAME);

  @Component
  export default class QueryResults extends Vue {
    @helpModule.State showHelp!: boolean;

    @modelModule.State queryResults!: QueryResultWrapper[];

    @modelModule.State queryResultsIx!: number;

    @modelModule.Mutation setQueryResultsIx!: (index: number) => void;

    formatResult(r: QueryResultWrapper, index: number): string {
      const expr = r.expressionResult;
      let answer;
      if (Array.isArray(expr.answers)) {
        answer = expr.answers.join('\n');
      }

      if (answer) {
        if (answer.startsWith('Error:')) {
          return answer;
        }
        const time = moment(expr.completionDate)
        .format('h:mm:ss a');
        return `[${this.queryResults.length - index}] ${answer} (${expr.queryDuration} ms, ${time})`;
      }
      return '';
    }
  }
</script>

<style lang="scss" scoped>
  .msg-text {
    width: 100%;
    white-space: normal;
    text-align: left;
  }
</style>
