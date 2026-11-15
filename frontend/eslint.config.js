import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
  },
  {
    // Arquivos do Jest: libera describe/it/expect/jest e variáveis do Node.
    files: ['**/*.test.{js,jsx}', 'tests/jest/**', 'jest.setup.js', 'jest.config.js', 'tests/__mocks__/**'],
    languageOptions: {
      globals: { ...globals.jest, ...globals.node },
    },
    rules: {
      // Fast refresh é recurso do servidor de desenvolvimento; não se aplica a testes.
      'react-refresh/only-export-components': 'off',
    },
  },
])
