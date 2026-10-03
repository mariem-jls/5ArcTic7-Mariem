# ============ ÉTAPE 1 : BUILD ANGULAR ============
FROM node:20-alpine AS build
WORKDIR /app

# Copier package.json et installer les dépendances
COPY package*.json ./
RUN npm install

# Copier le code source et builder
COPY . .
RUN npm run build -- --configuration production

# ============ ÉTAPE 2 : NGINX ============
FROM nginx:alpine

# Copier la config Nginx custom (voir ci-dessous)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copier les fichiers buildés depuis l'étape de build
COPY --from=build /app/dist/*/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
