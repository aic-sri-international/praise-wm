<template>
  <div>
    <b-card header-tag="header" style="text-align: left">
      <div slot="header">
        <div class="d-flex justify-content-start">
          <h4 style="font-weight: bold" class="mb-0">{{header}}</h4>
          <div class="ml-auto">
            <slot name="header"></slot>
          </div>
        </div>
      </div>

      <div v-if="replaceTable">
        <slot name="replaceTable">
        </slot>
      </div>
      <div v-else>
        <div class="d-flex justify-content-start">
          <div style="margin-top: 8px; margin-right: 20px">
            Show
          </div>
          <b-form-group>
            <div>
              <b-form-select size="md" :options="[
                  {text:5,value:5},
                  {text:10,value:10},
                  {text:15,value:15},
                  {text:20,value:20},
                  {text:30,value:30},
                  {text:40,value:40},
                  {text:50,value:50},
                  {text:60,value:60}
                  ]"
                  v-model="perPage">
              </b-form-select>
            </div>
          </b-form-group>
          <div class="p-2">entries</div>
          <div class="ml-auto">
            <div class="d-flex justify-content-start">
              <b-form-group horizontal label="Search" :label-cols="4">
                <b-form-input size="md" v-model="filter"
                              placeholder="Type to Search"></b-form-input>
              </b-form-group>
            </div>
          </div>
        </div>
        <slot name="table"
              :perPage="perPage"
              :currentPage="currentPage"
              :filter="filter">
        </slot>
        {{footingMessage}}
        <div class="float-right">
          <b-pagination size="md" style="margin-bottom: 0;"
                        :total-rows="totalRows"
                        :per-page="perPage"
                        v-model="currentPage">
          </b-pagination>
        </div>
      </div>
    </b-card>
  </div>
</template>

<script>
  // @flow

  export default {
    name: 'cardTable',
    props: {
      header: {
        type: String,
        required: true,
      },
      vmodel: {
        type: Array,
        required: true,
      },
      replaceTable: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        currentPage: 1,
        showReportModal: false,
        perPage: 5,
        filter: null,
        filteredRows: [],
      };
    },
    methods: {
      onFiltered(filteredRows : Array<Object>) : void {
        this.filteredRows = filteredRows;
      },
      async onReportHidden() {
        this.showReportModal = false;
      },
    },
    computed: {
      totalRows() {
        return this.filteredRows.length;
      },
      footingMessage() {
        const totalRows = this.filteredRows.length;
        const numRowsDisplayed = this.vmodel ? this.vmodel.length : 0;

        const lastRowDisplayed = numRowsDisplayed === 0 ? 0
          : Math.min(
            this.currentPage * this.perPage,
            Math.max(numRowsDisplayed, totalRows),
          );

        let firstRowDisplayed = numRowsDisplayed === 0 ? 0 :
          Math.max(lastRowDisplayed - numRowsDisplayed, 1);

        if (this.currentPage > 1 && firstRowDisplayed > 1
            && firstRowDisplayed < lastRowDisplayed) {
          firstRowDisplayed += 1;
        }

        return `Showing ${firstRowDisplayed} to ${lastRowDisplayed} of ${totalRows}`;
      },
    },
  };

</script>
