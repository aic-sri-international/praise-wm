// @flow
import { http, toApiUrl } from '@/services/http';
import type {
  ModelPagesDto,
  ModelQueryDto,
  FormattedPageModelDto,
  SegmentedModelDto,
  ExpressionResultDto,
  GraphRequestDto,
  GraphRequestResultDto,
} from './types';

export const alternateServer = {
  enable: false,
  url: 'http://localhost:7654',
};

async function fetchExamples(): Promise<ModelPagesDto[]> {
  let result: ModelPagesDto[] = [];

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

async function solve(model: ModelQueryDto): Promise<ExpressionResultDto> {
  const path = 'solve';
  let url;

  if (alternateServer.enable) {
    url = `${alternateServer.url}/api/${path}`;
  } else {
    url = toApiUrl(path);
  }
  const result = await http.post(url, model);
  return Promise.resolve(result);
}

async function fetchGraph(request: GraphRequestDto): Promise<GraphRequestResultDto> {
  const result = await http.post(toApiUrl('buildGraph'), request);
  return Promise.resolve(result);
}

async function interruptSolver() {
  const result = await http.post(toApiUrl('interruptSolver'), {});
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
  fetchGraph,
  interruptSolver,
  toFormattedPageModel,
  fromFormattedPageModel,
};
