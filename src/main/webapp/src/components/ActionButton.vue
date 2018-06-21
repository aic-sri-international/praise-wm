<template>
  <b-btn size="sm"
         :title="title"
         variant="outline-secondary"
         :disabled="disabled"
         :style="{ color: btnColor }"
         @click.stop="$emit('clicked')">
      <i :class="btnClass" :data-fa-transform="transform"></i>
  </b-btn>
</template>

<script>
  // @flow

  export default {
    name: 'ActionButton',
    props: {
      type: {
        type: String,
        validator: (value: string) =>
          ['clone', 'download', 'add', 'edit', 'delete', 'play',
            'undo', 'redo', 'open', 'save', 'broom',
            'angle-left', 'angle-right', 'eye', 'eye-slash',
            'sync', 'help'].includes(value),
      },
      title: {
        type: String,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      transform: {
        type: String,
      },
    },
    computed: {
      btnColor() {
        return this.disabled ? 'gray' : 'blue';
      },
      btnClass() {
        let name;
        switch (this.type) {
          case 'clone':
            name = 'fa-clone';
            break;
          case 'download':
            name = 'fa-download';
            break;
          case 'add':
            name = 'fa-plus';
            break;
          case 'edit':
            name = 'fa-edit';
            break;
          case 'delete':
            name = 'fa-trash-alt';
            break;
          case 'play':
            name = 'fa-play';
            break;
          case 'undo':
            name = 'fa-undo-alt';
            break;
          case 'redo':
            name = 'fa-redo-alt';
            break;
          case 'open':
            name = 'fa-folder-open';
            break;
          case 'save':
            name = 'fa-save';
            break;
          case 'broom':
            name = 'fa-broom';
            break;
          case 'angle-left':
            name = 'fa-angle-left';
            break;
          case 'angle-right':
            name = 'fa-angle-right';
            break;
          case 'eye':
            name = 'fa-eye';
            break;
          case 'eye-slash':
            name = 'fa-eye-slash';
            break;
          case 'help':
            name = 'fa-question-circle';
            break;
          case 'sync':
            name = 'fa-sync';
            break;
          default:
            throw Error(`unsupported type: ${this.type}`);
        }
        return `fas ${name}`;
      },
    },
  };
</script>