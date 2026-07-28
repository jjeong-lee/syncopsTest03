FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY tsconfig.json vite.config.ts vitest.config.ts tailwind.config.js postcss.config.js index.html ./
COPY src ./src
COPY tests ./tests
RUN npm run build

FROM nginx:1.27.2-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN mkdir -p /var/cache/nginx /var/run \
    && chown -R nginx:nginx /var/cache/nginx /var/run /run /usr/share/nginx/html /etc/nginx/conf.d
USER nginx
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --retries=10 CMD wget -qO- http://127.0.0.1/ || exit 1
