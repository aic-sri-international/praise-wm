import { http, toApiUrl } from '@/services/http';
import {
  ModelQueryDto,
  SegmentedModelDto,
  ExpressionResultDto,
  GraphRequestDto,
  GraphRequestResultDto,
} from './types';

async function fetchSegmentedModels(): Promise<SegmentedModelDto[]> {
  const result: any = await http.get(toApiUrl('segmentedModels'));
  return Promise.resolve(result);
}

async function solve(model: ModelQueryDto, init?: Object): Promise<ExpressionResultDto> {
  const result: any = await http.post(toApiUrl('solve'), model, init);
  return Promise.resolve(result);
}

async function fetchGraph(request: GraphRequestDto, init?: Object): Promise<GraphRequestResultDto> {
  const result: any = await http.post(toApiUrl('buildGraph'), request, init);
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
