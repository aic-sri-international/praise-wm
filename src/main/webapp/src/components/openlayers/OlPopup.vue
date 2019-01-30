<template>
  <div class="ol-popup" ref="popup_ref">
    <a @click="closePopup()" class="ol-popup-closer" href="#" ref="popup_closer_ref"></a>
    <div v-html="html"></div>
  </div>
</template>

<script>
  // @flow
  import Map from 'ol/Map';
  import Overlay from 'ol/Overlay';
  import Feature from 'ol/Feature';
  import MapBrowserEvent from 'ol/MapBrowserEvent';
  import { props as propConst } from './features';

  const popupTdCommonStyle = 'text-align: left; white-space: nowrap; font-size: .9em';
  const popupConsts = {
    // We can't use scoped styles, so in-line the styles
    tdCommonStyle: popupTdCommonStyle,
    tdStyle: `style="${popupTdCommonStyle}"`,
    tdLabelStyle: `style="${popupTdCommonStyle};font-weight: 600"`,
    trStyle: 'style="line-height: 1.1"',
  };

  export default {
    name: 'OlPopup',
    data() {
      return {
        html: null,
        overlay: null,
        display: {
          attributes: {
            state: null,
            county: null,
          },
        },
      };
    },
    methods: {
      onMapEvent(event: MapBrowserEvent, getOpacityForFeature: (feature: Feature) => number) {
        if (!this.overlay) {
          throw Error('addOverlay must be called before calling onMapClick');
        }

        let html = '';
        let isOurs = false;


        event.map.forEachFeatureAtPixel(event.pixel, (feature: Feature) => {
          if (html) {
            html += '<hr>';
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

          const probability = getOpacityForFeature(feature);
          if (typeof probability === 'number') {
            html += `
            <tr ${popupConsts.trStyle}>
              <td ${popupConsts.tdLabelStyle}>Probability:</td>
              <td ${popupConsts.tdStyle}>${probability}</td>
            </tr>`;
          }

          html += '</table>';
          isOurs = true;
        });

        if (isOurs) {
          this.overlay.setPosition(event.coordinate);
          this.html = html;
        } else {
          this.closePopup();
          this.html = null;
          console.log('mouse pointer moved');
        }
      },
      addOverlay(map: Map) {
        if (!this.overlay) {
          this.overlay = new Overlay({
            element: this.$refs.popup_ref,
            autoPan: true,
            autoPanAnimation: {
              duration: 250,
            },
          });
          map.addOverlay(this.overlay);
        }
      },
      closePopup() {
        if (this.overlay && this.overlay.getPosition() !== undefined) {
          this.overlay.setPosition(undefined);
        }
      },
    },
  };
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
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

  .ol-popup-closer {
    text-decoration: none;
    position: absolute;
    top: 2px;
    right: 8px;
  }

  .ol-popup-closer:after {
    content: "✖";
  }
</style>
