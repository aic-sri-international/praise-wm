// @flow

import Vue from 'vue';

/**
 * Scrolls the component into view when its binding value changes from false to true.
 */
Vue.directive('scrollToTopOnUpdated', {
  componentUpdated(el, binding) {
    if (binding.value !== binding.oldValue && binding.value) {
      Vue.nextTick(() => el.scrollIntoView());
    }
  },
});

Vue.directive('onClickOutside', {
  bind(el, binding, vnode) {
    // eslint-disable-next-line no-param-reassign
    el.event = (event) => {
      // check that click was outside the el and his children
      if (!(el === event.target || el.contains(event.target))) {
        // and if it did, call method provided in attribute value
        vnode.context[binding.expression](event);
      }
    };
    if (el && el.event && document.body) {
      document.body.addEventListener('click', (el.event : Function));
    }
  },
  unbind(el) {
    if (el && el.event && document.body) {
      document.body.removeEventListener('click', el.event);
    }
  },
});
