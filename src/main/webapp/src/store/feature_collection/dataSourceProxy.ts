import { http, toApiUrl } from '@/services/http';

export async function fetchFeatureCollectionNames(): Promise<string[]> {
  const result: any = await http.get(toApiUrl('featureCollectionNames'));
  return Promise.resolve(result);
}

export async function fetchFeatureCollection(name: string): Promise<Object> {
  const result: any = await http.get(toApiUrl(`featureCollection/${name}`));
  return Promise.resolve(result);
}
