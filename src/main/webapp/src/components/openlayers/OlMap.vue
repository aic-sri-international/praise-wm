<template>
  <div>
    <div class="ol-map" ref="map_ref"></div>
    <ol-popup ref="ol_popup_ref" @clicked-non-feature="$emit('closeMap')"></ol-popup>
  </div>
</template>

<script>
  // @flow
  import getCentroid from '@turf/centroid';
  import Map from 'ol/Map';
  import Collection from 'ol/Collection';
  import View from 'ol/View';
  import { transform } from 'ol/proj';
  import GeoJSON from 'ol/format/GeoJSON';
  import { Tile as TileLayer, Vector as VectorLayer } from 'ol/layer';
  import { OSM, Vector as VectorSource } from 'ol/source';
  import OlPopup from './OlPopup';
  import featureCollection from './SS_Admin2_2011.4326';
  import { getStyleFunction } from './features';

  const srcProjection = 'EPSG:4326';
  const destProjection = 'EPSG:3857';


  export default {
    name: 'OlMap',
    components: {
      OlPopup,
    },
    data() {
      return {
        map: null,
        featureCollection: null,
      };
    },
    methods: {
      updateMapSize() {
        if (this.map) {
          this.map.updateSize();
        }
      },
      getPolygon() {

      },
    },
    mounted() {
      window.addEventListener('resize', this.updateMapSize);

      this.featureCollection = featureCollection;
      const geoJson: GeoJSON = new GeoJSON();
      const geoJsonFeatures = geoJson.readFeatures(
        featureCollection,
        { dataProjection: srcProjection, featureProjection: destProjection },
      );
      const centroid = getCentroid(featureCollection);
      const center = transform(centroid.geometry.coordinates, srcProjection, destProjection);

      const vectorSource = new VectorSource({
        features: geoJsonFeatures,
      });

      const vectorLayer = new VectorLayer({
        source: vectorSource,
        style: getStyleFunction(featureCollection),
      });

      const controls = new Collection();
      this.map = new Map({
        layers: [
          new TileLayer({
            source: new OSM(),
          }),
          vectorLayer,
        ],
        target: this.$refs.map_ref,
        view: new View({
          center,
          zoom: 6,
        }),
        controls, // we do not want any map controls, this will remove the defaults
      });
      this.map.on('singleclick', this.$refs.ol_popup_ref.onMapClick);
      this.$refs.ol_popup_ref.addOverlay(this.map);
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.updateMapSize);
      this.map.un('singleclick');
      this.map.setTarget(undefined);
    },
  };
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .ol-map {
    height: 400px;
    width: 100%;
  }
</style>
