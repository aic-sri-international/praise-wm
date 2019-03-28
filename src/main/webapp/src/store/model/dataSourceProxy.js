// @flow
import { http, toApiUrl } from '@/services/http';
import type {
  ModelQueryDto,
  SegmentedModelDto,
  ExpressionResultDto,
  GraphRequestDto,
  GraphRequestResultDto,
} from './types';

async function fetchSegmentedModels(): Promise<SegmentedModelDto[]> {
  let result: SegmentedModelDto[] = [];

  // $FlowFixMe
  result = await http.get(toApiUrl('segmentedModels'));
  return Promise.resolve(result);
}

async function solve(model: ModelQueryDto, init?: Object): Promise<ExpressionResultDto> {
  const result = await http.post(toApiUrl('solve'), model, init);
  return Promise.resolve(result);
}

async function fetchGraph(request: GraphRequestDto, init?: Object): Promise<GraphRequestResultDto> {
  const result = await http.post(toApiUrl('buildGraph'), request, init);
  return Promise.resolve(result);
}

async function interruptSolver() : Promise<any> {
  const result = await http.post(toApiUrl('interruptSolver'), {});
  return Promise.resolve(result);
}

export {
  fetchSegmentedModels,
  solve,
  fetchGraph,
  interruptSolver,
};
