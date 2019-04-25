Notes
-----
warning " > bootstrap@4.3.1" has unmet peer dependency "jquery@1.9.1 - 3".
warning " > bootstrap@4.3.1" has unmet peer dependency "popper.js@^1.14.7".

The above are to be expected.

// Console messages similar to the following are expected. See https://github.com/ajaxorg/ace/issues/3633
ace.js:1 [Violation] Added non-passive event listener to a scroll-blocking 'mousewheel' event.

// The following is expected
ERROR in static/js/vendor.c73f84683ac948744d8a.js from UglifyJs
Unexpected token: name (global) [./node_modules/ace-builds/src-noconflict/ace.js:40,0][static/js/vendor.c73f84683ac948744d8a.js:60692,6]
