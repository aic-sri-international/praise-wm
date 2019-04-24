<template>
  <div>
    <div style="margin-top: 5px">
      <a class="btn-block" style="color: black"
         @click="toggleSideBarCollapse">
        <div v-show="isSideBarCollapsed">
          <i class="fas fa-caret-right"></i>
        </div>
        <div v-show="!isSideBarCollapsed">
          <i class="fas fa-caret-left"></i>
        </div>
      </a>
    </div>

    <!-- Sidebar Holder -->
    <nav id="sidebar" :class="{ 'active': isSideBarCollapsed }">
      <ul class="list-unstyled components">
        <li :class="{ currentRoute : $route.path === paths.HOME }">
          <a @click="routeTo(paths.HOME)">
            <span v-tippy="tipOptions" :title="titles.home">
                <i class="fas fa-home" data-fa-transform="grow-8"></i>
            </span>
            <span v-if="!isSideBarCollapsed" class="ml-2">
               {{titles.home}}
            </span>
          </a>
        </li>
        <li>
          <a v-b-toggle.maintSubmenu data-toggle="collapse">
            <span v-tippy="tipOptions" :title="titles.maint">
              <i class="fas fa-wrench" data-fa-transform="grow-8"></i>
            </span>
            <span v-show="!isSideBarCollapsed" class="ml-2">
               {{titles.maint}}
            </span>
          </a>
          <b-collapse class="collapse" id="maintSubmenu">
            <ul class="list-unstyled">
              <li :class="{ currentRoute : $route.path === paths.USER_MAINT }">
                <a @click="routeTo(paths.USER_MAINT)">
                  <span v-tippy="tipOptions" :title="titles.maint_u">
                    <i class="fas fa-user" data-fa-transform="grow-6"></i>
                  </span>
                  <span v-if="!isSideBarCollapsed" class="ml-2">
                      {{titles.maint_u}}
                  </span>
                </a>
              </li>
            </ul>
          </b-collapse>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
  // @flow
  import { SIDEBAR_VXC as SC } from '@/store';
  import { paths } from '@/router';
  import { mapGetters, mapMutations } from 'vuex';

  export default {
    name: 'sideBar',
    data() {
      return {
        paths,
        titles: {
          home: 'Home',
          maint: 'Maintenance',
          // maint_e: 'Model Editor',
          maint_u: 'User',
        },
        tipOptions: {
          placement: 'right',
          popperOptions: {
            modifiers: {
              preventOverflow: {
                enabled: false,
              },
              hide: {
                enabled: false,
              },
            },
          },
        },
      };
    },
    methods: {
      ...mapMutations(SC.MODULE, [
        SC.SET.TOGGLE_SIDEBAR_COLLAPSE,
      ]),
      routeTo(path: string) {
        this.$router.push(path);
      },
    },
    computed: {
      ...mapGetters(SC.MODULE, [
        SC.GET.IS_SIDEBAR_COLLAPSED,
      ]),
    },
  };
</script>
<style scoped>
  a, a:hover, a:focus {
    color: inherit;
    text-decoration: none;
    transition: all 0.3s;
    cursor: pointer;
  }

  i, span {
    display: inline-block;
  }

  /* ---------------------------------------------------
      SIDEBAR STYLE
  ----------------------------------------------------- */
  .currentRoute {
    color: #777c80;
    background-color: #cdcdcd;
  }

  #sidebar {
    min-width: 180px;
    max-width: 180px;
    background-color: #dddddd;
    color: #777c80;
    transition: all 0.3s;
  }

  #sidebar.active {
    min-width: 80px;
    max-width: 80px;
    text-align: center;
  }

  #sidebar.active .sidebar-header h4 {
    display: none;
  }

  #sidebar.active .sidebar-header strong {
    display: block;
  }

  #sidebar ul li a {
    text-align: left;
  }

  #sidebar.active ul li a {
    padding: 20px 10px;
    text-align: center;
    font-size: 0.85em;
  }

  #sidebar.active ul li a i {
    margin-right:  0;
    display: block;
    font-size: 1.8em;
    margin-bottom: 5px;
  }

  #sidebar.active ul ul a {
    padding: 10px !important;
  }

  #sidebar.active a[aria-expanded="false"]::before, #sidebar.active a[aria-expanded="true"]::before {
    top: auto;
    bottom: 5px;
    right: 50%;
    -webkit-transform: translateX(50%);
    -ms-transform: translateX(50%);
    transform: translateX(50%);
  }

  #sidebar .sidebar-header strong {
    display: none;
    font-size: 1.8em;
  }

  #sidebar ul.components {
    padding: 20px 0;
    border-bottom: 1px solid #a8a8a8;
  }

  #sidebar ul li a {
    padding: 10px;
    font-size: 1.1em;
    display: block;
  }
  #sidebar ul li a:hover {
    color: white;
    background: inherit;
  }
  #sidebar ul li a i {
    margin-right: 10px;
  }

  a[data-toggle="collapse"] {
    position: relative;
  }

  a[aria-expanded="false"]::before, a[aria-expanded="true"]::before {
    content: '';
    display: block;
    position: absolute;
    right: 20px;
    margin-top: 6px;
    width: 0;
    height: 0;
    border-top: 5px solid transparent;
    border-bottom: 5px solid transparent;
    border-left: 5px solid currentColor;
  }

  a[aria-expanded="true"]::before {
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid currentColor;
  }

  /* Selected child menus when open */
  ul ul a {
    font-size: 0.9em !important;
    padding-left: 30px !important;
  }
</style>
