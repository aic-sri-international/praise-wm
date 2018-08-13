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
      <div>
        <li class="ctx-item" @click="onToggleMetadata">Toggle Metadata</li>
        <li class="ctx-item" @click="onOpenAllMetadata(true)">Open All Metadata</li>
        <li class="ctx-item" @click="onOpenAllMetadata(false)">Close All Metadata</li>
        <li class="ctx-item" @click="onInsertModelRule(true)">Insert Rule Above</li>
        <li class="ctx-item" @click="onInsertModelRule(false)">Insert Rule Below</li>
        <li class="ctx-item" @click="onDeleteModelRule">Delete Rule</li>
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
          openMetadata: false,
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
        this.mrws[this.lastRightClickData.index].openMetadata
            = !this.mrws[this.lastRightClickData.index].openMetadata;
      },
      onOpenAllMetadata(open: boolean) {
        this.mrws.forEach(((mrw) => {
          // eslint-disable-next-line no-param-reassign
          mrw.openMetadata = open;
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
</style>