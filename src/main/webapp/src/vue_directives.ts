/* eslint-disable no-param-reassign */
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
    // @ts-ignore
    el.event = (event) => {
      // check that click was outside the el and his children
      if (!(el === event.target || el.contains(event.target))) {
        // and if it did, call method provided in attribute value
        // @ts-ignore
        vnode.context[binding.expression](event);
      }
    };
    // @ts-ignore
    if (el && el.event && document.body) {
      // @ts-ignore
      document.body.addEventListener('click', (el.event));
    }
  },
  unbind(el) {
    // @ts-ignore
    if (el && el.event && document.body) {
      // @ts-ignore
      document.body.removeEventListener('click', el.event);
    }
  },
});
