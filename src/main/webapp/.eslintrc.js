module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    'plugin:vue/recommended',
    '@vue/airbnb',
    '@vue/typescript',
  ],
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'import/prefer-default-export': 'off',
    // There is currently an issue with eslint not finding imported types from type files
    // Since we are using typescript for all our files and typescript correctly
    // reports if a type is not found, we are disabling the eslint feature.
    'import/named': 'off',
    'class-methods-use-this': 'off',
    indent: 'off',
    'vue/script-indent': [
      'error',
      2,
      { baseIndent: 1 },
    ],
  },
  parserOptions: {
    parser: '@typescript-eslint/parser',
  },
};
