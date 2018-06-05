➜  webapp yarn install
yarn install v1.6.0
[1/4] 🔍  Resolving packages...
[2/4] 🚚  Fetching packages...
[3/4] 🔗  Linking dependencies...
warning "bootstrap-vue > bootstrap@4.1.1" has unmet peer dependency "jquery@1.9.1 - 3".
warning "bootstrap-vue > bootstrap@4.1.1" has incorrect peer dependency "popper.js@^1.14.3".
warning " > bootstrap@4.0.0" has unmet peer dependency "jquery@1.9.1 - 3".
warning " > bootstrap@4.0.0" has unmet peer dependency "popper.js@^1.12.9".
warning " > karma-sinon-chai@1.3.3" has incorrect peer dependency "sinon-chai@^2.9.0".
[4/4] 📃  Building fresh packages...
✨  Done in 24.71s.

The above should be resolved in future updates and do not appear to cause a problem;

// Console messages similar to the following are expected. See https://github.com/ajaxorg/ace/issues/3633
ace.js:1 [Violation] Added non-passive event listener to a scroll-blocking 'mousewheel' event.

// The following is expected
ERROR in static/js/vendor.c73f84683ac948744d8a.js from UglifyJs
Unexpected token: name (global) [./node_modules/ace-builds/src-noconflict/ace.js:40,0][static/js/vendor.c73f84683ac948744d8a.js:60692,6]
