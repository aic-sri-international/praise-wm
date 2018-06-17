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
      value: {
        type: String,
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
        this.editor$ = new AceModelEditor({
          mode: `ace/mode/${this.type}`,
          showGutter: false,
          value: '',
          minLines: 1,
          maxLines: 1000,
          readOnly: this.readOnly === true,
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
      value() {
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

<style scoped>
</style>