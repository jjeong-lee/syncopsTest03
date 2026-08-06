FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY tsconfig.json vite.config.ts tailwind.config.js postcss.config.js index.html ./
COPY src ./src
COPY tests ./tests
RUN npm run build

FROM nginx:1.27.2-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=20s --timeout=5s --retries=5 CMD wget -qO- http://127.0.0.1/ || exit 1
