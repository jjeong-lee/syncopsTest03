FROM node:20.11.0-alpine AS build
WORKDIR /workspace
COPY package.json ./
RUN npm install
COPY tsconfig.json vite.config.ts vitest.config.ts tailwind.config.js postcss.config.js index.html ./
COPY src ./src
COPY tests ./tests
RUN npm run build

FROM nginx:1.25.4-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
RUN chown -R nginx:nginx /etc/nginx/conf.d /usr/share/nginx/html /var/cache/nginx /var/log/nginx \
  && touch /var/run/nginx.pid \
  && chown nginx:nginx /var/run/nginx.pid
EXPOSE 8080
USER nginx
HEALTHCHECK --interval=10s --timeout=3s --retries=12 CMD wget -qO- http://127.0.0.1:8080/ >/dev/null || exit 1
