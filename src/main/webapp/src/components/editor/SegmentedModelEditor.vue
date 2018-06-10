<template>
  <div>
    <div>
      <b-button-toolbar>
        <b-button-group size="sm" class="mx-1">
          <action-button
              type="open"
              title="Open Model"
              @clicked="()=>$refs.input_ref.click()">
          </action-button>
          <!--<action-button-->
          <!--type="download"-->
          <!--title="Download Model"-->
          <!--@clicked="download">-->
          <!--</action-button>-->
        </b-button-group>
        <!--<b-input-group size="sm" class="w-50 mx-1" prepend="Model">-->
        <!--<b-form-select-->
        <!--@input="modelSelectionChanged"-->
        <!--v-model="modelOptionSelected"-->
        <!--:options="modelOptions">-->
        <!--</b-form-select>-->
        <!--</b-input-group>-->
      </b-button-toolbar>
    </div>
    <hr/>
    <div></div>
    <div v-for="(mrw, index) in mrws"
         @contextmenu.prevent="$refs.ctxmenu_ref.open($event, { index: index })">
      <model-rule-editor :model-rule-wrapper="mrw"></model-rule-editor>
    </div>

    <input-text-file ref="input_ref" @change="inputFileChanged" accept=".json"></input-text-file>

    <context-menu id="context-menu" ref="ctxmenu_ref" @ctx-open="setCurrentRightClickData">
      <li @click="onToggleMetadata">Toggle Metadata</li>
      <li @click="onToggleAllMetadata">Toggle All Metadata</li>
      <!--<li @click="onInsertAbove">Insert Rule Above</li>-->
      <!--<li @click="onInsertBelow">Insert Rule Below</li>-->
      <!--<li @click="onDelete">Delete Rule</li>-->
      <li>Insert Rule Above</li>
      <li>Insert Rule Below</li>
      <li>Delete Rule</li>
    </context-menu>
  </div>
</template>

<script>
  import ContextMenu from 'vue-context-menu';
  import ActionButton from '@/components/ActionButton';
  import InputTextFile from '@/components/InputTextFile';
  import type { FileInfo } from '@/utils';
  import type { SegmentedModel } from './types';
  import ModelRuleEditor from './ModelRuleEditor';

  export default {
    name: 'SegmentedModelEditor',
    components: {
      ActionButton,
      InputTextFile,
      ContextMenu,
      ModelRuleEditor,
    },
    data() {
      return {
        mrws: [],
        lastRightClickData: {},
      };
    },
    methods: {
      async inputFileChanged(filesInfo: FileInfo[]) {
        if (filesInfo.length === 0) {
          return;
        }
        // const { basename, jsonString } = filesInfo[0];
        const { text } = filesInfo[0];
        const segmentedModel: SegmentedModel = JSON.parse(text);

        this.mrws = segmentedModel.rules.map(mr => ({
          modelRule: mr,
          toggleMetadata: false,
          emitData: false,
        }));
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
      setCurrentRightClickData(data) {
        this.lastRightClickData = data;
        // console.log(data);
      },
    },
    created() {
      this.mrws.push({
        modelRule: {
          metadata: 'metadata for rule 1  goes here',
          rule: 'variables for rule 1 along with its definition goes here',
        },
        toggleMetadata: false,
        emitData: false,
      });
      this.mrws.push({
        modelRule: {
          metadata: 'metadata for rule 2  goes here',
          rule: 'variables for rule 2 along with its definition goes here',
        },
        toggleMetadata: false,
        emitData: false,
      });
      this.mrws.push({
        modelRule: {
          metadata: 'metadata for rule 3  goes here',
          rule: 'variables for rule 3 along with its definition goes here',
        },
        toggleMetadata: false,
        emitData: false,
      });
    },
  };
</script>

<style scoped>
  #context-menu li:hover, li:focus {
    color: inherit;
    background-color: #e9e9e9;
    text-decoration: none;
    transition: all 0.3s;
    cursor: pointer;
  }
</style>