import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://backend:4000',
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    css: false,
    exclude: ['**/node_modules/**', '**/e2e/**'],
  },
});
