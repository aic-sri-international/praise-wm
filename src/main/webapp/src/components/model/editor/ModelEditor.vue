<template>
  <div>
    <div v-if="rules.length" id="popupId" style="height: 1px;text-align: right">.</div>
    <div v-for="(mrw, index) in mrws"
         @contextmenu.prevent="$refs.ctxmenu_ref.open($event, { index: index })">
      <model-rule-editor :model-rule-wrapper="mrw" :ref="getRefName(index)"></model-rule-editor>
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
    <!--For some reason we need to specify placement 'left' to have popup display on the right-->
    <b-popover :show="showHelp && rules.length > 0" target="popupId" placement="left" triggers="">
      <div class="help-title">Right-click within a rule section to display a context menu</div>
      The context menu allows you to toggle the display of metadata for the rule, insert a new rule,
      or delete the rule.
    </b-popover>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex';
  import { HELP_VXC as HELP } from '@/store';
  import ContextMenu from 'vue-context-menu';
  import type { ModelRuleDto } from '@/components/model/types';
  import ModelRuleEditor from './ModelRuleEditor';


  type ModelRuleWrapper = {
    modelRule: ModelRuleDto,
    toggleMetadata: boolean,
    emitData: boolean,
  };

  export default {
    name: 'ModelEditor',
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
      getRefName(ix) {
        return `modelRuleEditors_${ix}_ref`;
      },
      getModelRules() : ModelRuleDto[] {
        this.modelRules = [];
        for (let i = 0; i < this.mrws.length; i += 1) {
          const refName = this.getRefName(i);
          const mreRef = this.$refs[refName];
          if (!mreRef) {
            // eslint-disable-next-line no-console
            console.error(`ref not found for ${refName}`);
          } else {
            const mr : ModelRuleDto = this.modelRules.push(mreRef[0].getModelRule());
            this.modelRules.push(mr);
          }
        }
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
    computed: {
      ...mapGetters(HELP.MODULE, [
        HELP.GET.SHOW_HELP,
      ]),
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
