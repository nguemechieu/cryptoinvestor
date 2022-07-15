# Filename: Dockerfile

FROM node:latest

WORKDIR bin/www.js
RUN npm install -g npm@8.14.0
RUN  yarn
COPY package*.json ./
COPY . .
EXPOSE 4000
CMD ["npm", "run"]

