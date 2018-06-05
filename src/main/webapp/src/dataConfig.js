// @flow

const mock = {
  activate: false,
  type: {
    dataRefresh: true,
    login: true,
    usermaint: true,
    upload: true,
  },
};

const isAllMock : () => boolean = () => {
  let anyFalse = false;
  function ckIfFalse(o) {
    Object.values(o).forEach((v) => {
      if (typeof v === 'object') {
        ckIfFalse(v);
      } else if (v === false) {
        anyFalse = true;
      }
    });
  }
  ckIfFalse(mock);
  return !anyFalse;
};

const isMock = {
  dataRefresh: mock.activate && mock.type.dataRefresh,
  login: isAllMock(),
  usermaint: mock.activate && mock.type.usermaint,
  upload: mock.activate && mock.type.upload,
};

export default isMock;

