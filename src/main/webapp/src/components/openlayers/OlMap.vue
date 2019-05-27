<template>
  <div>
    <div
      ref="mapRef"
      :style="mapStyle"
    />
    <ol-popup
      ref="olPopupRef"
      @clicked-non-feature="$emit('closeMap')"
    />
  </div>
</template>

<script lang="ts">
  import {
    Component, Prop, Vue, Watch,
  } from 'vue-property-decorator';
  import getCentroid from '@turf/centroid';
  import OlFeature, { FeatureLike } from 'ol/Feature';
  import Map from 'ol/Map';
  import MapBrowserEvent from 'ol/MapBrowserEvent';
  import View from 'ol/View';
  import { transform } from 'ol/proj';
  import GeoJSON from 'ol/format/GeoJSON';
  import { Tile as TileLayer, Vector as VectorLayer } from 'ol/layer';
  import { OSM, Vector as VectorSource } from 'ol/source';
  // eslint-disable-next-line import/extensions,import/no-unresolved
  import { FeatureCollection } from 'geojson';
  // eslint-disable-next-line
  import { namespace } from 'vuex-class';
  import { Style } from 'ol/style';
  import { Control } from 'ol/control';
  import OlPopup from './OlPopup.vue';
  import { FeatureCollectionHandler, newFeatureCollectionHandler, defaultFeatureStyle } from './features';
  import { OlPopupInterface } from '@/components/openlayers/OlPopup.types';
  import { OlMapInterface } from '@/components/openlayers/OlMap.types';
  import { FEATURE_COLLECTION_MODULE_NAME } from '@/store/feature_collection/constants';
  import { VuexFeatureCollectionState } from '@/store/feature_collection/types';

  const srcProjection = 'EPSG:4326';
  const destProjection = 'EPSG:3857';

  const featureCollectionModule = namespace(FEATURE_COLLECTION_MODULE_NAME);

  @Component({
    components: {
      OlPopup,
    },
  })

  export default class OlMap extends Vue implements OlMapInterface {
    @Prop(Object) readonly mapRegionNameToValue?: { [key: string]: number };

    @Prop({ default: 74 }) readonly heightOffset!: number;

    $refs!: {
      mapRef: HTMLElement,
      olPopupRef: OlPopupInterface,
    };

    map: Map | null = null;

    featureHandler: FeatureCollectionHandler | null = null;

    @featureCollectionModule.State
    currentFeatureCollection!: VuexFeatureCollectionState['currentFeatureCollection'];

    @featureCollectionModule.Action initFeatureCollectionState!: () => Promise<any>;

    get mapStyle() {
      return { width: '100%', height: `calc(100vh - ${this.heightOffset}px)` };
    }

    @Watch('mapRegionNameToValue')
    onMapRegionNameToValue() {
      if (!this.currentFeatureCollection) {
        return;
      }
      this.featureHandler = newFeatureCollectionHandler(
        this.currentFeatureCollection,
        this.mapRegionNameToValue,
      );
      // Force the view to redraw the features; replace if there's a better way to do it
      if (this.map) {
        const view: View = this.map.getView();
        const oldZoom = view.getZoom();
        // Small enough so that it probably won't be noticed
        view.setZoom(oldZoom + 0.0000001);
        // Reset it
        setTimeout(() => {
          view.setZoom(oldZoom);
        }, 50);
      }
    }

    async mounted() {
      if (!this.currentFeatureCollection) {
        await this.initFeatureCollectionState();
        if (!this.currentFeatureCollection) {
          return;
        }
      }

      window.addEventListener('resize', this.updateMapSize);
      this.featureHandler = newFeatureCollectionHandler(
        this.currentFeatureCollection,
        this.mapRegionNameToValue,
      );
      const geoJson: GeoJSON = new GeoJSON();
      const geoJsonFeatures = geoJson.readFeatures(
        this.currentFeatureCollection,
        { dataProjection: srcProjection, featureProjection: destProjection },
      );
      const centroid = getCentroid(this.currentFeatureCollection);
      const center = transform(centroid.geometry.coordinates, srcProjection, destProjection);

      const vectorSource = new VectorSource({
        features: geoJsonFeatures,
      });

      const getStyle = (feature: FeatureLike, param: any) : Style => {
        if (!this.featureHandler) {
          return defaultFeatureStyle;
        }
        return this.featureHandler.getStyleForFeature(feature as OlFeature);
      };

      const vectorLayer = new VectorLayer({
        source: vectorSource,
        style: getStyle,
      });

      const controls: Control[] = [];
      this.map = new Map({
        layers: [
          new TileLayer({
            source: new OSM(),
          }),
          vectorLayer,
        ],
        target: this.$refs.mapRef,
        view: new View({
          center,
          zoom: 6,
        }),
        controls, // we do not want any map controls, this will remove the defaults
      });

      const onMapEvent = (event: MapBrowserEvent) => {
        const getValueForFeature = (feature: OlFeature) => {
          if (!this.featureHandler) {
            return null;
          }
          return this.featureHandler.getValueForFeature(feature);
        };
        this.$refs.olPopupRef.onMapEvent(event, getValueForFeature);
      };
      this.map.on('pointermove', onMapEvent);
      this.$refs.olPopupRef.addOverlay(this.map);
    }

    beforeDestroy() {
      window.removeEventListener('resize', this.updateMapSize);
      if (this.map) {
        this.map.un('pointermove', () => {});
        // param should be: HTMLElement|string|undefined
        const noTarget: any = undefined;
        this.map.setTarget(noTarget);
      }
    }

    updateMapSize() {
      if (this.map) {
        this.map.updateSize();
      }
    }
  }
</script>
