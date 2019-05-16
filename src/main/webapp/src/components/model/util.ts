// eslint-disable-next-line import/prefer-default-export
export const getSliderRangeLabel = (num: number | string, textToAppend?: string) => {
  let numOut: number | string = '';
  if (typeof num === 'number') {
    numOut = Number.isInteger(num) ? num : num.toFixed(2);
  }
  return textToAppend ? `${numOut}${textToAppend}` : `${numOut}`;
};
