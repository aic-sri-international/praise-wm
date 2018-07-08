<template>
  <div>
    <div v-for="(mrw, index) in mrws"
         @contextmenu.prevent="$refs.ctxmenu_ref.open($event, { index: index })">
      <model-rule-editor
          id="modelRuleEditor"
          :model-rule-wrapper="mrw"
          @modelRuleData="mrd => modelRules.push(mrd)">
      </model-rule-editor>
    </div>
    <context-menu id="context-menu" ref="ctxmenu_ref" @ctx-open="setCurrentRightClickData">
      <div class="context-menu">
        <li @click="onToggleMetadata">Toggle Metadata</li>
        <li @click="onToggleAllMetadata">Toggle All Metadata</li>
        <li @click="onInsertModelRule(true)">Insert Rule Above</li>
        <li @click="onInsertModelRule(false)">Insert Rule Below</li>
        <li @click="onDeleteModelRule">Delete Rule</li>
      </div>
    </context-menu>
  </div>
</template>

<script>
  import ContextMenu from 'vue-context-menu';
  import type { ModelRuleDto, ModelRuleWrapper } from './types';
  import ModelRuleEditor from './ModelRuleEditor';

  export default {
    name: 'SegmentedModelEditor',
    components: {
      ContextMenu,
      ModelRuleEditor,
    },
    props: {
      rules: {
        type: Array,
        default() {
          return {
            metadata: '',
            rule: '',
          };
        },
      },
    },
    data() {
      return {
        mrws: [],
        lastRightClickData: {},
        modelRules: [],
      };
    },
    methods: {
      async getModelRules() : ModelRuleDto[] {
        this.modelRules = [];
        this.mrws.forEach(((mrw) => {
          // eslint-disable-next-line no-param-reassign
          mrw.emitData = !mrw.emitData;
        }));
        await this.$nextTick();
        return [...this.modelRules];
      },
      wrapModelRules(mrs: ModelRuleDto[]) : ModelRuleWrapper[] {
        return mrs.map(mr => ({
          modelRule: mr,
          toggleMetadata: false,
          emitData: false,
        }));
      },
      getEmptyWrappedModelRule() : ModelRuleWrapper {
        return this.wrapModelRules([{ metadata: '', rule: '' }])[0];
      },
      initialize(mrs: ModelRuleDto[]) {
        this.mrws = this.wrapModelRules(mrs);
      },
      onToggleMetadata() {
        this.mrws[this.lastRightClickData.index].toggleMetadata
            = !this.mrws[this.lastRightClickData.index].toggleMetadata;
      },
      onToggleAllMetadata() {
        this.mrws.forEach(((mrw) => {
          // eslint-disable-next-line no-param-reassign
          mrw.toggleMetadata = !mrw.toggleMetadata;
        }));
      },
      onInsertModelRule(isAbove: boolean) {
        let ix = this.lastRightClickData.index;
        if (!isAbove) {
          ix += 1;
        }
        this.mrws.splice(ix, 0, this.getEmptyWrappedModelRule());
      },
      onDeleteModelRule() {
        this.mrws.splice(this.lastRightClickData.index, 1);
        if (!this.mrws.length) {
          this.mrws.push(this.getEmptyWrappedModelRule());
        }
      },
      setCurrentRightClickData(data) {
        this.lastRightClickData = data;
      },
    },
    watch: {
      rules(newValue: ModelRuleDto[]) {
        this.initialize(newValue);
      },
    },
    created() {
      this.initialize(this.rules);
    },
  };
</script>

<style scoped>
  .context-menu {
    margin-left: 4px;
  }
  #context-menu li:hover, li:focus {
    color: inherit;
    background-color: #e9e9e9;
    text-decoration: none;
    transition: all 0.3s;
    cursor: pointer;
  }
</style>