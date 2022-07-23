require('dotenv').config();
const  express = require('express'), path = require('path'),

    root= require('./routes/root'),
    cookieParser = require('cookie-parser'),
        signup1= require('./routes/register'),
     app = express();
const bodyParser = require('body-parser');
const cors = require('cors');
const corsOptions = require('./config/corsOptions');
const { logger } = require('./middleware/logEvents');
const verifyJWT = require('./middleware/verifyJWT');
const credentials = require('./middleware/credentials');
const swaggerJsDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const errorHandler = require("./middleware/errorHandler");
const {login} = require("./controllers/loginController");
const {resetPassword} = require("./controllers/resetPassword");

let  nodemailer = require("nodemailer")
const db = require("./_helpers/db");


// Initialize documentation module with SwaggerJsdoc
const swaggerOptions = {
  swaggerDefinition: {
    info: {
      title: 'CryptoInvestor API',
      description: "Trade Management Application ",
      contact: {
        name: "CryptoInvestor",
      },
      servers: ["http://localhost:4000"]
    }
  },

  router: ['./routes/*.js'],
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

// Handle options credentials check - before CORS!
// and fetch cookies credentials requirement
app.use(credentials);

// Cross Origin Resource Sharing
app.use(cors(corsOptions));

// built-in middleware to handle urlencoded form data
app.use(express.urlencoded({ extended: false }));

// built-in middleware for json
app.use(express.json());

//middleware for cookies
app.use(cookieParser());
app.use(bodyParser.urlencoded({ extended: false }))
//serve static files
app.use('/', express.static(path.join(__dirname, '/public')));

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');
app.get("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerDocs))
app.get('/',root)

app.use('/auth/login', login);

app.post(  '/signup',(req,res) => {

  res.render('register.ejs', { title : 'Registration'})


});



app.post("/forgotPassword" , require('./routes/forgotPassword'))
;
app.post( '/resetPassword',resetPassword);
app.use('/refresh', require('./routes/refresh'));

app.post('/auth/signup', signup1);
app.post('/logout', require('./routes/logout'));
app.get('/employees',require('./routes/api/employees'));
app.get('/users/:userId/roles', require('./routes/api/users'));





app.all('*', (req, res) => {
  res.status(404);
  if (req.accepts('ejs')) {
    res.render('404.ejs', {accepts: req.accepts('ejs')});
  } else if (req.accepts('json')) {
    res.json({ "error": "404 Not Found" });
  } else {
    res.type('txt').send("404 Not Found");
  }
});

app.use(errorHandler);
module.exports = app;
