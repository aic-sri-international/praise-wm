/* eslint-disable no-param-reassign */
// @flow

import chroma from 'chroma-js';
import round from 'lodash/round';

const rgbaObject = { positive: [], negative: [] };

const buildRgbaArray = (isPositive: boolean): Array<any> => {
  const numberOfColors = 40;
  const hexArray: string[]
        = chroma.scale([
          '#FFFFFF', // white
          isPositive ? '#0000FF' : '#FF0000', // blue or red
        ]).mode('lch').colors(numberOfColors);
  return hexArray.map(hex => chroma(hex).rgba());
};

const getRgbaArray = (isPositive) => {
  if (isPositive) {
    if (!rgbaObject.positive.length) {
      rgbaObject.positive = buildRgbaArray(true);
    }
    return rgbaObject.positive.slice(0);
  }

  if (!rgbaObject.negative.length) {
    rgbaObject.negative = buildRgbaArray(false);
  }
  return rgbaObject.negative.slice(0);
};

export default class RgbaFactor {
  rangeData = {
    pos: {
      max: Number.MIN_SAFE_INTEGER,
      rgbaArray: [],
    },
    neg: {
      // Maximum absolute value
      max: Number.MIN_SAFE_INTEGER,
      rgbaArray: [],
    },
  };
  mapRegionNameToValue: ?{ [string]: number };

  constructor(mapRegionNameToValue: { [string]: number }) {
    this.mapRegionNameToValue = mapRegionNameToValue;
    this.updateRangeData();
  }

  updateRangeData() {
    Object.values(this.mapRegionNameToValue).forEach((value) => {
      let val: any = value;
      const data = val < 0 ? this.rangeData.neg : this.rangeData.pos;

      val = Math.abs(val);

      if (val > data.max) {
        data.max = val;
      }
    });

    this.rangeData.pos.rgbaArray = getRgbaArray(true);
    this.rangeData.neg.rgbaArray = getRgbaArray(false);
  }

  createRegionToRgbaMap() {
    return Object.entries(this.mapRegionNameToValue).reduce((accum, entry) => {
      const getRgba = (value: any) => {
        const data = value < 0 ? this.rangeData.neg : this.rangeData.pos;
        let slot;

        if (data.max === 0) {
          slot = 0;
        } else {
          slot = round((Math.abs(value) / data.max) * (data.rgbaArray.length - 1));
        }
        return data.rgbaArray[slot];
      };

      const [region, value] = entry;
      accum[region] = getRgba(value);
      return accum;
    }, {});
  }
}

