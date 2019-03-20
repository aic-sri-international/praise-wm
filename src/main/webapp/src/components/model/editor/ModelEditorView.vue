<template>
  <div class="top-level-container">
    <div>
      <b-form-textarea class="mb-2"
                         id="modelDescriptionId" max-rows="20"
                         placeholder="Enter a description for the model"
                         rows="1"
                         size="sm"
                         v-model="description"
                         wrap="off">
        </b-form-textarea>
      <!--For some reason we need to set placement to 'left' to display on the right-->
        <b-popover :show="showHelp" target="modelDescriptionId" placement="left" triggers="">
          Describes the model
        </b-popover>
    </div>
    <div class="modelEditor">
      <div class="dcl-editor" id="dclEditorId">
        <editor :editorInitFlag="dclEditInitFlag" :value="declarations"
                ref="dcl_editor_ref" type="hogm">
        </editor>
      </div>
      <b-popover :show="showHelp" target="dclEditorId" triggers="">
        <div class="help-title">Global model declarations section</div>
        You can optionally include rules in this section.
      </b-popover>
      <model-editor id="modelEditorId" :rules="rules" ref="seg_model_editor_ref"></model-editor>
    </div>
  </div>
</template>

<script>
  // @flow
  import { mapGetters, mapMutations } from 'vuex';
  import { HELP_VXC as HELP, MODEL_VXC as MODEL } from '@/store';
  import { editorTransitions } from '@/store/model/types';
  import type { EditorTransition } from '@/store/model/types';
  import type {
    SegmentedModelDto,
    ModelRuleDto,
  } from '@/components/model/types';
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
        MODEL.GET.EDITOR_TRANSITION,
        MODEL.GET.CUR_MODEL_DTO,
      ]),
    },
    methods: {
      ...mapMutations(MODEL.MODULE, [
        MODEL.SET.EDITOR_TRANSITION,
        MODEL.SET.MODEL_DTO,
      ]),
      getUpdatedModel() {
        const model: SegmentedModelDto = this.curModelDto;
        const rules: ModelRuleDto[] = this.$refs.seg_model_editor_ref.getModelRules();
        return {
          name: model.name,
          description: this.description.trim(),
          declarations: this.$refs.dcl_editor_ref.getValue().trim(),
          rules,
          queries: model.queries,
        };
      },
      loadModel() {
        const model: SegmentedModelDto = this.curModelDto;
        this.description = model.description;
        this.declarations = model.declarations;
        this.rules = model.rules;
        this.dclEditInitFlag = !this.dclEditInitFlag;
        this.setEditorTransition(editorTransitions.NONE);
      },
    },
    watch: {
      editorTransition(newTransition: EditorTransition) {
        if (newTransition === editorTransitions.LOAD) {
          this.loadModel();
        } else if (newTransition === editorTransitions.STORE) {
          const model: SegmentedModelDto = this.getUpdatedModel();
          this.setModelDto(model);
          this.setEditorTransition(editorTransitions.NONE);
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
