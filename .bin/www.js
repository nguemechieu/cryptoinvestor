#!/bin/bash


const app = require('../app');

const port = process.env.PORT || 4000;


    console.log('Connected to Mysql Server');
    app.listen(port, () => console.log(`Server running on port ${port}`));
