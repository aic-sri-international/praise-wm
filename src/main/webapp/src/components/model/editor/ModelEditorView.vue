<template>
  <div class="top-level-container">
    <div>
      <b-form-textarea class="mb-2"
                         id="modelDescriptionId" max-rows="20"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         v-model="description"
                         :disabled="isQueryActive"
                         :class="{ disabledBackground: isQueryActive }"
                         wrap="off">
        </b-form-textarea>
      <!--For some reason we need to set placement to 'left' to display on the right-->
        <b-popover :show="showHelp" target="modelDescriptionId" placement="left" triggers="">
          Describes the model
        </b-popover>
    </div>
    <div class="modelEditor">
      <div class="dcl-editor" id="dclEditorId">
        <editor :editorInitFlag="dclEditInitFlag"
                :value="declarations"
                ref="dcl_editor_ref"
                :readOnly="isQueryActive"
                type="hogm">
        </editor>
      </div>
      <b-popover :show="showHelp" target="dclEditorId" triggers="">
        <div class="help-title">Global model declarations section</div>
        You can optionally include rules in this section.
      </b-popover>
      <model-editor
          id="modelEditorId"
          :rules="rules"
          :readOnly="isQueryActive"
          ref="seg_model_editor_ref"></model-editor>
    </div>
  </div>
</template>

<script>
  // @flow
  import { mapState, mapGetters, mapMutations } from 'vuex';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import { editorTransitions } from '@/store/model/types';
  import type {
    EditorTransition,
    SegmentedModelDto,
    ModelRuleDto,
    ModelEditorData,
  } from '@/store/model/types';
  import Editor from './Editor';
  import ModelEditor from './ModelEditor';

  export default {
    name: 'ModelEditorView',
    components: {
      Editor,
      ModelEditor,
    },
    data() {
      return {
        description: '',
        declarations: '',
        rules: [{
          metadata: '',
          rule: '',
        }],
        dclEditInitFlag: false,
      };
    },
    computed: {
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
      ...mapGetters(MODEL.MODULE, [
        MODEL.GET.CUR_MODEL_DTO,
        MODEL.GET.IS_QUERY_ACTIVE,
      ]),
      ...mapState(MODEL.MODULE, [
        'editorTransition',
      ]),
    },
    methods: {
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.EDITOR_TRANSITION,
        MODEL.SET.UPDATE_MODEL_DTO,
      ]),
      getUpdatedModel(): ModelEditorData {
        const rules: ModelRuleDto[] = this.$refs.seg_model_editor_ref.getModelRules();
        return {
          description: this.description.trim(),
          declarations: this.$refs.dcl_editor_ref.getValue().trim(),
          rules,
        };
      },
      loadModel() {
        const model: SegmentedModelDto = this[MODEL.GET.CUR_MODEL_DTO];
        this.description = model.description;
        this.declarations = model.declarations;
        this.rules = model.rules;
        this.dclEditInitFlag = !this.dclEditInitFlag;
        this[MODEL.SET.EDITOR_TRANSITION](editorTransitions.NONE);
      },
    },
    watch: {
      editorTransition(newTransition: EditorTransition) {
        if (newTransition === editorTransitions.LOAD) {
          this.loadModel();
        } else if (newTransition === editorTransitions.STORE) {
          const modelEditorData: ModelEditorData = this.getUpdatedModel();
          const modelName = this[MODEL.GET.CUR_MODEL_DTO].name;
          this[MODEL.SET.UPDATE_MODEL_DTO]({ modelName, modelEditorData });
          this[MODEL.SET.EDITOR_TRANSITION](editorTransitions.NONE);
        }
      },
    },
    mounted() {
      this.loadModel();
    },
  };
</script>

<style scoped>
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
