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
      <b-popover :show="showHelp" target="modelDescriptionId" placement="right" triggers="">
        Describes the model
      </b-popover>
    </div>
    <div class="modelEditor" id="modelEditorId">
      <div class="dcl-editor" id="dclEditorId">
        <editor :editTextWatch="dclEditTextWatch" :value="declarations"
                ref="dcl_editor_ref" type="hogm">
        </editor>
      </div>
      <b-popover :show="showHelp" target="dclEditorId" triggers="">
        <div class="help-title">Global model declarations section</div>
        You can optionally include rules in this section.
      </b-popover>
      <model-editor :rules="rules" ref="seg_model_editor_ref"></model-editor>
      <b-popover :show="showHelp && rules.length > 0" target="modelEditorId" triggers="">
        <div class="help-title">Right-click within a rule section to display a context menu</div>
        The context menu allows you to toggle the display of metadata for the rule, insert a new rule,
        or delete the rule.
      </b-popover>
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
        dclEditTextWatch: false,
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
        MODEL.SET.CUR_MODEL_DTO,
      ]),
      async getUpdatedModel() {
        const model: SegmentedModelDto = this.curModelDto;
        const rules: ModelRuleDto[] = await this.$refs.seg_model_editor_ref.getModelRules();
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
        this.dclEditTextWatch = !this.dclEditTextWatch;
        this.setEditorTransition(editorTransitions.NONE);
      },
    },
    watch: {
      async editorTransition(newTransition: EditorTransition) {
        if (newTransition === editorTransitions.LOAD) {
          this.loadModel();
        } else if (newTransition === editorTransitions.STORE) {
          const model: SegmentedModelDto = await this.getUpdatedModel();
          this.setCurModelDto(model);
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
    flex-direction: row;
  }

  .modelEditor {
    flex: 1 1 auto;
    position: relative;
    overflow-y: auto;
  }

  .dcl-editor {
    border: thin double lightgrey;
    padding: 10px;
  }
</style>
