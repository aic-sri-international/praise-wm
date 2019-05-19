// @ts-ignore
import ace from 'ace-builds/src-noconflict/ace';

/* eslint-disable prefer-destructuring,import/no-unresolved,no-undef,func-names */
ace.define('ace/mode/hogm',
  [
    'require', 'exports', 'module', 'ace/lib/oop', 'ace/mode/text', 'ace/mode/text_highlight_rules', 'ace/worker/worker_client',
  ],
  // @ts-ignore
  (require, exports, module) => {
  const oop = require('ace/lib/oop');
  const TextMode = require('ace/mode/text').Mode;
  const TextHighlightRules = require('ace/mode/text_highlight_rules').TextHighlightRules;

  const MyHighlightRules = function () {
    // @ts-ignore
    const keywordMapper = this.createKeywordMapper({
      'keyword.control': 'if|then|else|for|all|there|exists|in',
      'keyword.operator': 'and|or|not',
      'keyword.other': 'exists',
      'storage.type': 'Boolean|Integer|Real|String',
      'storage.modifier': 'const|random',
      'support.function': 'sort',
      'constant.language': 'x',
    }, 'identifier');
    // @ts-ignore
    this.$rules = {
      start: [
        { token: 'comment', regex: '//.*' },
        { token: 'string', regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]' },
        { token: 'constant.numeric', regex: '0[xX][0-9a-fA-F]+\\b' },
        { token: 'constant.numeric', regex: '[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b' },
        { token: 'keyword.operator', regex: '!|%|\\\\|/|\\*|\\-|\\+|~=|==|<>|!=|<=|>=|=|<|>|&&|\\|\\|' },
        { token: 'punctuation.operator', regex: '\\?|\\:|\\,|\\;|\\.' },
        { token: 'paren.lparen', regex: '[[({]' },
        { token: 'paren.rparen', regex: '[\\])}]' },
        { token: 'text', regex: '\\s+' },
        { token: keywordMapper, regex: '[a-zA-Z_$][a-zA-Z0-9_$]*\\b' },
      ],
    };
  };
  oop.inherits(MyHighlightRules, TextHighlightRules);

  const HOGMMode = function () {
    // @ts-ignore
    this.HighlightRules = MyHighlightRules;
  };
  oop.inherits(HOGMMode, TextMode);

  (function () {
    // @ts-ignore
    this.$id = 'ace/mode/hogm';
  }).call(HOGMMode.prototype);

  exports.Mode = HOGMMode;
});
