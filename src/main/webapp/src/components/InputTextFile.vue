<template>
  <input v-show="false" ref="input_ref"
         type="file"
         class="input-file"
         :multiple="multiple"
         @change="inputFileChanged($event.target.files)"
         :accept="accept">
</template>

<script>
  import type { FileInfo } from '@/utils';
  import { readTextFile } from '@/utils';

  export default {
    name: 'InputTextFile',
    props: {
      accept: {
        type: String,
        required: true,
      },
      multiple: {
        type: Boolean,
        default: false,
      },
    },
    methods: {
      async inputFileChanged(files: FileList) : FileInfo[] {
        const promises : Promise<FileInfo>[] = [];
        Array.from(files).forEach(async (file) => {
          promises.push(readTextFile(file));
        });
        const fileInfos : FileInfo[] = await Promise.all(promises);

        this.$refs.input_ref.value = '';
        this.$emit('change', fileInfos);
      },
      click() {
        this.$refs.input_ref.click();
      },
    },
  };
</script>

<style scoped>

</style>