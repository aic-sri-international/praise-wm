<template>
  <div>
    <div class="rule-border">
      <span v-show="modelRuleWrapper.openMetadata">
        <editor
          ref="metadataRef"
          type="text"
          :read-only="readOnly"
          style-class="metadata"
          :editor-init-flag="editorInitFlag"
          :value="modelRuleWrapper.modelRule.metadata"
        />
        <hr>
      </span>
      <editor
        ref="ruleRef"
        type="hogm"
        :read-only="readOnly"
        :editor-init-flag="editorInitFlag"
        :value="modelRuleWrapper.modelRule.rule"
      />
    </div>
  </div>
</template>

<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';

  import { ModelRuleDto } from '@/store/model/types';
  import Editor from './Editor.vue';
  import {
    EditorInterface,
    ModelRuleEditorInterface,
    ModelRuleWrapper,
  } from '@/components/model/editor/types';

  @Component({
    components: {
      Editor,
    },
  })
  export default class ModelRuleEditor extends Vue implements ModelRuleEditorInterface {
    @Prop({
      type: Object,
      required: true,
    }) readonly modelRuleWrapper!: ModelRuleWrapper;

    @Prop({
      type: Boolean,
      default: false,
    }) readonly readOnly!: boolean;

    $refs!: {
      metadataRef: EditorInterface;
      ruleRef: EditorInterface;
    };

    editorInitFlag = false;

    @Watch('modelRuleWrapper')
    onEditorInitFlag() {
      this.editorInitFlag = !this.editorInitFlag;
    }

    getModelRule(): ModelRuleDto {
      return {
        metadata: this.$refs.metadataRef.getValue().trim(),
        rule: this.$refs.ruleRef.getValue().trim(),
      };
    }

    focusOnRuleEditor() {
      this.$refs.ruleRef.focus();
    }
  }
</script>

<style lang="scss" scoped>
  .rule-border {
    border: thin double lightgrey;
    padding: 10px;
  }
</style>
