<template>
  <div class="top-level-container">
    <div>
      <b-form-textarea
        id="modelDescriptionId"
        v-model="description"
        :class="{ disabledBackground: isQueryActive }"
        :disabled="isQueryActive"
        class="mb-2"
        max-rows="20"
        placeholder="Enter a description for the model"
        rows="1"
        size="sm"
        wrap="off"
      />
      <!--For some reason we need to set placement to 'left' to display on the right-->
      <b-popover
        :show="showHelp"
        placement="left"
        target="modelDescriptionId"
        triggers=""
      >
        Describes the model
      </b-popover>
    </div>
    <div class="modelEditor">
      <div
        id="dclEditorId"
        class="dcl-editor"
      >
        <editor
          ref="dclEditorRef"
          :editor-init-flag="dclEditInitFlag"
          :read-only="isQueryActive"
          :value="declarations"
          type="hogm"
        />
      </div>
      <b-popover
        :show="showHelp"
        target="dclEditorId"
        triggers=""
      >
        <div class="help-title">
          Global model declarations section
        </div>
        You can optionally include rules in this section.
      </b-popover>
      <model-editor
        id="modelEditorId"
        ref="segModelEditorRef"
        :read-only="isQueryActive"
        :rules="rules"
      />
    </div>
  </div>
</template>

<script lang="ts">
  import { Component, Vue, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import {
    EditorTransition,
    ModelEditorData,
    ModelRuleDto,
    SegmentedModelDto,
    UpdateModelDtoPayload,
  } from '@/store/model/types';
  import Editor from './Editor.vue';
  import ModelEditor from './ModelEditor.vue';
  import { EditorInterface, ModelEditorInterface } from '@/components/model/editor/types';
  import { HELP_MODULE_NAME } from '@/store/help/constants';
  import { MODEL_MODULE_NAME } from '@/store/model/constants';

  const helpModule = namespace(HELP_MODULE_NAME);
  const modelModule = namespace(MODEL_MODULE_NAME);

  @Component({
    components: {
      Editor,
      ModelEditor,
    },
  })
  export default class ModelEditorView extends Vue {
    description = '';

    declarations = '';

    rules: ModelRuleDto[] = [{
      metadata: '',
      rule: '',
    }];

    dclEditInitFlag = false;

    $refs!: {
      dclEditorRef: EditorInterface;
      segModelEditorRef: ModelEditorInterface;
    };

    @helpModule.State
    showHelp!: boolean;

    @modelModule.State
    editorTransition!: boolean;

    @modelModule.Getter
    isQueryActive!: boolean;

    @modelModule.Getter
    curModelDto!: SegmentedModelDto;

    @modelModule.Mutation
    setEditorTransition!: (payload: EditorTransition) => void;

    @modelModule.Mutation
    updateModelDto!: (payload: UpdateModelDtoPayload) => void;

    @Watch('editorTransition')
    onEditorTransition(newTransition: EditorTransition) {
      if (newTransition === EditorTransition.LOAD) {
        this.loadModel();
      } else if (newTransition === EditorTransition.STORE) {
        const modelEditorData: ModelEditorData = this.getUpdatedModel();
        const modelName = this.curModelDto.name;
        this.updateModelDto({
          modelName,
          modelEditorData,
        });
        this.setEditorTransition(EditorTransition.NONE);
      }
    }

    mounted() {
      this.loadModel();
    }

    getUpdatedModel(): ModelEditorData {
      const rules: ModelRuleDto[] = this.$refs.segModelEditorRef.getModelRules();
      return {
        description: this.description.trim(),
        declarations: this.$refs.dclEditorRef.getValue()
        .trim(),
        rules,
      };
    }

    loadModel() {
      const model: SegmentedModelDto = this.curModelDto;
      this.description = model.description || '';
      this.declarations = model.declarations || '';
      this.rules = model.rules;
      this.dclEditInitFlag = !this.dclEditInitFlag;
      this.setEditorTransition(EditorTransition.NONE);
    }
  }
</script>

<style lang="scss" scoped>
  .top-level-container {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .dcl-editor {
    border: thin double lightgrey;
    padding: 10px;
  }

  .modelEditor {
    overflow-y: auto;
  }
</style>
