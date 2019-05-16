<template>
  <div>
    <b-card
      header-tag="header"
      style="text-align: left"
    >
      <div slot="header">
        <div class="d-flex justify-content-start">
          <h4
            class="mb-0"
            style="font-weight: bold"
          >
            {{ header }}
          </h4>
          <div class="ml-auto">
            <slot name="header" />
          </div>
        </div>
      </div>

      <div v-if="replaceTable">
        <slot name="replaceTable" />
      </div>
      <div v-else>
        <div class="d-flex justify-content-start">
          <div style="margin-top: 8px; margin-right: 20px">
            Show
          </div>
          <b-form-group>
            <div>
              <b-form-select
                v-model="perPage"
                v-tippy
                :options="optionEntryCounts"
                size="md"
                title="Select # of items to show per page"
              />
            </div>
          </b-form-group>
          <div class="p-2">
            entries
          </div>
          <div class="ml-auto">
            <div class="d-flex justify-content-start">
              <b-form-group
                :label-cols="3"
                label="Search"
              >
                <b-form-input
                  v-model.trim="filter"
                  v-tippy
                  placeholder="Type to Search"
                  size="md"
                  title="Enter text to search in table"
                />
              </b-form-group>
            </div>
          </div>
        </div>
        <slot
          :currentPage="currentPage"
          :filter="filter"
          :perPage="perPage"
          name="table"
        />
        {{ footingMessage }}
        <div class="float-right">
          <b-pagination
            v-model="currentPage"
            :per-page="perPage"
            :total-rows="totalRowsForPagination"
            size="md"
            style="margin-bottom: 0;"
          />
        </div>
      </div>
    </b-card>
  </div>
</template>

<script lang="ts">
  import { Component, Prop, Vue } from 'vue-property-decorator';

  type OptionEntry = {
    text: number;
    value: number;
  };

  @Component
  export default class CardTable extends Vue {
    @Prop({
      type: String,
      required: true,
    }) readonly header!: string;

    // Array of all items available for the table
    // This includes any items that are currently filtered-out for display
    @Prop({
      type: Array,
      required: true,
    }) readonly items!: Array<Object>;

    @Prop({ default: false }) readonly replaceTable!: boolean;

    @Prop({
      default: () => [5, 10, 15, 20, 30, 40, 50, 60],
    }) readonly perPageOptions!: Array<number>;

    @Prop({ default: 5 }) readonly perPageDefault!: number;

    optionEntryCounts: OptionEntry[] = [];

    currentPage = 1;

    perPage = -1;

    filter = '';

    filteredRows: Array<Object> = [];

    get totalRowsForPagination() {
      return this.filter ? this.filteredRows.length : this.items.length;
    }

    get footingMessage() {
      const totalRows = this.totalRowsForPagination;
      const maxPageRow = this.currentPage * this.perPage;

      const lastRowDisplayed = Math.min(maxPageRow, totalRows);

      let firstRowDisplayed;

      if (lastRowDisplayed === 0) {
        firstRowDisplayed = 0;
      } else if (lastRowDisplayed <= this.perPage) {
        firstRowDisplayed = 1;
      } else if (totalRows < maxPageRow) {
        // partially full page that is not the 1st page
        firstRowDisplayed = maxPageRow - this.perPage + 1;
      } else {
        // full page
        firstRowDisplayed = lastRowDisplayed - this.perPage + 1;
      }

      return `Showing ${firstRowDisplayed} to ${lastRowDisplayed} of ${totalRows}`;
    }

    mounted() {
      this.updatePerPageOptions();
    }

    updatePerPageOptions() {
      this.optionEntryCounts = this.perPageOptions.map(n => ({
        text: n,
        value: n,
      }));
      if (this.perPageOptions.includes(this.perPageDefault)) {
        this.perPage = this.perPageDefault;
      } else {
        [this.perPage] = this.perPageOptions;
      }
    }

    onFiltered(filteredRows: Array<Object>): void {
      this.filteredRows = filteredRows;
    }
  }

</script>
