
const Joi = require('joi');
const validateRequest = require('../middleware/validate-request');
const Role = require('../config/roles_list');
const bcrypt = require('bcrypt');
const fs = require('fs');
const User = require('../model/User')
const db = require('../_helpers/db');
const jwt = require('jsonwebtoken');


exports.getAll = (req, res, next) => {
  db.User.find({where:{username: req.body.username||  req.body.password}})
      .then((user) => {res.json(user)})

}

exports.getById = async (req, res, next) => {
   db.User.findOne( {where:{id: req.params.id}})
       .then(user => res.json(user))
       .catch(next);
}

exports.delete = (req, res, next) => {
   db.User.findByPk(req.params.id)
     .then(user =>{
       if (!user) {
         return res.status(401).json({ error: 'User not found!' });
       }
       user.destroy()
       .then(() => res.json({ message: 'User deleted' }))
       .catch(next);
     })
}

// schema functions

exports.createSchema = (req, res, next) => {
   const schema = Joi.object({
       username:     Joi.string().required(),
         middleName:  Joi.string().required(),
       firstName: Joi.string().required(),
       lastName: Joi.string().required(),
        email: Joi.string().email().required(),
       password: Joi.string().min(6).required(),
       confirmPassword: Joi.string().valid(Joi.ref('password')).required(),
       role: Joi.string().valid(Role.Admin, Role.User,Role.Editor).required()

   });
   validateRequest(req, next, schema);
}

exports.updateSchema = (req, res, next) => {
   const schema = Joi.object({

           username:     Joi.string().required(),
           middleName:  Joi.string().required(),
           firstName: Joi.string().required(),
           lastName: Joi.string().required(),
           email: Joi.string().email().required(),
           password: Joi.string().min(6).required(),
           confirmPassword: Joi.string().valid(Joi.ref('password')).required(),   }).with('password', 'confirmPassword');
   validateRequest(req, next, schema);
}