// @flow

import Feature from 'ol/Feature';
import { Fill, Stroke, Style } from 'ol/style';

export const props = {
  State: 'ADMIN1',
  County: 'ADMIN2',
};

type StyleEntry = {
  state: string,
  county: string,
  opacity: number,
  style: Style,
}

type StyleMap = {
  [county: string]: StyleEntry
}

let styleMap: ?StyleMap;

const invalidFeatureStyle =
    new Style({
      stroke: new Stroke({
        color: 'red',
        width: 1,
      }),
      fill: new Fill({
        color: 'rgba(0, 0, 255, 0.1)',
      }),
    });

const initStyleMap = (featureCollection: Object) => {
  // Currently Opacity is just determined based on the State
  const featureQualifies = geoJsonFeature =>
    geoJsonFeature.properties[props.State]
        && geoJsonFeature.properties[props.County];

  const styleEntryArray : StyleEntry[] = [];
  const statesMap = {};
  const styleByState : { [state: string]: Style } = {};

  featureCollection.features.forEach((geoJsonFeature) => {
    if (featureQualifies(geoJsonFeature)) {
      const styleEntry : StyleEntry = {
        state: geoJsonFeature.properties[props.State],
        county: geoJsonFeature.properties[props.County],
        opacity: -1,
        style: invalidFeatureStyle,
      };
      styleEntryArray.push(styleEntry);
      statesMap[styleEntry.state] = styleEntry.state;
    } else {
      // eslint-disable-next-line no-console
      console.error(`Feature does not have both a state and a county ${JSON.stringify(geoJsonFeature)}`);
    }
  });

  const defaultStroke = new Stroke({
    color: 'blue',
    width: 1,
  });

  const opacityByState = {};

  const states = Object.keys(statesMap).sort((a, b) =>
    a.toLowerCase().localeCompare(b.toLowerCase()));

  for (let i = 0; i < states.length; i += 1) {
    const state = states[i];
    const opacity = 0.5 / (i + 1);
    opacityByState[state] = opacity;
    styleByState[states[i]] = new Style({
      stroke: defaultStroke,
      fill: new Fill({
        color: `rgba(255, 0, 0, ${opacity})`,
      }),
    });
  }

  styleMap = styleEntryArray.reduce((accum: any, styleEntry: StyleEntry) : StyleMap => {
    const { state } = styleEntry;
    // eslint-disable-next-line no-param-reassign
    accum[styleEntry.county]
        = { ...styleEntry, style: styleByState[state], opacity: opacityByState[state] };
    return accum;
  }, {});
};

const getStyleEntryForCounty = (feature: Feature) : ?StyleEntry => {
  const county: ?string = feature.get(props.County);
  return county && styleMap ? styleMap[county] : null;
};

export const getOpacityForFeature = (feature: Feature) : number => {
  const styleEntry : ?StyleEntry = getStyleEntryForCounty(feature);
  return styleEntry ? styleEntry.opacity : -1;
};

export const getStyleFunction = (featureCollection: Object) : (feature: Feature) => Style => {
  initStyleMap(featureCollection);

  return (feature: Feature) : Style => {
    const styleEntry : ?StyleEntry = getStyleEntryForCounty(feature);
    return styleEntry ? styleEntry.style : invalidFeatureStyle;
  };
};

