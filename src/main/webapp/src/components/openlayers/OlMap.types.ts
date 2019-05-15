import { Vue } from 'vue-property-decorator';

export interface OlMapInterface extends Vue {
  updateMapSize: () => void;
}
