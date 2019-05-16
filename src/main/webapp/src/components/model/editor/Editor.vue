<template>
  <div>
    <span ref="editorRef" />
  </div>
</template>


<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';

  import AceModelEditor from './aceModelEditor';
  import { EditorInterface } from '@/components/model/editor/types';

  @Component
  export default class Editor extends Vue implements EditorInterface {
    @Prop({
      type: String,
      required: true,
      validator: (value: string) => ['text', 'hogm'].includes(value),
    }) readonly type!: string;

    @Prop({
      type: String,
      validator: (value: string) => ['metadata'].includes(value),
    }) readonly styleClass?: string;

    @Prop({
      type: String,
      required: true,
    }) readonly value!: string;

    @Prop({
      type: Boolean,
      required: true,
    }) readonly editorInitFlag!: boolean;

    @Prop({
      type: Boolean,
      default: false,
    }) readonly readOnly!: boolean;

    canUndo = false;

    canRedo = false;

    private editor$!: AceModelEditor;

    @Watch('editorInitFlag')
    onEditorInitFlag() {
      this.$nextTick(() => this.initEditorText());
    }

    @Watch('readOnly')
    onReadOnly(readOnly: boolean) {
      this.editor$.setReadOnly(readOnly);
    }

    mounted() {
      this.createEditor();
    }

    beforeDestroy() {
      this.editor$.destroy();
    }

    getValue(): string {
      return this.editor$.getValue();
    }

    focus() {
      this.editor$.focus();
    }

    initEditorText() {
      this.editor$.setValue(this.value);
      this.editor$.clearSelection();
      this.editor$.resetUndoManager();
      this.canUndo = false;
      this.canRedo = false;
    }

    createEditor() {
      if (this.editor$ !== undefined) {
        this.editor$.destroy();
      }

      let styleClass;

      if (this.styleClass) {
        styleClass = `ace_${this.styleClass}_style`;
      }

      this.editor$ = new AceModelEditor({
        mode: `ace/mode/${this.type}`,
        showGutter: false,
        value: '',
        minLines: 1,
        maxLines: 1000,
        readOnly: this.readOnly,
        styleClass,
      });
      const that = this;
      this.editor$.onChange(() => {
        that.canUndo = that.editor$.canUndo();
        that.canRedo = that.editor$.canRedo();
      });
      that.initEditorText();
      this.editor$.appendToRef(this.$refs.editorRef);
    }
  }
</script>

<style lang="scss">
  .ace_metadata_style {
    font-style: italic;
    color: #0066CC !important;
  }
</style>
