import { Vue$$ } from '@/vue_instance_properties';

declare module 'vue/types/vue' {
  interface Vue {
    $$: Vue$$
  }
}
