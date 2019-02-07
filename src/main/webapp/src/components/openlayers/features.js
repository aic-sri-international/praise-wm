// @flow

import chroma from 'chroma-js';
import round from 'lodash/round';
import Feature from 'ol/Feature';
import { Fill, Stroke, Style } from 'ol/style';

export const props = {
  State: 'ADMIN1',
  County: 'ADMIN2',
};

type StyleEntry = {
  state: string,
  county: string,
  value?: number;
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

const newFill = (r: number, g: number, b: number, opacity: number) => new Fill({
  color: `rgba(${r}, ${g}, ${b}, ${opacity})`,
});

const defaultFeatureStyle =
    new Style({
      stroke: defaultStroke,
      fill: newFill(0, 0, 0, 0),
    });

class FeatureCollectionHandler {
  featureCollection: Object;
  countyToStyleMap: {[county: string]: StyleEntry };
  stateToStyleMap: {[state: string]: StyleEntry };
  mapRegionNameToValue: ?{ [string]: number };
  regionToRgba: ? { [string]: number[] };
  countiesWereInput: ?boolean = undefined;

  constructor(featureCollection: Object, mapRegionNameToValue?: { [string]: number }) {
    this.featureCollection = featureCollection;

    // Round all values to 4 digits of precision we will be creating
    // a map of
    this.mapRegionNameToValue = mapRegionNameToValue;
    this.initRgba();
    this.initMaps();
  }

  initRgba() {
    if (!this.mapRegionNameToValue) {
      return;
    }

    // Round all values to 4 digits of precision to minimize unique values
    const roundedMap = Object.entries(this.mapRegionNameToValue).reduce((accum, entry) => {
      const [region, value] = entry;
      // eslint-disable-next-line no-param-reassign
      accum[region] = round(value, 4);
      return accum;
    }, {});

    // eslint-disable-next-line
    const valArray: number[] = Object.values(roundedMap);

    // Sort entries by value in ascending order returning array of arrays
    // eslint-disable-next-line
    const sortedMapAsArrays: [] = Object.entries(roundedMap).sort((a, b) => {
      // eslint-disable-next-line
      const v1: number = a[1];
      // eslint-disable-next-line
      const v2: number = b[1];
      if (v1 > v2) {
        return 1;
      }
      if (v1 < v2) {
        return -1;
      }
      return 0;
    });

    // We want white to always represent a zero value and no other value.
    // So we need to know if we already have an entry with a zero value
    // before we create the hexArray.
    let includesZeroValue: boolean = false;

    // #of unique values
    const nonDupCount
        = sortedMapAsArrays.reduce((accum: number, regionValue: [], ix: number) => {
          // eslint-disable-next-line
          const val = regionValue[1];

          if (val === 0) {
            includesZeroValue = true;
          }
          if (ix > 0) {
            const priorRegionValue = sortedMapAsArrays[ix - 1];
            const priorVal = priorRegionValue[1];
            if (val === priorVal) {
              return accum;
            }
          }

          return accum + 1;
        }, 0);

    const computeNumberOfColors = () : number => {
      let numColors = nonDupCount;

      if (nonDupCount === 1 && valArray[0] === 0) {
        // The chroma.scale method we use will not include white if ask
        // it to only create 1 color.
        numColors += 1;
      } else if (!includesZeroValue) {
        numColors += 1;
      }

      return numColors;
    };

    const numberOfColors: number = computeNumberOfColors();
    const hexArray: string[]
        = chroma.scale([
          '#FFFFFF', // white
          '#0000FF', // blue
        ]).mode('lch').colors(numberOfColors);


    let hexArrayIx = includesZeroValue ? 0 : 1;

    const mapOfValueToHex = sortedMapAsArrays.reduce((accum, val) => {
      const num : number = val[1];

      if (accum[num] === undefined) {
        // eslint-disable-next-line no-param-reassign
        accum[num] = hexArray[hexArrayIx];
        hexArrayIx += 1;
      }

      return accum;
    }, {});


    this.regionToRgba = sortedMapAsArrays.reduce((accum, entry) => {
      const [region, value] = entry;
      const hex: string = mapOfValueToHex[value];
      // eslint-disable-next-line no-param-reassign
      accum[region] = chroma(hex).rgba();
      return accum;
    }, {});
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

    const value = map[styleEntry.county] !== undefined
      ? map[styleEntry.county]
      : map[styleEntry.state];

    if (typeof value !== 'number') {
      return;
    }

    // eslint-disable-next-line no-param-reassign
    styleEntry.value = value;

    let rgba: number[] = [0, 0, 0, 0];

    if (this.regionToRgba) {
      if (this.regionToRgba[styleEntry.county]) {
        rgba = this.regionToRgba[styleEntry.county];
      } else if (this.regionToRgba[styleEntry.state]) {
        rgba = this.regionToRgba[styleEntry.state];
      }
    }

    // eslint-disable-next-line no-param-reassign
    styleEntry.style = new Style({
      stroke: inQueryStroke,
      fill: newFill(rgba[0], rgba[1], rgba[2], rgba[3]),
    });
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

  getValueForFeature(feature: Feature) : Style {
    const styleEntry: ?StyleEntry = this.getStyleEntryForFeature(feature);
    return styleEntry ? styleEntry.value : null;
  }
}

export const newFeatureCollectionHandler
    = (featureCollection: Object, mapRegionNameToValue?: { [string]: number }) =>
      new FeatureCollectionHandler(featureCollection, mapRegionNameToValue);
