FROM ubuntu
MAINTAINER nguemechieu@live.com
RUN apt update
CMD ["echo", "Hello World\n welcome to cryptoinvestor_dockerfile!"]
RUN apt-get install yarn

RUN apt install npm

BUILD  https://github.com/nguemechieu/cryptoinvestor
RUN yarn
CMD ["echo", "Now running  cryptoinvestor web server!"]


ENTRYPOINT npm run api

