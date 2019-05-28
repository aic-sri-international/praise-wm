const keyShowMapPopupOnMouseOver = 'showMapPopupOnMouseOver';

export const getLocalShowMapPopupOnMouseOver = () : boolean => {
  let val = localStorage.getItem(keyShowMapPopupOnMouseOver);
  if (!val || (val !== 'true' && val !== 'false')) {
    localStorage.setItem(keyShowMapPopupOnMouseOver, 'true');
    val = 'true';
  }

  return val === 'true';
};

export const setLocalShowMapPopupOnMouseOver = (mouseOver: boolean) => {
  localStorage.setItem(keyShowMapPopupOnMouseOver, mouseOver ? 'true' : 'false');
};
