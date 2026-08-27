FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY index.html ./
COPY tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts vitest.config.ts ./
COPY src ./src
RUN npm run build

FROM nginx:1.29.4-alpine
RUN apk upgrade --no-cache
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 CMD wget -q -O /dev/null http://localhost/ || exit 1
RUN chown -R nginx:nginx /var/cache/nginx && touch /var/run/nginx.pid && chown nginx:nginx /var/run/nginx.pid
USER nginx
