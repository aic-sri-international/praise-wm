<template>
  <div style="width: 100%">
    <b-input-group size="sm" :prepend="label">
      <input v-model.trim="currentItem"
             ref="input_ref"
             @contextmenu.prevent="$refs.ctxmenu_ref.open($event, { currentItem })"
             type="text"
             autocomplete="off"
             class="form-control"
             :placeholder="placeholder"/>
      <b-input-group-append>
        <b-btn variant="outline-secondary" @click.stop="displayList = !displayList">
          <i class="fas fa-caret-down"></i>
        </b-btn>
      </b-input-group-append>
    </b-input-group>
    <b-form-select
        v-if="displayList"
        size="sm"
        v-model="modelOptionSelected"
        @click.native="modelOptionChange"
        multiple
        :options="modelOptions">
    </b-form-select>
    <context-menu id="context-menu" ref="ctxmenu_ref" @ctx-open="setCurrentRightClickData">
      <div>
        <li @click="onMenuClearField"
            :class="{ disabled: isFieldEmpty}"
            class="ctx-item">Clear field
        </li>
        <li @click="onMenuAddToList"
            :class="{ disabled: isFieldEmpty || !isNewEntry }"
            class="ctx-item" >Add to list
        </li>
        <li @click="onMenuRemoveFromList"
            :class="{ disabled: isFieldEmpty || isNewEntry }"
            class="ctx-item" >Remove from list
        </li>
      </div>
    </context-menu>
  </div>
</template>

<script>
  import ContextMenu from 'vue-context-menu';

  export default {
    name: 'EditableDatalist',
    components: {
      ContextMenu,
    },
    props: {
      label: {
        type: String,
        required: false,
      },
      placeholder: {
        type: String,
      },
      options: {
        type: Array,
        required: true,
      },
    },
    data() {
      return {
        currentItem: '',
        items: [],
        lastRightClickData: {},
        displayList: false,
        modelOptionSelected: [],
        modelOptions: [],
      };
    },
    computed: {
      isFieldEmpty() {
        return this.currentItem === '';
      },
      isNewEntry() {
        return this.modelOptions.findIndex(e => e.value === this.currentItem) === -1;
      },
    },
    methods: {
      getCurrentOption() {
        return this.currentItem;
      },
      showList(which: boolean) {
        this.displayList = which;
      },
      addCurrentEntry() {
        if (!this.isFieldEmpty && this.isNewEntry) {
          this.modelOptions
              = [{ value: this.currentItem, text: this.currentItem }, ...this.modelOptions];
          this.modelOptionSelected = [this.currentItem];
        }
      },
      modelOptionChange(evt) {
        const selection = evt.target.value;
        if (selection) {
          this.modelOptionSelected = [selection];
          this.currentItem = selection;
          this.displayList = false;
        }
      },
      optionsChanged() {
        this.items = [...this.options];
        this.modelOptions = this.options.map(text => ({ text, value: text }));
        this.currentItem = this.options.length ? this.options[0] : '';
      },
      setCurrentRightClickData(data) {
        this.lastRightClickData = data;
      },
      onMenuClearField() {
        this.currentItem = '';
        this.$refs.input_ref.focus();
      },
      onMenuAddToList() {
        this.addCurrentEntry();
        this.$refs.input_ref.focus();
      },
      onMenuRemoveFromList() {
        const index = this.modelOptions.findIndex(e => e.value === this.currentItem);
        if (index > -1) {
          this.modelOptions.splice(index, 1);
          this.currentItem = '';
        }
        this.$refs.input_ref.focus();
      },
    },
    watch: {
      options() {
        this.optionsChanged();
      },
    },
    created() {
      this.optionsChanged();
    },
  };
</script>

<style scoped>
</style>