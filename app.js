let createError = require('http-errors'), express = require('express'), path = require('path'),
    cookieParser = require('cookie-parser'), indexRouter = require('./routes/index'),
    usersRouter = require('./routes/api/users'), app = express();


const cors = require('cors');
const corsOptions = require('./config/corsOptions');
const { logger } = require('./middleware/logEvents');
const verifyJWT = require('./middleware/verifyJWT');
const credentials = require('./middleware/credentials');
const swaggerJsDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const errorHandler = require("./middleware/errorHandler");


// Initialize documentation module with SwaggerJsdoc
const swaggerOptions = {
  swaggerDefinition: {
    info: {
      title: 'CryptoInvestor API',
      description: "Trade Management Application ",
      contact: {
        name: "TechExperts",
      },
      servers: ["http://localhost:4000"]
    }
  },
  //['.routes/*.js']
  apis: ["./bin/www.js"]
}
const swaggerDocs = swaggerJsDoc(swaggerOptions);



app.use(express.json());
app.use((req, res, next) => {
  // acceder a notre API depuis n'importe quelle origine
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content, Accept, Content-Type, Authorization');
  // envoyer des requettes avec les méthodes GET, POST, PUT, DELETE, PATCH, OPTION
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  next();
});



// custom middleware logger
app.use(logger);
// Handle options credentials check - before CORS!
// and fetch cookies credentials requirement
app.use(credentials);

app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// Cross Origin Resource Sharing
app.use(cors(corsOptions));

// built-in middleware to handle urlencoded form data
app.use(express.urlencoded({ extended: true }));

// built-in middleware for json
app.use(express.json());

//middleware for cookies
app.use(cookieParser());
// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger);
app.use(express.json());
app.use(express.urlencoded({ extended: true}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

app.use('/forgotPassword', require('./routes/forgotPassword'));

app.use(verifyJWT)
app.use('/users', usersRouter);

app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerDocs))

app.use('/register', require('./routes/index'));

app.use('/auth/login', require('./routes/auth'));
app.use('/auth/register', require('./routes/index'))



// routes


app.use('/refresh', require('./routes/refresh'));
app.use('/logout', require('./routes/logout'));

app.use('/employees', require('./routes/api/employees'));
app.use('/users', require('./routes/api/users'));


app.all('*', (req, res) => {
  res.status(404);
  if (req.accepts('ejs')) {
    res.sendFile(path.join(__dirname, 'views', '404.ejs'));
  } else if (req.accepts('json')) {
    res.json({ "error": "404 Not Found" });
  } else {
    res.type('txt').send("404 Not Found");
  }
});

app.all('*', (req, res) => {
  res.status(404);
  if (req.accepts('ejs')) {
    res.sendFile(path.join(__dirname, 'public/views', '404.ejs'));
  } else if (req.accepts('json')) {
    res.json({ "error": "404 Not Found" });
  } else {
    res.type('txt').send("404 Not Found");
  }
});

app.use(errorHandler);
module.exports = app;
