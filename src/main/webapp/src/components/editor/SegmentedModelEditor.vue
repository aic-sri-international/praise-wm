<template>
  <div>
      <div v-for="(mre, index) in mrs"
           @contextmenu.prevent="$refs.ctxmenu_ref.open($event, { mre: mre, index: index })">
        <model-rule-editor :model-rule="mre"></model-rule-editor>
      </div>
    <context-menu id="context-menu" ref="ctxmenu_ref" @ctx-open="setCurrentRightClickData">
      <li @click="onToggleMetaData">Toggle Metadata</li>
      <li class="disabled">option 2</li>
      <li>option 3</li>
    </context-menu>
  </div>
</template>

<script>
  import contextMenu from 'vue-context-menu';
  import ModelRuleEditor from './ModelRuleEditor';

  export default {
    name: 'SegmentedModelEditor',
    components: {
      ModelRuleEditor,
      contextMenu,
    },
    // props: {
    //   modelRules: {
    //     type: Array,
    //     required: true,
    //   },
    // },
    data() {
      return {
        mrs: [],
        lastRightClickData: {},
      };
    },
    methods: {
      onOptionOne() {
        console.log('option 1 selected');
      },
      setCurrentRightClickData(data) {
        this.lastRightClickData = data;
        console.log(data);
      },
    },
    created() {
      this.mrs.push({
        metadata: 'metadata for rule 1  goes here',
        rule: 'variables for rule 1 along with its definition goes here',
      });
      this.mrs.push({
        metadata: 'metadata for a rule 2 goes here',
        rule: 'variables for rule 1 along with its definition goes here',
      });
      this.mrs.push({
        metadata: 'metadata for a rule 3 goes here',
        rule: 'variables for rule 3 along with its definition goes here',
      });
    },
  };
</script>

<style scoped>
</style>