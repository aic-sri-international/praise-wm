<template>
  <div>
      <div class="rule-border" >
        <span v-show="metaIsOpen">
          <span class="float-left mr-2" @click.stop="metaIsOpen = false">
            <i class="fas fa-caret-up" style="color: green" data-fa-transform="grow-5 up-8"></i>
          </span>
          <editor ref="metadata_ref"
                  type="text"
                  :value="modelRuleWrapper.modelRule.metadata">
          </editor>
        </span>
        <span v-show="!metaIsOpen">
            <span class="float-left" @click.stop="metaIsOpen = true">
              <i class="fas fa-caret-down" style="color: green" data-fa-transform="grow-5 up-8"></i>
          </span>
        </span>
        <hr />
        <editor ref="rule_ref"
                type="hogm"
                :value="modelRuleWrapper.modelRule.rule">
        </editor>
      </div>
  </div>
</template>

<script>
  // @flow
  import Editor from './Editor';
  import type { ModelRule } from './types';

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
    computed: {
      emitData() {
        return this.modelRuleWrapper.emitData;
      },
      toggleMetadata() {
        return this.modelRuleWrapper.toggleMetadata;
      },
    },
    watch: {
      emitData(newValue: boolean) {
        if (newValue) {
          this.$emit('modelRuleData', this.getModelRule());
        }
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