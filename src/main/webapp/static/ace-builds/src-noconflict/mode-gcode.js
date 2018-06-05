ace.define('ace/mode/gcode_highlight_rules', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextHighlightRules = require('./text_highlight_rules').TextHighlightRules;

  const GcodeHighlightRules = function () {
    const keywords = (
      'IF|DO|WHILE|ENDWHILE|CALL|ENDIF|SUB|ENDSUB|GOTO|REPEAT|ENDREPEAT|CALL'
    );

    const builtinConstants = (
      'PI'
    );

    const builtinFunctions = (
      'ATAN|ABS|ACOS|ASIN|SIN|COS|EXP|FIX|FUP|ROUND|LN|TAN'
    );
    const keywordMapper = this.createKeywordMapper({
      'support.function': builtinFunctions,
      keyword: keywords,
      'constant.language': builtinConstants,
    }, 'identifier', true);

    this.$rules = {
      start: [{
        token: 'comment',
        regex: '\\(.*\\)',
      }, {
        token: 'comment', // block number
        regex: '([N])([0-9]+)',
      }, {
        token: 'string', // " string
        regex: '([G])([0-9]+\\.?[0-9]?)',
      }, {
        token: 'string', // ' string
        regex: '([M])([0-9]+\\.?[0-9]?)',
      }, {
        token: 'constant.numeric', // float
        regex: '([-+]?([0-9]*\\.?[0-9]+\\.?))|(\\b0[xX][a-fA-F0-9]+|(\\b\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?)',
      }, {
        token: keywordMapper,
        regex: '[A-Z]',
      }, {
        token: 'keyword.operator',
        regex: 'EQ|LT|GT|NE|GE|LE|OR|XOR',
      }, {
        token: 'paren.lparen',
        regex: '[\\[]',
      }, {
        token: 'paren.rparen',
        regex: '[\\]]',
      }, {
        token: 'text',
        regex: '\\s+',
      }],
    };
  };

  oop.inherits(GcodeHighlightRules, TextHighlightRules);

  exports.GcodeHighlightRules = GcodeHighlightRules;
});

ace.define('ace/mode/gcode', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextMode = require('./text').Mode;
  const GcodeHighlightRules = require('./gcode_highlight_rules').GcodeHighlightRules;
  const Range = require('../range').Range;

  const Mode = function () {
    this.HighlightRules = GcodeHighlightRules;
    this.$behaviour = this.$defaultBehaviour;
  };
  oop.inherits(Mode, TextMode);

  (function () {
    this.$id = 'ace/mode/gcode';
  }).call(Mode.prototype);

  exports.Mode = Mode;
});
(function () {
  ace.require(['ace/mode/gcode'], (m) => {
    if (typeof module === 'object' && typeof exports === 'object' && module) {
      module.exports = m;
    }
  });
}());
