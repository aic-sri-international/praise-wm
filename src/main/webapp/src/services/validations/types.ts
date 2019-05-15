export type ValParams = {
  required?: boolean | (() => boolean);
  validator?: () => null | string;
  ref: HTMLInputElement,
  // ref: HTMLInputElement | { $el: { focus: () => void }},
}

export type Validations = {
  [key: string]: Object | ValParams;
}
