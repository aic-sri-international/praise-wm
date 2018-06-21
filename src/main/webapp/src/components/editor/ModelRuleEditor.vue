<template>
  <div>
      <div class="rule-border" >
        <span v-show="metaIsOpen">
          <editor ref="metadata_ref"
                  type="text"
                  :editTextWatch="editTextWatch"
                  :value="modelRuleWrapper.modelRule.metadata">
          </editor>
          <hr />
        </span>
        <editor ref="rule_ref"
                type="hogm"
                :editTextWatch="editTextWatch"
                :value="modelRuleWrapper.modelRule.rule">
        </editor>
      </div>
  </div>
</template>

<script>
  // @flow
  import Editor from './Editor';
  import type { ModelRuleDto } from './types';

  export default {
    name: 'ModelRuleEditor',
    components: {
      Editor,
    },
    props: {
      modelRuleWrapper: {
        modelRule: {
          metadata: {
            type: String,
            default: '',
          },
          rule: {
            type: String,
            required: true,
          },
        },
        emitData: {
          type: Boolean,
          required: true,
          default: false,
        },
        toggleMetadata: {
          type: Boolean,
          required: true,
          default: false,
        },
      },
    },
    data() {
      return {
        metaIsOpen: false,
        editTextWatch: false,
      };
    },
    methods: {
      getModelRule(): ModelRuleDto {
        return {
          metadata: this.$refs.metadata_ref.getValue().trim(),
          rule: this.$refs.rule_ref.getValue().trim(),
        };
      },
    },
    computed: {
      emitData() {
        return this.modelRuleWrapper.emitData;
      },
      toggleMetadata() {
        return this.modelRuleWrapper.toggleMetadata;
      },
    },
    watch: {
      modelRuleWrapper() {
        this.editTextWatch = !this.editTextWatch;
      },
      emitData() {
        this.$emit('modelRuleData', this.getModelRule());
      },
      toggleMetadata() {
        this.metaIsOpen = !this.metaIsOpen;
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