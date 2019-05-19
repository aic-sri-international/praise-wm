<template>
  <div style="width: 100%">
    <b-input-group
      :prepend="label"
      size="sm"
    >
      <input
        ref="inputRef"
        v-model.lazy.trim="currentItem"
        :class="{ disabledBackground: disabled }"
        :disabled="disabled"
        :placeholder="placeholder"
        autocomplete="off"
        class="form-control"
        type="text"
        @contextmenu.prevent="$refs.ctxmenuRef.open($event, { currentItem })"
      >
      <b-input-group-append>
        <b-btn
          variant="outline-secondary"
          @click.stop="displayList = !displayList"
        >
          <i class="fas fa-caret-down" />
        </b-btn>
      </b-input-group-append>
    </b-input-group>
    <b-form-select
      v-if="displayList"
      v-model="modelOptionSelected"
      :disabled="disabled"
      :options="modelOptions"
      multiple
      size="sm"
      @click.native="modelOptionChange"
    />
    <context-menu
      id="context-menu"
      ref="ctxmenuRef"
      @ctx-open="setCurrentRightClickData"
    >
      <div>
        <li
          :class="{ disabled: isFieldEmpty}"
          class="ctx-item"
          @click="onMenuClearField"
        >
          Clear field
        </li>
        <li
          :class="{ disabled: isFieldEmpty || !isNewEntry }"
          class="ctx-item"
          @click="onMenuAddToList"
        >
          Add to list
        </li>
        <li
          :class="{ disabled: isFieldEmpty || isNewEntry }"
          class="ctx-item"
          @click="onMenuRemoveFromList"
        >
          Remove from list
        </li>
      </div>
    </context-menu>
  </div>
</template>


<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';

  import ContextMenu from 'vue-context-menu';
  import { EditableDatalistInterface } from '@/components/EditableDatalistInterface.types';

  type ModelOption = {
    value: string;
    text: string;
  }

  @Component({
    components: {
      ContextMenu,
    },
  })
  export default class EditableDatalist extends Vue implements EditableDatalistInterface {
    @Prop(String) readonly label?: string;

    @Prop(String) readonly placeholder?: string;

    @Prop({
      type: Array,
      required: true,
    }) readonly options!: Array<string>;

    @Prop({ default: false }) readonly disabled!: boolean;

    $refs!: {
      inputRef: HTMLInputElement
    };

    currentItem = '';

    items: string[] = [];

    lastRightClickData = {};

    displayList = false;

    modelOptionSelected: string[] = [];

    modelOptions: ModelOption[] = [];

    get isFieldEmpty() {
      return this.currentItem === '';
    }

    get isNewEntry() {
      return this.modelOptions.findIndex(e => e.value === this.currentItem) === -1;
    }

    @Watch('currentItem')
    onItemChanged(newValue: string) {
      this.$emit('currentEntryChanged', newValue);
    }

    @Watch('options')
    onOptionsChanged() {
      this.optionsChanged();
    }

    created() {
      this.optionsChanged();
    }

    sendOptionsChangedEvent() {
      this.$emit(
        'optionsChanged',
        this.modelOptions.reduce((accum: string[], cur) => {
          accum.push(cur.text);
          return accum;
        }, []),
      );
    }

    getCurrentOption() {
      return this.currentItem;
    }

    showList(show: boolean) {
      this.displayList = show;
    }

    addCurrentEntry() {
      if (!this.isFieldEmpty && this.isNewEntry) {
        this.modelOptions = [
          {
            value: this.currentItem,
            text: this.currentItem,
          }, ...this.modelOptions,
        ];
        this.modelOptionSelected = [this.currentItem];
        this.sendOptionsChangedEvent();
      }
    }

    modelOptionChange(event: Event) {
      if (event.target) {
        const selection = (event.target as HTMLInputElement).value;
        if (selection) {
          this.modelOptionSelected = [selection];
          this.currentItem = selection;
          this.displayList = false;
        }
      }
    }

    optionsChanged() {
      this.items = [...this.options];
      this.modelOptions = this.options.map(text => ({
        text,
        value: text,
      }));
      this.currentItem = this.options.length ? this.options[0] : '';
    }

    setCurrentRightClickData(data: Object) {
      this.lastRightClickData = data;
    }

    onMenuClearField() {
      this.currentItem = '';
      this.$refs.inputRef.focus();
    }

    onMenuAddToList() {
      this.addCurrentEntry();
      this.$refs.inputRef.focus();
    }

    onMenuRemoveFromList() {
      const index = this.modelOptions.findIndex(e => e.value === this.currentItem);
      if (index > -1) {
        this.modelOptions.splice(index, 1);
        this.currentItem = '';
        this.sendOptionsChangedEvent();
      }
      this.$refs.inputRef.focus();
    }

    getAllOptions(): string[] {
      const queries: string[] = this.modelOptions.reduce((accum: string[], value: ModelOption):
      string[] => [...accum, value.text], []);

      if (this.currentItem) {
        const ix = queries.indexOf(this.currentItem);
        if (ix !== -1) {
          queries.splice(ix, 1);
          // Make sure that the current query is at the top of the list
          queries.unshift(this.currentItem);
        }
      }
      return queries;
    }
  }
</script>
