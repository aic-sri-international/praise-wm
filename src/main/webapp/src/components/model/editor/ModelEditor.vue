<template>
  <div>
    <div
      v-if="mrws.length"
      id="popupId"
      style="height: 1px;text-align: right"
    >
      .
    </div>
    <div
      v-for="(mrw, index) in mrws"
      :key="mrw.id"
      @contextmenu.prevent="$refs.ctxmenuRef.open($event, { index })"
    >
      <model-rule-editor
        :ref="getModelRuleRefName(mrw.id)"
        :model-rule-wrapper="mrw"
        :read-only="readOnly"
      />
    </div>
    <context-menu
      id="context-menu"
      ref="ctxmenuRef"
      @ctx-open="setCurrentRightClickData"
    >
      <div>
        <li
          class="ctx-item"
          @click="onToggleMetadata"
        >
          Toggle Metadata
        </li>
        <li
          class="ctx-item"
          @click="onOpenAllMetadata(true)"
        >
          Open All Metadata
        </li>
        <li
          class="ctx-item"
          @click="onOpenAllMetadata(false)"
        >
          Close All Metadata
        </li>
        <li
          class="ctx-item"
          @click="onInsertModelRule(true)"
        >
          Insert Rule Above
        </li>
        <li
          class="ctx-item"
          @click="onInsertModelRule(false)"
        >
          Insert Rule Below
        </li>
        <li
          class="ctx-item"
          @click="onDeleteModelRule"
        >
          Delete Rule
        </li>
      </div>
    </context-menu>
    <!--For some reason we need to specify placement 'left' to have popup display on the right-->
    <b-popover
      :show="showHelp && mrws.length > 0"
      placement="left"
      target="popupId"
      triggers=""
    >
      <div class="help-title">
        Right-click within a rule section to display a context menu
      </div>
      The context menu allows you to toggle the display of metadata for the rule, insert a new rule,
      or delete the rule.
    </b-popover>
  </div>
</template>

<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';

  import { namespace } from 'vuex-class';
  import { HELP_VXC } from '@/store';
  import ContextMenu from 'vue-context-menu';
  import { ModelRuleDto } from '@/store/model/types';
  import ModelRuleEditor from './ModelRuleEditor.vue';
  import {
    ModelEditorInterface,
    ModelRuleEditorInterface,
    ModelRuleWrapper,
  } from '@/components/model/editor/types';

  const helpModule = namespace(HELP_VXC.MODULE);

  let nextMrwId = 0;
  const nextModelRuleWrapperId = () => {
    const cur = nextMrwId;
    nextMrwId += 1;
    return cur;
  };

  type ContextMenuData = { index: number }

  type ContextMenuType = Vue & {
    open: (event: MouseEvent, data: ContextMenuData) => any,
  }

  @Component({
    components: {
      ContextMenu,
      ModelRuleEditor,
    },
  })
  export default class ModelEditor extends Vue implements ModelEditorInterface {
    @Prop({
      type: Array,
      default: () => [{
        metadata: '',
        rule: '',
      }],
    }) readonly rules!: Array<ModelRuleDto>;

    @Prop({
      type: Boolean,
      default: false,
    }) readonly readOnly!: boolean;

    @helpModule.State
    showHelp!: boolean;

    $refs!: {
      ctxmenuRef: ContextMenuType;
    };

    mrws: ModelRuleWrapper[] = [];

    lastRightClickData!: ContextMenuData;

    modelRules: ModelRuleDto[] = [];

    @Watch('rules')
    onRules(newRules: ModelRuleDto[]) {
      this.initialize(newRules);
    }

    created() {
      this.initialize(this.rules);
    }

    getModelRuleRefName(mrwId: number) {
      return `modelRuleEditors_${mrwId}_ref`;
    }

    getModelRuleRef(mrw: ModelRuleWrapper) : ModelRuleEditorInterface {
      const refName = this.getModelRuleRefName(mrw.id);
      const mreRef = (this.$refs as any)[refName];
      if (Array.isArray(mreRef) && mreRef[0]) {
        return mreRef[0];
      }
      throw Error('ref not found for ModelRuleWrapper');
    }

    getModelRules(): ModelRuleDto[] {
      this.modelRules = [];
      for (let i = 0; i < this.mrws.length; i += 1) {
        const mrw: ModelRuleWrapper = this.mrws[i];
        const mre: ModelRuleEditorInterface = this.getModelRuleRef(mrw);
        const mr: ModelRuleDto = mre.getModelRule();
        this.modelRules.push(mr);
      }
      return [...this.modelRules];
    }

    wrapModelRules(mrs: ModelRuleDto[]): ModelRuleWrapper[] {
      return mrs.map(mr => ({
        id: nextModelRuleWrapperId(),
        modelRule: mr,
        openMetadata: false,
        emitData: false,
      }));
    }

    getEmptyWrappedModelRule(): ModelRuleWrapper {
      return this.wrapModelRules([{
        metadata: '',
        rule: '',
      }])[0];
    }

    initialize(mrs: ModelRuleDto[]) {
      this.mrws = this.wrapModelRules(mrs);
    }

    onToggleMetadata() {
      const mrw = this.mrws[this.lastRightClickData.index];
      this.mrws[this.lastRightClickData.index].openMetadata = !mrw.openMetadata;
      this.setFocus(mrw);
    }

    onOpenAllMetadata(open: boolean) {
      this.mrws.forEach(((mrw) => {
        // eslint-disable-next-line no-param-reassign
        mrw.openMetadata = open;
      }));
      this.setFocus(this.mrws[this.lastRightClickData.index]);
    }

    onInsertModelRule(isAbove: boolean) {
      let ix = this.lastRightClickData.index;
      if (!isAbove) {
        ix += 1;
      }
      const mrw = this.getEmptyWrappedModelRule();
      this.mrws.splice(ix, 0, mrw);
      this.setFocus(mrw);
    }

    onDeleteModelRule() {
      this.mrws.splice(this.lastRightClickData.index, 1);
      if (!this.mrws.length) {
        this.mrws.push(this.getEmptyWrappedModelRule());
      }
      const ix = Math.min(this.lastRightClickData.index, this.mrws.length - 1);
      this.setFocus(this.mrws[ix]);
    }

    setCurrentRightClickData(data: ContextMenuData) {
      this.lastRightClickData = data;
    }

    private setFocus(mrw: ModelRuleWrapper) {
      this.$nextTick(() => this.getModelRuleRef(mrw).focusOnRuleEditor());
    }
  }
</script>
