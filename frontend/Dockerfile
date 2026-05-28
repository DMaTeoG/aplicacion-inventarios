# --- Build Stage ---
FROM node:20-alpine AS build
WORKDIR /app

# Copiar package.json y package-lock.json para cachear capas de dependencias
COPY package.json package-lock.json ./
RUN npm ci

# Copiar las fuentes de Angular y compilar para producción
COPY . .
RUN npm run build -- --configuration=production

# --- Nginx Server Stage ---
FROM nginx:1.25-alpine

# Eliminar archivos estáticos por defecto
RUN rm -rf /usr/share/nginx/html/*

# Copiar los estáticos construidos desde la etapa anterior (Angular 17+ usa dist/frontend/browser)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copiar la plantilla de configuración de Nginx
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Exponer el puerto 80 para tráfico web
EXPOSE 80
