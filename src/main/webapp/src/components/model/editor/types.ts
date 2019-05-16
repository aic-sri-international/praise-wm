import { ModelRuleDto } from '@/store/model/types';
import { Vue } from 'vue-property-decorator';

export type ModelRuleWrapper = {
  id: number;
  modelRule: ModelRuleDto;
  openMetadata: boolean;
  emitData: boolean;
}

export type EditorInterface = Vue & {
  getValue: () => string;
  focus: () => void;
}

export type ModelEditorInterface = Vue & {
  getModelRules: () => ModelRuleDto[];
}

export type ModelRuleEditorInterface = Vue & {
  getModelRule: () => ModelRuleDto;
  focusOnRuleEditor: () => void;
}
