import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

const frontendDirectory = dirname(fileURLToPath(import.meta.url));
const repositoryDirectory = resolve(frontendDirectory, '..');

export default defineConfig(({ mode }) => {
  // O .env normal fica na raiz do projeto, no mesmo nível das pastas backend/ e frontend/.
  const env = loadEnv(mode, repositoryDirectory, '');
  const apiKey = process.env.API_KEY || env.API_KEY;
  const backendUrl = process.env.BACKEND_URL || env.BACKEND_URL || 'http://localhost:8081';

  if (!apiKey) {
    throw new Error(
      'API_KEY não encontrada. Verifique se o arquivo .env existe na raiz do projeto e contém API_KEY.',
    );
  }

  return {
    plugins: [react()],
    server: {
      host: '0.0.0.0',
      port: 5173,
      strictPort: true,
      proxy: {
        '/api': {
          target: backendUrl,
          changeOrigin: true,
          headers: { 'X-API-Key': apiKey },
        },
      },
    },
  };
});
