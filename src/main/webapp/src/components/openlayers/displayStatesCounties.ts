// eslint-disable-next-line import/extensions,import/no-unresolved
import { Feature } from 'geojson';

type RegionMap = {
  [key: string]: string;
}

export default (features: Feature[]) => {
  const states: RegionMap = {};
  const counties: RegionMap = {};
  const countryStates: RegionMap = {};

  features.forEach((f) => {
    if (!f.properties) {
      return;
    }
    const st = f.properties.ADMIN1 as string;
    states[st] = st;
    const ct = f.properties.ADMIN2 as string;
    counties[ct] = ct;
    if (countryStates[ct] && countryStates[ct] !== st) {
      console.error(`Same county for multiple states: State: ${st} : cty: ${countryStates[ct]}, ${ct}`);
    }
    countryStates[ct] = st;
  });

  console.log(`states: \n ${JSON.stringify(Object.keys(states).sort())}\n`);
  console.log(`counties: \n ${JSON.stringify(Object.keys(counties).sort())}\n`);
  // console.log(`countryStates: \n ${JSON.stringify(Object.keys(counties).sort())}\n`);
};
