# Task Orchestrator Frontend

This frontend application provides a React-based UI for the Task Orchestrator backend service. It uses TypeScript, Vite, and various modern frontend libraries for building a responsive, type-safe application.

## Architecture

### SPA Routing with Nginx

The frontend is served by Nginx in a Docker container configured for Single Page Application (SPA) routing. The `nginx.conf` includes a fallback rule that redirects all non-existent paths to `index.html`, enabling client-side routing to handle all navigation:

```nginx
location / {
  try_files $uri $uri/ /index.html;
}
error_page 404 /index.html;
```

This configuration allows users to reload the page or share deep links without encountering 404 errors. The React Router handles all routing on the client side.

### Authentication & Authorization

The application implements JWT-based authentication with role-based access control:

- **Token Storage**: Access and refresh tokens are stored in `sessionStorage`
- **Token Refresh**: Automatic token refresh via axios interceptor on 401 responses
- **Role-based Routes**: Protected routes check user roles before rendering
- **State Persistence**: Auth state persists through page reloads via stored tokens

## Development Setup

```bash
npm install
npm run dev
```

## Building for Production

```bash
npm run build
npm run test:run
```

## Project Structure

- `src/auth/` - Authentication context, hooks, and utilities
- `src/pages/` - Page components (Dashboard, Jobs, Login)
- `src/components/` - Reusable UI components
- `src/hooks/` - React hooks for API calls and auth
- `src/layout/` - Layout components (Navbar, Sidebar, etc.)
- `src/api/` - API client and endpoints
- `src/types/` - TypeScript type definitions

## React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
