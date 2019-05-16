<template>
  <input
    v-show="false"
    ref="inputRef"
    :accept="accept"
    :multiple="multiple"
    class="input-file"
    type="file"
    @change="inputFileChanged($event.target.files)"
  >
</template>

<script lang="ts">
  import { Component, Prop, Vue } from 'vue-property-decorator';

  import { FileInfo, readTextFile } from '@/utils';

  @Component
  export default class InputTextFile extends Vue {
    @Prop({
      type: String,
      required: true,
    }) readonly accept!: string;

    @Prop({ default: false }) readonly multiple!: boolean;

    $refs!: {
      inputRef: HTMLInputElement
    };

    async inputFileChanged(files: FileList) {
      const promises: Promise<FileInfo>[] = [];
      Array.from(files)
      .forEach(async (file) => {
        promises.push(readTextFile(file));
      });
      const fileInfos: FileInfo[] = await Promise.all(promises);

      this.$refs.inputRef.value = '';
      this.$emit('change', fileInfos);
    }

    click() {
      this.$refs.inputRef.click();
    }
  }
</script>
