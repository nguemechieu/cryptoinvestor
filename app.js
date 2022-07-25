require('dotenv').config();
const express= require('express'),app=  express() , path = require('path')
    ,
    root= require('./routes/root'),
    cookieParser = require('cookie-parser'),
        signup1= require('./routes/register'),
        signin= require('./routes/login')
const bodyParser = require('body-parser');
const cors = require('cors');
const corsOptions = require('./config/corsOptions');
const { logger } = require('./middleware/logEvents');
const verifyJWT = require('./middleware/verifyJWT');
const credentials = require('./middleware/credentials');
const swaggerJsDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const errorHandler = require("./middleware/errorHandler");

const {resetPassword} = require("./controllers/resetPassword");
const signup = require("./routes/registerPage");


// Initialize documentation module with SwaggerJsdoc
const swaggerOptions = {
  swaggerDefinition: {
    info: {
      title: 'CryptoInvestor',
      description: "Trade Management Application ",
      contact: {
        name: "CryptoInvestor",
      },
      servers: ["http://localhost:4000"]
    }
  },

//  router: ['./routes/*.js'],
  apis: [".bin/www.js"]
}
const swaggerDocs = swaggerJsDoc(swaggerOptions);

app.use((req, res, next) => {

  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content, Accept, Content-Type, Authorization');
   res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  next();
});


// custom middleware logger
app.use(logger);

app.use(bodyParser.json());

// Handle options credentials check - before CORS!
// and fetch cookies credentials requirement
app.use(credentials);

// Cross Origin Resource Sharing
app.use(cors(corsOptions));

// built-in middleware to handle urlencoded form data
app.use(express.urlencoded({ extended: true}));
app.use(bodyParser.urlencoded({ extended: true}))
// built-in middleware for json
app.use(express.json());

//middleware for cookies
app.use(cookieParser());

//serve static files

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use('/api/documentation', swaggerUi.serve, swaggerUi.setup(swaggerDocs))
app.get('/',root)

app.post('/api/users/login', signin);

app.get("/api/users/forgot/password" , require('./routes/forgotPassword'))
;
app.post('/api/users/recover/account' , require('./routes/recoverAccount.js'));

app.post( '/api/users/reset/password',resetPassword);
app.post('/api/users/refresh', require('./routes/refresh'));

app.post('/api/users/signup/now', signup1);

//app.use(verifyJWT);
app.post('/api/users/logout',verifyJWT,require('./routes/logout'));
app.get('/api/employees',verifyJWT,require('./routes/api/employees'));
app.use('/api/users/:id/role',verifyJWT, require('./routes/api/users'));

app.get(  '/api/users/signup',signup);
// Routing Implement




app.use(errorHandler);


module.exports = app;
