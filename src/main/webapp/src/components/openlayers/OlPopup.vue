<template>
  <!-- eslint-disable vue/no-v-html -->
  <div
    ref="popupRef"
    class="ol-popup"
  >
    <div v-html="html" />
  </div>
</template>

<script lang="ts">

  import { Component, Vue } from 'vue-property-decorator';
  import Map from 'ol/Map';
  import Overlay from 'ol/Overlay';
  import OlFeature, { FeatureLike } from 'ol/Feature';
  import MapBrowserEvent from 'ol/MapBrowserEvent';
  import { props as propConst } from './features';
  import { OlPopupInterface } from '@/components/openlayers/OlPopup.types';

  const popupTdCommonStyle = 'text-align: left; white-space: nowrap; font-size: .9em';

  const popupConsts = {
    // We can't use scoped styles, so in-line the styles
    tdCommonStyle: popupTdCommonStyle,
    tdStyle: `style="${popupTdCommonStyle}"`,
    tdLabelStyle: `style="${popupTdCommonStyle};font-weight: 600"`,
    trStyle: 'style="line-height: 1.1"',
  };

  const formatDecimal = (num: number) => {
    const text = num.toFixed(4);
    let i = text.length - 1;
    // noinspection StatementWithEmptyBodyJS
    for (; i >= 0 && text.charAt(i) === '0'; i -= 1) ;
    if (text.charAt(i) === '.') {
      i -= 1;
      if (i < 0) {
        return '0';
      }
    }
    return text.slice(0, i + 1);
  };

  type Display = {
    attributes: {
      state: string | null,
      county: string | null,
    }
  }

  @Component
  export default class OlPopup extends Vue implements OlPopupInterface {
    $refs!: {
      popupRef: HTMLElement,
    };

    html: string | null = null;

    overlay: Overlay | null = null;

    display: Display = {
      attributes: {
        state: null,
        county: null,
      },
    };

    onMapEvent(event: MapBrowserEvent, getValueForFeature:
      (feature: OlFeature) => number | null) {
      if (!this.overlay) {
        throw Error('addOverlay must be called before calling onMapClick');
      }

      let html = '';
      let isOurs = false;

      const featureCallback = (feature: FeatureLike) => {
        if (isOurs) {
          // We only want the first one
          return;
        }
        html += `
          <table>
            <tr ${popupConsts.trStyle}>
              <td ${popupConsts.tdLabelStyle}>State:</td>
              <td ${popupConsts.tdStyle}>${feature.get(propConst.State)}</td>
            </tr>
            <tr ${popupConsts.trStyle}>
              <td ${popupConsts.tdLabelStyle}>County:</td>
              <td ${popupConsts.tdStyle}>${feature.get(propConst.County)}</td>
            </tr>`;

        const value = getValueForFeature(feature as OlFeature);
        if (typeof value === 'number') {
          html += `
            <tr ${popupConsts.trStyle}>
              <td ${popupConsts.tdLabelStyle}>Value:</td>
              <td ${popupConsts.tdStyle}>${formatDecimal(value)}</td>
            </tr>`;
        }

        html += '</table>';
        isOurs = true;
      };

      event.map.forEachFeatureAtPixel(event.pixel, featureCallback);

      if (isOurs) {
        this.overlay.setPosition(event.coordinate);
        this.html = html;
      } else {
        this.closePopup();
        this.html = null;
      }
    }

    addOverlay(map: Map) {
      if (!this.overlay) {
        this.overlay = new Overlay({
          element: this.$refs.popupRef,
          autoPan: true,
          autoPanAnimation: {
            duration: 250,
          },
        });

        if (this.overlay) {
          map.addOverlay(this.overlay);
        }
      }
    }

    closePopup() {
      if (this.overlay && this.overlay.getPosition() !== undefined) {
        // If the position is `undefined` the overlay is hidden.
        const position: any = undefined;
        this.overlay.setPosition(position);
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
  .ol-popup {
    position: absolute;
    background-color: white;
    -webkit-filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.2));
    filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.2));
    padding: 15px;
    border-radius: 10px;
    border: 1px solid #cccccc;
    bottom: 12px;
    left: -50px;
    min-width: 280px;
  }

  .ol-popup:after, .ol-popup:before {
    top: 100%;
    border: solid transparent;
    content: " ";
    height: 0;
    width: 0;
    position: absolute;
    pointer-events: none;
  }

  .ol-popup:after {
    border-top-color: white;
    border-width: 10px;
    left: 48px;
    margin-left: -10px;
  }

  .ol-popup:before {
    border-top-color: #cccccc;
    border-width: 11px;
    left: 48px;
    margin-left: -11px;
  }

</style>
