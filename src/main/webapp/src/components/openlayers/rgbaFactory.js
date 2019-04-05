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
      min: Number.MAX_SAFE_INTEGER,
      max: Number.MIN_SAFE_INTEGER,
      // First slot with a discernible non-white color
      firstNonZeroValueSlot: 3,
      divisorToGetSlot: -1,
      rgbaArray: [],
    },
    neg: {
      min: Number.MAX_SAFE_INTEGER,
      max: Number.MIN_SAFE_INTEGER,
      // First slot with a discernible non-white color
      firstNonZeroValueSlot: 3,
      divisorToGetSlot: -1,
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

      if (val < data.min) {
        data.min = val;
      }

      if (val > data.max) {
        data.max = val;
      }
    });

    const setRangeData = (entry: Object, rgbaArray: Array<any>) => {
      entry.rgbaArray = rgbaArray;
      entry.divisorToGetSlot
          = (entry.max - entry.min) / (rgbaArray.length - entry.firstNonZeroValueSlot - 1);
    };

    if (this.rangeData.pos.max !== Number.MIN_SAFE_INTEGER) {
      setRangeData(this.rangeData.pos, getRgbaArray(true));
    }

    if (this.rangeData.neg.max !== Number.MIN_SAFE_INTEGER) {
      setRangeData(this.rangeData.neg, getRgbaArray(false));
    }
  }

  createRegionToRgbaMap() {
    return Object.entries(this.mapRegionNameToValue).reduce((accum, entry) => {
      const getRgba = (value: any) => {
        const data = value < 0 ? this.rangeData.neg : this.rangeData.pos;
        let slot;
        const absVal = round(Math.abs(value), 4);

        if (absVal === 0) {
          slot = 0;
        } else {
          slot = round(absVal / data.divisorToGetSlot) + data.firstNonZeroValueSlot;
        }

        if (absVal === 0) {
          slot = 0;
        } else if (data.divisorToGetSlot !== 0) {
          slot = round((absVal - data.min) / data.divisorToGetSlot)
              + data.firstNonZeroValueSlot;
        } else {
          // if divisorToGetSlot === 0, then data.min === data.max and since
          // absVal === 0 has already been handled, set to the max slot
          slot = data.rgbaArray.length - 1;
          // sanity check and to guard against a subsequent change to the
          // algorithm
          if (data.min !== data.max || data.max === 0) {
            // eslint-disable-next-line
            console.error('Logic error');
          }
        }

        return data.rgbaArray[slot];
      };

      const [region, value] = entry;
      accum[region] = getRgba(value);
      return accum;
    }, {});
  }
}

