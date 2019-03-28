// @flow

// eslint-disable-next-line import/prefer-default-export
export const getSliderRangeLabel = (num: number, textToAppend: ?string) => {
  let numOut = '';
  if (num !== undefined) {
    numOut = Number.isInteger(num) ? num : num.toFixed(2);
  }
  return textToAppend ? `${numOut}${textToAppend}` : `${numOut}`;
};
