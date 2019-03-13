<template>
  <div v-if="currentPageIx !== -1">
    <b-card no-body header="header" style="text-align: left;">
      <div slot="header">
        <div class="d-flex justify-content-start">
          <h4 style="font-weight: bold; margin-bottom: 0;">Model Editor</h4>
        </div>
      </div>
      <b-card-body>
        <div>
          <b-button-toolbar>
            <b-button-group size="sm" class="mx-1">
              <action-button
                  type="open"
                  title="Open Model"
                  @clicked="()=>$refs.input_ref.click()">
              </action-button>
              <action-button
                  type="download"
                  title="Download Model"
                  @clicked="download">
              </action-button>
            </b-button-group>
            <b-input-group size="sm" class="w-50 mx-1" prepend="Example">
              <b-form-select
                  @input="exampleSelectionChanged"
                  v-model="exampleOptionSelected"
                  :options="exampleOptions">
              </b-form-select>
            </b-input-group>
            <b-button-group size="sm" class="mx-1">
              <action-button
                  type="undo"
                  title="Undo last edit"
                  :disabled="!pages[currentPageIx].canUndo"
                  @clicked="undo">
              </action-button>
              <action-button
                  type="redo"
                  title="Redo last edit"
                  :disabled="!pages[currentPageIx].canRedo"
                  @clicked="redo">
              </action-button>
            </b-button-group>
          </b-button-toolbar>
        </div>
        <hr/>
        <div ref="editor_ref"></div>
        <hr/>
        <div>
          <b-button-toolbar>
              <b-input-group size="sm" class="w-50 mx-1" prepend="Query">
                <b-form-select
                    v-model="pages[currentPageIx].queryOptionSelected"
                    :options="pages[currentPageIx].queryOptions">
                </b-form-select>
              </b-input-group>
              <action-button
                  type="play"
                  title="Run the query"
                  @clicked="runQuery">
              </action-button>
            <action-button
                type="broom"
                title="Sweep away query results"
                @clicked="()=> pages[currentPageIx].queryResults = []">
            </action-button>
            <span style="width: 24px"></span>
            <action-button
                type="angle-right"
                title="Next page"
                :disabled="currentPageIx + 2 > pages.length"
                @clicked="gotoPage(currentPageIx + 1)">
            </action-button>
            <span class="mt-1">
              {{currentPageIx + 1}}/{{pages.length}}
            </span>
            <action-button
                type="angle-left"
                title="Prior page"
                :disabled="currentPageIx <= 0"
                @clicked="gotoPage(currentPageIx - 1)">
            </action-button>
          </b-button-toolbar>
        </div>
        <hr />
        <div v-for="result in pages[currentPageIx].queryResults" >
          {{result}}
        </div>
        <input-text-file ref="input_ref" @change="inputFileChanged" accept=".praise"></input-text-file>
      </b-card-body>
    </b-card>
  </div>
</template>

