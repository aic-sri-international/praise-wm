// @flow

const SYSTEM_STATUS_VXC = {
  MODULE: 'systemStatus',
  GET: {
    UI_IS_OPEN: 'systemStatusUiIsOpen',
    STATUS_OVERALL_CLASS: 'systemStatusOverallClass',
    DATABASE_CLASS: 'systemStatusDatabaseClass',
  },
  SET: {
    UI_IS_OPEN: 'setSystemStatusUiIsOpen',
    DATABASE: 'setSystemStatusDatabase',
    ALL_STATUSES_UNKNOWN: 'setAllStatusesToUnknown',
  },
};

export default SYSTEM_STATUS_VXC;
