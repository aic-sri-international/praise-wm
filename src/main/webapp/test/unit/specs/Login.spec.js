import Vue from 'vue';
import Login from '@/components/login/Login';

describe('Login.vue', () => {
  it('should render correct contents', () => {
    const Constructor = Vue.extend(Login);
    const vm = new Constructor().$mount();
    expect(vm.$el.querySelector('#usernameText').textContent)
      .to.equal('Username');
  });
});
