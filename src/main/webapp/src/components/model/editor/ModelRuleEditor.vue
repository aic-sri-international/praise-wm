<template>
  <div>
      <div class="rule-border" >
        <span v-show="modelRuleWrapper.openMetadata">
          <editor ref="metadata_ref"
                  type="text"
                  styleClass="metadata"
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
  import type { ModelRuleDto } from '@/components/model/types';
  import Editor from './Editor';

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
        openMetadata: {
          type: Boolean,
          required: false,
          default: false,
        },
      },
    },
    data() {
      return {
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
    },
    watch: {
      modelRuleWrapper() {
        this.editTextWatch = !this.editTextWatch;
      },
      emitData() {
        this.$emit('modelRuleData', this.getModelRule());
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
