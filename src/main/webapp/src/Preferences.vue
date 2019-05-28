<template>
  <div v-on-click-outside="onClickOutside">
    <b-container class="container panelShadowBox">
      <div class="d-flex justify-content-start pt-2">
        <h4>Preferences</h4>
      </div>
      <hr>
      <b-form-group label="Display Region Popover On Map">
        <b-form-radio
          v-model="mapPopupClickOrHover"
          style="margin-left: -80px"
          value="hover"
          @change="onMapPopupClickOrHoverChange"
        >
          On mouse over
        </b-form-radio>
        <b-form-radio
          v-model="mapPopupClickOrHover"
          style="margin-left: -80px"
          value="click"
          @change="onMapPopupClickOrHoverChange"
        >
          On mouse click
        </b-form-radio>
      </b-form-group>
      <hr>
    </b-container>
  </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import { VuexPreferencesState } from '@/store/preferences/types';
  import { PREFERENCES_MODULE_NAME } from '@/store/preferences/constants';

  const preferencesModule = namespace(PREFERENCES_MODULE_NAME);

  type ClickOrHover = 'click' | 'hover';

  @Component
  export default class Preferences extends Vue {
    mapPopupClickOrHover: ClickOrHover = 'hover';

    @preferencesModule.State showMapPopupOnMouseOver!: VuexPreferencesState['showMapPopupOnMouseOver'];

    @preferencesModule.Mutation setMapPopupOnMouseOver!: (show: boolean) => void;

    @preferencesModule.Mutation setShowPreferences!: (show: boolean) => void;


    mounted() {
      this.mapPopupClickOrHover = this.showMapPopupOnMouseOver ? 'hover' : 'click';
    }

    onMapPopupClickOrHoverChange(which: ClickOrHover) {
      this.setMapPopupOnMouseOver(which === 'hover');
    }

    onClickOutside() {
      this.setShowPreferences(false);
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    background-color: white;
  }

</style>
