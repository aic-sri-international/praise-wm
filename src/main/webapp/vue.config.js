module.exports = {
  devServer: {
    proxy: {
      '^/admin': {
        target: 'http://localhost:4567',
      },
      '^/api': {
        target: 'http://localhost:4567',
      },
      '^/ws': {
        target: 'ws://localhost:4567',
        ws: true,
      },
    },
  },
};
