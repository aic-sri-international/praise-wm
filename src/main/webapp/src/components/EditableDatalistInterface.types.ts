import { Vue } from 'vue-property-decorator';

export interface EditableDatalistInterface extends Vue {
  getCurrentOption: () => string;
  addCurrentEntry: () => void;
  showList: (show: boolean) => void;
}
