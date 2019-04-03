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

async function solve(model: ModelQueryDto, init?: Object): Promise<ExpressionResultDto> {
  const path = 'solve';
  let url;

  if (alternateServer.enable) {
    url = `${alternateServer.url}/api/${path}`;
  } else {
    url = toApiUrl(path);
  }
  const result = await http.post(url, model, init);
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
  solve,
  toFormattedPageModel,
  fromFormattedPageModel,
};