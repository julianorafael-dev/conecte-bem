// Configuração do Jest para o frontend (React + Vite).
// O Babel é configurado aqui dentro (e não num babel.config.*) para não
// interferir no build do Vite.
export default {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testMatch: ['<rootDir>/src/**/*.test.{js,jsx}', '<rootDir>/tests/**/*.test.{js,jsx}'],
  transform: {
    '^.+\\.[jt]sx?$': [
      'babel-jest',
      {
        presets: [
          ['@babel/preset-env', { targets: { node: 'current' } }],
          ['@babel/preset-react', { runtime: 'automatic' }],
        ],
        // Converte import.meta.env (Vite) em process.env para o Jest entender.
        plugins: ['babel-plugin-transform-vite-meta-env'],
      },
    ],
  },
  moduleNameMapper: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '\\.(png|jpe?g|gif|svg|avif|webp|ico)$': '<rootDir>/tests/__mocks__/arquivoMock.js',
  },
};
