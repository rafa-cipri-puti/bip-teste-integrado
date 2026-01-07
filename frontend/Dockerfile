FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine
RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist /tmp/dist
RUN set -eux; \
    APP_DIR="$(ls -1 /tmp/dist | head -n 1)"; \
    cp -R "/tmp/dist/${APP_DIR}/browser/." /usr/share/nginx/html/; \
    rm -rf /tmp/dist

COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
