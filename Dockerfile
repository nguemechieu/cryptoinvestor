FROM ubuntu:latest

RUN echo mkdir "cryptoinvestor"
RUN echo cd "cryptoinvestor"
FROM node


WORKDIR ./cryptoinvestor


COPY ./package.json ./


RUN npm install -g npm@8.15.0
RUN npm fund
RUN yarn


COPY . .
EXPOSE 4000
CMD ["npm", "run", "api"]
RUN npm run start

