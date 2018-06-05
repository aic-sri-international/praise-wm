ace.define('ace/mode/csp_highlight_rules', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextHighlightRules = require('./text_highlight_rules').TextHighlightRules;

  const CspHighlightRules = function () {
    const keywordMapper = this.createKeywordMapper({
      'constant.language': 'child-src|connect-src|default-src|font-src|frame-src|img-src|manifest-src|media-src|object-src'
                  + '|script-src|style-src|worker-src|base-uri|plugin-types|sandbox|disown-opener|form-action|frame-ancestors|report-uri'
                  + '|report-to|upgrade-insecure-requests|block-all-mixed-content|require-sri-for|reflected-xss|referrer|policy-uri',
      variable: "'none'|'self'|'unsafe-inline'|'unsafe-eval'|'strict-dynamic'|'unsafe-hashed-attributes'",
    }, 'identifier', true);

    this.$rules = {
      start: [{
        token: 'string.link',
        regex: /https?:[^;\s]*/,
      }, {
        token: 'operator.punctuation',
        regex: /;/,
      }, {
        token: keywordMapper,
        regex: /[^\s;]+/,
      }],
    };
  };

  oop.inherits(CspHighlightRules, TextHighlightRules);

  exports.CspHighlightRules = CspHighlightRules;
});

ace.define('ace/mode/csp', [], (require, exports, module) => {
  const TextMode = require('./text').Mode;
  const CspHighlightRules = require('./csp_highlight_rules').CspHighlightRules;
  const oop = require('../lib/oop');

  const Mode = function () {
    this.HighlightRules = CspHighlightRules;
  };

  oop.inherits(Mode, TextMode);

  (function () {
    this.$id = 'ace/mode/csp';
  }).call(Mode.prototype);

  exports.Mode = Mode;
});
(function () {
  ace.require(['ace/mode/csp'], (m) => {
    if (typeof module === 'object' && typeof exports === 'object' && module) {
      module.exports = m;
    }
  });
}());
