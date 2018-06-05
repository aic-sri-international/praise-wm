// @flow

export type ModelRule = {
  metadata: string,
  rule: string,
};

export type SegmentedModel = {
  rules: ModelRule[],
}

// eslint-disable-next-line no-useless-escape
const RE_META_TAG_START = /^\s*\/\*\*\s*$/;
// eslint-disable-next-line no-useless-escape
const RE_META_TAG_END = /^\s*\*\/\s*$/;
// eslint-disable-next-line no-useless-escape
const RE_LINE_NOT_EMPTY = /\S+/;

export function segmentModel(model : string) : SegmentedModel {
  const lines = model.split('\n');
  lines.push('');

  const rulesWithMetadata : ModelRule[] = [];

  let metadata = null;
  let rule = null;

  lines.forEach((line) => {
    if (RE_META_TAG_START.test(line)) {
      metadata = [];
      return;
    }
    if (RE_META_TAG_END.test(line) && metadata !== null) {
      rule = [];
      return;
    }

    if (metadata === null) {
      return;
    }

    if (rule === null) {
      metadata.push(line);
      return;
    }

    if (RE_LINE_NOT_EMPTY.test(line)) {
      rule.push(line);
      return;
    }

    if (rule.length) {
      rulesWithMetadata.push({
        metadata: `${metadata.join('\n')}\n`,
        rule: `${rule.join('\n')}\n`,
      });
    }
    metadata = null;
    rule = null;
  });

  return { rules: rulesWithMetadata };
}

export function coalesceModel(segmentedModel: SegmentedModel) : string {
  if (segmentedModel) {
    return 'ok';
  }
  return '';
}
