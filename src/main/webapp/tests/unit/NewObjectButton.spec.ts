import { createLocalVue, mount, shallowMount } from '@vue/test-utils';
import BootstrapVue from 'bootstrap-vue';
import NewObjectButton from '@/components/NewObjectButton.vue';


// create an extended `Vue` constructor
const localVue = createLocalVue();

// install plugins as normal
localVue.use(BootstrapVue);

const title = 'Press Me';
const wrapper = shallowMount(NewObjectButton, {
  localVue,
  propsData: { title },
});

describe('NewObjectButton.vue', () => {
  it('renders props.title when passed', () => {
    expect(wrapper.props().title).toMatch(title);
  });
});
