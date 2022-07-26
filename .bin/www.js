#!/bin/bash


const app = require('../app');
const http = require("http");

const server = http.createServer(app);

const port = process.env.PORT || 3000;


    console.log('Connected to Mysql Server');
    server.listen(port, () => console.log(`Server running on port ${port}`));
