<template>
  <b-btn
    :disabled="disabled"
    :style="{ color: btnColor }"
    :title="title"
    size="sm"
    variant="outline-secondary"
    @click.stop="$emit('clicked')"
  >
    <i
      :class="btnClass"
      :data-fa-transform="transform"
    />
  </b-btn>
</template>

<script lang="ts">
  import { Component, Prop, Vue } from 'vue-property-decorator';

  @Component
  export default class ActionButton extends Vue {
    @Prop({
      type: String,
      required: true,
      validator: (value: string) => ['clone', 'download', 'add', 'edit', 'delete', 'play',
        'undo', 'redo', 'open', 'save', 'broom',
        'angle-left', 'angle-right', 'eye', 'eye-slash',
        'help', 'sync'].includes(value),
    }) readonly type!: string;

    @Prop(String) readonly title?: string;

    @Prop({ default: false }) readonly disabled!: boolean;

    @Prop(String) readonly transform?: string;

    get btnColor() {
      return this.disabled ? 'gray' : 'blue';
    }

    get btnClass() {
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
    }
  }
</script>
