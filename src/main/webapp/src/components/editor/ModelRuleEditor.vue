<template>
  <div>
      <div class="rule-border" >
        <span v-show="metaIsOpen">
          <span class="float-left mr-2" @click.stop="metaIsOpen = false">
            <i class="fas fa-caret-up" style="color: green" data-fa-transform="grow-5 up-8"></i>
          </span>
          <editor ref="metadata_ref" type="text" :value="modelRule.metadata" :read-only="true"></editor>
        </span>
        <span v-show="!metaIsOpen">
            <span class="float-left" @click.stop="metaIsOpen = true">
              <i class="fas fa-caret-down" style="color: green" data-fa-transform="grow-5 up-8"></i>
          </span>
        </span>
        <hr />
        <editor ref="rule_ref" type="hogm" :value="modelRule.rule"></editor>
      </div>
  </div>
</template>

<script>
  // @flow
  import Editor from './Editor';
  import type { ModelRule } from './modelSplitter';

  export default {
    name: 'ModelRuleEditor',
    components: {
      Editor,
    },
    props: {
      modelRule: {
        type: Object,
        required: true,
      },
    },
    data() {
      return {
        metaIsOpen: true,
      };
    },
    methods: {
      getModelRule(): ModelRule {
        return {
          metadata: this.$refs.metadata_ref.getValue(),
          rule: this.$refs.rule_ref.getValue(),
        };
      },
    },
  };
</script>

<style scoped>
  .rule-border {
    border: thin double lightgrey;
    padding: 10px;
  }
</style>