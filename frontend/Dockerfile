FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY Dockerfile ./Dockerfile
COPY index.html ./
COPY tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts vitest.config.ts ./
COPY src ./src
RUN npm run build

FROM nginx:1.29.2-alpine
RUN apk upgrade --no-cache
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
