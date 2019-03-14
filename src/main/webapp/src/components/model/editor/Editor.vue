<template>
  <div>
      <span ref="editor_ref"></span>
  </div>
</template>

<script>
  // @flow
  import AceModelEditor from './aceModelEditor';

  export default {
    name: 'Editor',
    props: {
      type: {
        type: String,
        validator: (value: string) =>
          ['text', 'hogm'].includes(value),
        required: true,
      },
      styleClass: {
        type: String,
        validator: (value: string) =>
          ['metadata'].includes(value),
        required: false,
      },
      value: {
        type: String,
        required: true,
      },
      editorInitFlag: {
        type: Boolean,
        required: true,
      },
      readOnly: {
        type: Boolean,
      },
    },
    data() {
      return {
        canUndo: false,
        canRedo: false,
      };
    },
    methods: {
      getValue() {
        return this.editor$.getValue();
      },
      initEditorText() {
        this.editor$.setValue(this.value);
        this.editor$.clearSelection();
        this.editor$.resetUndoManager();
        this.canUndo = false;
        this.canRedo = false;
      },
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
          readOnly: this.readOnly === true,
          styleClass,
        });
        const that = this;
        this.editor$.on('change', () => {
          that.canUndo = that.editor$.canUndo();
          that.canRedo = that.editor$.canRedo();
        });
        that.initEditorText();
        this.editor$.appendToRef(this.$refs.editor_ref);
      },
    },
    watch: {
      editorInitFlag() {
        this.$nextTick(() => this.initEditorText());
      },
    },
    mounted() {
      this.createEditor();
    },
    beforeDestroy() {
      this.editor$.destroy();
    },
  };
</script>

<style>
  .ace_metadata_style {
    font-style: italic;
    color: #0066CC !important;
  }
</style>
