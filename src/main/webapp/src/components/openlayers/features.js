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
  probability?: number;
  style: Style,
}

const defaultStroke = new Stroke({
  color: 'blue',
  width: 1,
});

const inQueryStroke = new Stroke({
  color: 'red',
  width: 2,
});

// const errorStroke = new Stroke({
//   color: 'red',
//   width: 1,
// });

const defaultOpacity = 0.0;

const newFill = (r: number, g: number, b: number, opacity: number) => new Fill({
  color: `rgba(${r}, ${g}, ${b}, ${opacity})`,
});

// const invalidFeatureStyle =
//     new Style({
//       stroke: errorStroke,
//       fill: newFill(0, 0, 255, 0.1),
//     });


const defaultFeatureStyle =
    new Style({
      stroke: defaultStroke,
      fill: newFill(0, 0, 255, defaultOpacity),
    });

class FeatureCollectionHandler {
  featureCollection: Object;
  countyToStyleMap: {[county: string]: StyleEntry };
  stateToStyleMap: {[state: string]: StyleEntry };
  mapRegionNameToValue: ?{ [string]: number };
  countiesWereInput: ?boolean = undefined;

  constructor(featureCollection: Object, mapRegionNameToValue?: { [string]: number }) {
    this.featureCollection = featureCollection;
    this.mapRegionNameToValue = mapRegionNameToValue;
    this.initMaps();
  }

  setUseCountyFlag() {
    this.countiesWereInput = undefined;

    const map = this.mapRegionNameToValue;
    if (!map) {
      return;
    }
    const counties = [];
    const states = [];
    const other = [];

    Object.keys(map).forEach((name) => {
      if (this.countyToStyleMap[name]) {
        counties.push(name);
      } else if (this.stateToStyleMap[name]) {
        states.push(name);
      } else {
        other.push(name);
      }
    });

    if (counties.length) {
      this.countiesWereInput = true;
    } else if (states.length) {
      this.countiesWereInput = false;
    }
    if (other.length || (counties.length && states.length)) {
      // eslint-disable-next-line no-console
      console.error(`mapRegionNameToValue must contain names that 
      are either only counties or states: counties: ${JSON.stringify(counties)},
      states: ${JSON.stringify(states)}, other: ${JSON.stringify(other)}`);
    }
  }

  updateIfInQuery(styleEntry : StyleEntry) {
    const map = this.mapRegionNameToValue;
    if (!map) {
      return;
    }
    let value = map[styleEntry.state] || map[styleEntry.county];
    if (typeof value !== 'number') {
      return;
    }

    // eslint-disable-next-line no-param-reassign
    styleEntry.probability = value;

    if (value > 0.95) {
      value = 0.85;
    }
    const opacity = +value.toFixed(4);
    const style = new Style({
      stroke: inQueryStroke,
      fill: newFill(0, 0, 255, opacity),
    });

    // eslint-disable-next-line no-param-reassign
    styleEntry.opacity = opacity;
    // eslint-disable-next-line no-param-reassign
    styleEntry.style = style;
  }

  initMaps() {
    this.stateToStyleMap = {};
    this.countyToStyleMap = {};

    const featureQualifies = geoJsonFeature =>
      geoJsonFeature.properties[props.State]
        && geoJsonFeature.properties[props.County];


    this.featureCollection.features.forEach((geoJsonFeature) => {
      if (featureQualifies(geoJsonFeature)) {
        const styleEntry : StyleEntry = {
          state: geoJsonFeature.properties[props.State],
          county: geoJsonFeature.properties[props.County],
          opacity: defaultOpacity,
          style: defaultFeatureStyle,
        };

        this.updateIfInQuery(styleEntry);

        this.stateToStyleMap[styleEntry.state] = styleEntry;
        this.countyToStyleMap[styleEntry.county] = styleEntry;
      } else {
        // eslint-disable-next-line no-console
        console.error(`Feature does not have both a state and a county ${JSON.stringify(geoJsonFeature)}`);
      }
    });

    this.setUseCountyFlag();
  }

  getStyleEntryForCounty(feature: Feature) : ?StyleEntry {
    const county: ?string = feature.get(props.County);
    return county && this.countyToStyleMap[county]
      ? this.countyToStyleMap[county]
      : null;
  }

  getStyleEntryForState(feature: Feature) : ?StyleEntry {
    const state: ?string = feature.get(props.State);
    return state && this.stateToStyleMap[state]
      ? this.stateToStyleMap[state]
      : null;
  }

  getStyleEntryForFeature(feature: Feature) : ?StyleEntry {
    if (this.countiesWereInput !== undefined) {
      return this.getStyleEntryForCounty(feature);
    }

    return null;
  }

  getStyleForFeature(feature: Feature) : Style {
    const styleEntry: ?StyleEntry = this.getStyleEntryForFeature(feature);
    return styleEntry ? styleEntry.style : defaultFeatureStyle;
  }

  getProbabilityForFeature(feature: Feature) : Style {
    const styleEntry: ?StyleEntry = this.getStyleEntryForFeature(feature);
    return styleEntry ? styleEntry.probability : null;
  }
}

export const newFeatureCollectionHandler
    = (featureCollection: Object, mapRegionNameToValue?: { [string]: number }) =>
      new FeatureCollectionHandler(featureCollection, mapRegionNameToValue);
