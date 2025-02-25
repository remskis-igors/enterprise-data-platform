# Stage 1: Build React app
FROM node:14 AS build
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Run Node.js server for backend and serve built files
FROM node:14-slim
WORKDIR /app
COPY backend/package.json backend/package-lock.json ./
RUN npm install
COPY backend/ ./
COPY --from=build /app/build ./public
EXPOSE 3001
CMD ["node", "server.js"]
