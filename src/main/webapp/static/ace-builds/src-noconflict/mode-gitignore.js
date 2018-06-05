ace.define('ace/mode/gitignore_highlight_rules', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextHighlightRules = require('./text_highlight_rules').TextHighlightRules;

  const GitignoreHighlightRules = function () {
    this.$rules = {
      start: [
        {
          token: 'comment',
          regex: /^\s*#.*$/,
        }, {
          token: 'keyword', // negated patterns
          regex: /^\s*!.*$/,
        },
      ],
    };

    this.normalizeRules();
  };

  GitignoreHighlightRules.metaData = {
    fileTypes: ['gitignore'],
    name: 'Gitignore',
  };

  oop.inherits(GitignoreHighlightRules, TextHighlightRules);

  exports.GitignoreHighlightRules = GitignoreHighlightRules;
});

ace.define('ace/mode/gitignore', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextMode = require('./text').Mode;
  const GitignoreHighlightRules = require('./gitignore_highlight_rules').GitignoreHighlightRules;

  const Mode = function () {
    this.HighlightRules = GitignoreHighlightRules;
    this.$behaviour = this.$defaultBehaviour;
  };
  oop.inherits(Mode, TextMode);

  (function () {
    this.lineCommentStart = '#';
    this.$id = 'ace/mode/gitignore';
  }).call(Mode.prototype);

  exports.Mode = Mode;
});
(function () {
  ace.require(['ace/mode/gitignore'], (m) => {
    if (typeof module === 'object' && typeof exports === 'object' && module) {
      module.exports = m;
    }
  });
}());
