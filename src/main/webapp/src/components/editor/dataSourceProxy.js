// @flow
// import isMock from '@/dataConfig';
import { http, toApiUrl } from '@/services/http';
import type {
  ModelPagesDto,
  ModelQueryDto,
  FormattedPageModelDto,
  SegmentedModelDto,
  ExpressionResultDto,
} from './types';

async function fetchExamples(): Promise<ModelPagesDto[]> {
  let result: ModelPagesDto[] = [];

  // if (isMock.editor) {
  //   try {
  //     result = getExamples();
  //     return Promise.resolve(result);
  //   } catch (err) {
  //     // eslint-disable-next-line no-console
  //     console.error(err);
  //     return Promise.reject();
  //   }
  // }

  // $FlowFixMe
  result = await http.get(toApiUrl('examples'));
  return Promise.resolve(result);
}

async function fetchSegmentedModels(): Promise<SegmentedModelDto[]> {
  let result: SegmentedModelDto[] = [];

  // $FlowFixMe
  result = await http.get(toApiUrl('segmentedModels'));
  return Promise.resolve(result);
}

async function solve(model: ModelQueryDto): Promise<ExpressionResultDto[]> {
  const result = await http.post(toApiUrl('solve'), model);
  return Promise.resolve(result);
}

async function toFormattedPageModel(modelPages: ModelPagesDto): Promise<FormattedPageModelDto> {
  const result = await http.post(toApiUrl('formatModelPages'), modelPages);
  return Promise.resolve(result);
}

async function fromFormattedPageModel(formattedModel: FormattedPageModelDto)
    : Promise<ModelPagesDto> {
  const result = await http.post(toApiUrl('unformatModelPages'), formattedModel);
  return Promise.resolve(result);
}

export {
  fetchExamples,
  fetchSegmentedModels,
  solve,
  toFormattedPageModel,
  fromFormattedPageModel,
};
