// @ts-ignore
// noinspection TypeScriptCheckImport
import Map from 'ol/Map';
// @ts-ignore
// noinspection TypeScriptCheckImport
import MapBrowserEvent from 'ol/MapBrowserEvent';
// @ts-ignore
// noinspection TypeScriptCheckImport
import OlFeature from 'ol/Feature';
import { Vue } from 'vue-property-decorator';

export interface OlPopupInterface extends Vue {
  onMapEvent(
    event: MapBrowserEvent,
    getValueForFeature: (feature: OlFeature) => number | null
  ): void;

  addOverlay(map: Map): void;
}
