ace.define('ace/mode/graphqlschema_highlight_rules', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextHighlightRules = require('./text_highlight_rules').TextHighlightRules;

  const GraphQLSchemaHighlightRules = function () {
    const keywords = (
      'type|interface|union|enum|schema|input|implements|extends|scalar'
    );

    const dataTypes = (
      'Int|Float|String|ID|Boolean'
    );

    const keywordMapper = this.createKeywordMapper({
      keyword: keywords,
      'storage.type': dataTypes,
    }, 'identifier');

    this.$rules = {
      start: [{
        token: 'comment',
        regex: '#.*$',
      }, {
        token: 'paren.lparen',
        regex: /[\[({]/,
        next: 'start',
      }, {
        token: 'paren.rparen',
        regex: /[\])}]/,
      }, {
        token: keywordMapper,
        regex: '[a-zA-Z_$][a-zA-Z0-9_$]*\\b',
      }],
    };
    this.normalizeRules();
  };

  oop.inherits(GraphQLSchemaHighlightRules, TextHighlightRules);

  exports.GraphQLSchemaHighlightRules = GraphQLSchemaHighlightRules;
});

ace.define('ace/mode/folding/cstyle', [], (require, exports, module) => {
  const oop = require('../../lib/oop');
  const Range = require('../../range').Range;
  const BaseFoldMode = require('./fold_mode').FoldMode;

  const FoldMode = exports.FoldMode = function (commentRegex) {
    if (commentRegex) {
      this.foldingStartMarker = new RegExp(this.foldingStartMarker.source.replace(/\|[^|]*?$/, `|${commentRegex.start}`));
      this.foldingStopMarker = new RegExp(this.foldingStopMarker.source.replace(/\|[^|]*?$/, `|${commentRegex.end}`));
    }
  };
  oop.inherits(FoldMode, BaseFoldMode);

  (function () {
    this.foldingStartMarker = /([\{\[\(])[^\}\]\)]*$|^\s*(\/\*)/;
    this.foldingStopMarker = /^[^\[\{\(]*([\}\]\)])|^[\s\*]*(\*\/)/;
    this.singleLineBlockCommentRe = /^\s*(\/\*).*\*\/\s*$/;
    this.tripleStarBlockCommentRe = /^\s*(\/\*\*\*).*\*\/\s*$/;
    this.startRegionRe = /^\s*(\/\*|\/\/)#?region\b/;
    this._getFoldWidgetBase = this.getFoldWidget;
    this.getFoldWidget = function (session, foldStyle, row) {
      const line = session.getLine(row);

      if (this.singleLineBlockCommentRe.test(line)) {
        if (!this.startRegionRe.test(line) && !this.tripleStarBlockCommentRe.test(line)) { return ''; }
      }

      const fw = this._getFoldWidgetBase(session, foldStyle, row);

      if (!fw && this.startRegionRe.test(line)) { return 'start'; } // lineCommentRegionStart

      return fw;
    };

    this.getFoldWidgetRange = function (session, foldStyle, row, forceMultiline) {
      const line = session.getLine(row);

      if (this.startRegionRe.test(line)) { return this.getCommentRegionBlock(session, line, row); }

      var match = line.match(this.foldingStartMarker);
      if (match) {
        var i = match.index;

        if (match[1]) { return this.openingBracketBlock(session, match[1], row, i); }

        let range = session.getCommentFoldRange(row, i + match[0].length, 1);

        if (range && !range.isMultiLine()) {
          if (forceMultiline) {
            range = this.getSectionRange(session, row);
          } else if (foldStyle != 'all') { range = null; }
        }

        return range;
      }

      if (foldStyle === 'markbegin') { return; }

      var match = line.match(this.foldingStopMarker);
      if (match) {
        var i = match.index + match[0].length;

        if (match[1]) { return this.closingBracketBlock(session, match[1], row, i); }

        return session.getCommentFoldRange(row, i, -1);
      }
    };

    this.getSectionRange = function (session, row) {
      let line = session.getLine(row);
      const startIndent = line.search(/\S/);
      const startRow = row;
      const startColumn = line.length;
      row += 1;
      let endRow = row;
      const maxRow = session.getLength();
      while (++row < maxRow) {
        line = session.getLine(row);
        const indent = line.search(/\S/);
        if (indent === -1) { continue; }
        if (startIndent > indent) { break; }
        const subRange = this.getFoldWidgetRange(session, 'all', row);

        if (subRange) {
          if (subRange.start.row <= startRow) {
            break;
          } else if (subRange.isMultiLine()) {
            row = subRange.end.row;
          } else if (startIndent == indent) {
            break;
          }
        }
        endRow = row;
      }

      return new Range(startRow, startColumn, endRow, session.getLine(endRow).length);
    };
    this.getCommentRegionBlock = function (session, line, row) {
      const startColumn = line.search(/\s*$/);
      const maxRow = session.getLength();
      const startRow = row;

      const re = /^\s*(?:\/\*|\/\/|--)#?(end)?region\b/;
      let depth = 1;
      while (++row < maxRow) {
        line = session.getLine(row);
        const m = re.exec(line);
        if (!m) continue;
        if (m[1]) depth--;
        else depth++;

        if (!depth) break;
      }

      const endRow = row;
      if (endRow > startRow) {
        return new Range(startRow, startColumn, endRow, line.length);
      }
    };
  }).call(FoldMode.prototype);
});

ace.define('ace/mode/graphqlschema', [], (require, exports, module) => {
  const oop = require('../lib/oop');
  const TextMode = require('./text').Mode;
  const GraphQLSchemaHighlightRules = require('./graphqlschema_highlight_rules').GraphQLSchemaHighlightRules;
  const FoldMode = require('./folding/cstyle').FoldMode;

  const Mode = function () {
    this.HighlightRules = GraphQLSchemaHighlightRules;
    this.foldingRules = new FoldMode();
  };
  oop.inherits(Mode, TextMode);

  (function () {
    this.lineCommentStart = '#';
    this.$id = 'ace/mode/graphqlschema';
  }).call(Mode.prototype);

  exports.Mode = Mode;
});
(function () {
  ace.require(['ace/mode/graphqlschema'], (m) => {
    if (typeof module === 'object' && typeof exports === 'object' && module) {
      module.exports = m;
    }
  });
}());
