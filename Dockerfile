# Filename: Dockerfile
FROM ubuntu
RUN apt-get update \
    && apt-get install -y --no-install-recommends mysql-client \
    && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["mysql"]
WORKDIR _server/server.js
COPY package*.json ./
FROM node
RUN npm install

COPY . .
EXPOSE 4000
CMD ["npm", "run api"]