<script>
  // @flow
  import cloneDeep from 'lodash/cloneDeep';
  import ActionButton from '@/components/ActionButton';
  import InputTextFile from '@/components/InputTextFile';
  import type { FileInfo } from '@/utils';
  import {
    fetchExamples,
    solve,
    toFormattedPageModel,
    fromFormattedPageModel,
  } from '@/components/model/dataSourceProxy';
  import { modelQueryDtoDefaults } from '@/components/model/types';
  import type {
    ModelPagesDto,
    ModelPageDto,
    ModelQueryDto,
    FormattedPageModelDto,
    ExpressionResultDto,
  } from '@/components/model/types';
  import AceModelEditor from './aceModelEditor';

  export default {
    name: 'oldModelEditor',
    components: {
      ActionButton,
      InputTextFile,
    },
    data() {
      return {
        exampleOptionSelected: -1,
        exampleOptions: [],
        examples: [],
        currentPageIx: -1,
        pages: [{
          canUndo: false,
          canRedo: false,
          model: '',
          queryOptionSelected: -1,
          queryOptions: [],
          queryResults: [],
        }],
      };
    },
    methods: {
      exampleSelectionChanged(exampleIx: number) {
        const epd: ModelPagesDto = this.examples[exampleIx];

        const getQueryOptions = (mpd: ModelPageDto) => {
          const queryOptions = [];
          for (let i = 0; i < mpd.queries.length; i += 1) {
            queryOptions.push({ value: i, text: mpd.queries[i] });
          }
          return queryOptions;
        };

        const pages = [];
        epd.pages.forEach((mpd) => {
          const qo = getQueryOptions(mpd);
          pages.push({
            canUndo: false,
            canRedo: false,
            model: mpd.model,
            queryOptions: qo,
            queryOptionSelected: qo.length ? 0 : -1,
            queryResults: [],
          });
        });

        this.pages = pages;
        this.currentPageIx = 0;
        this.editor$.setValue(pages[0].model);
        this.pages[0].canUndo = false;

        pages.forEach((p, ix) => {
          if (ix !== 0) {
            this.editor$.addSession(pages[ix].model);
          }
        });

        this.editor$.clearSelection();
        this.editor$.resetUndoManager();
      },
      setupExamples(examples: ModelPagesDto[]) {
        this.examples = examples;
        const exampleOptions = [];
        for (let i = 0; i < examples.length; i += 1) {
          exampleOptions.push({ value: i, text: examples[i].name });
        }
        this.exampleOptions = exampleOptions;
        this.exampleOptionSelected = 0;
      },
      async loadExamples() : Promise<ModelPagesDto[]> {
        try {
          return await fetchExamples();
        } catch (err) {
          // errors already logged/displayed
          return [];
        }
      },
      async inputFileChanged(filesInfo : FileInfo[]) {
        if (filesInfo.length === 0) {
          return;
        }
        const { basename, text } = filesInfo[0];
        const fpm: FormattedPageModelDto = { name: basename, text };
        let modelPagesDto : ModelPagesDto;
        try {
          modelPagesDto = await fromFormattedPageModel(fpm);
        } catch (err) {
          // errors already logged/displayed
          return;
        }

        this.createEditor();
        this.setupExamples([modelPagesDto]);
        this.exampleSelectionChanged(this.exampleOptionSelected);
        this.currentPageIx = -1;
        const that = this;
        this.$nextTick(() => { that.currentPageIx = 0; });
      },
      async runQuery() {
        const curPage = this.pages[this.currentPageIx];

        const query: ModelQueryDto = {
          model: this.editor$.getValue(),
          query: curPage.queryOptions[curPage.queryOptionSelected].text,
          numberOfInitialSamples: modelQueryDtoDefaults.numberOfInitialSamples,
          numberOfDiscreteValues: modelQueryDtoDefaults.numberOfDiscreteValues,
        };

        try {
          const removeImageData = (results) => {
            if (Array.isArray(results) && results.length > 0 && results[0].graphQueryResultDto) {
            // eslint-disable-next-line no-param-reassign
              delete results[0].graphQueryResultDto.imageData;
            }
          };
          const singleResult: ExpressionResultDto = await solve(query);
          // solve use to return an array, which is why we are converting it to an array below
          // so that the rest of the code will still work as before without further modifications
          const result = [singleResult];
          removeImageData(result);
          curPage.queryResults = [result].concat(curPage.queryResults);
        } catch (err) {
          // errors already logged/displayed
        }
      },
      gotoPage(pageIx: number) {
        this.pages[this.currentPageIx].model = this.editor$.getValue();
        this.editor$.setSession(pageIx);
        this.currentPageIx = pageIx;
      },
      async download() {
        const mpd: ModelPagesDto = cloneDeep(this.examples[this.exampleOptionSelected]);

        mpd.pages.forEach((p, ix) => {
          const { value } = this.editor$.getSessionData(ix);
          mpd.pages[ix].model = value;
        });

        let fpmd: FormattedPageModelDto;
        try {
          fpmd = await toFormattedPageModel(mpd);
        } catch (err) {
          // errors already logged/displayed
          return;
        }

        this.$$.downloadFile(fpmd.text, fpmd.name);
      },
      undo() {
        this.editor$.undo();
      },
      redo() {
        this.editor$.redo();
      },
      createEditor() {
        if (this.editor$ !== undefined) {
          this.editor$.destroy();
        }
        this.editor$ = new AceModelEditor({ mode: 'ace/mode/hogm', minLines: 10, maxLines: 30 });
        const that = this;
        this.editor$.on('change', () => {
          that.pages[that.currentPageIx].canUndo = that.editor$.canUndo();
          that.pages[that.currentPageIx].canRedo = that.editor$.canRedo();
        });
      },
    },
    async created() {
      const examples: ModelPagesDto[] = await this.loadExamples();
      if (examples.length) {
        this.setupExamples(examples);
        this.exampleSelectionChanged(this.exampleOptionSelected);
      }
    },
    mounted() {
      this.createEditor();
    },
    beforeDestroy() {
      this.editor$.destroy();
    },
    watch: {
      currentPageIx(newValue: number, oldValue: number) {
        if (oldValue === -1) {
          this.$nextTick(() => {
            this.editor$.appendToRef(this.$refs.editor_ref);
          });
        }
      },
    },
  };
</script>

<style scoped>
</style>
