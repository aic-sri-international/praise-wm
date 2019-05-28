import Map from 'ol/Map';
import MapBrowserEvent from 'ol/MapBrowserEvent';
import OlFeature from 'ol/Feature';
import { Vue } from 'vue-property-decorator';

export interface OlPopupInterface extends Vue {
  onMapEvent(
    event: MapBrowserEvent,
    getValueForFeature: (feature: OlFeature) => number | null,
  ): void;

  addOverlay(map: Map): void;

  closePopup() : void;
}
