

const db = require("../_helpers/db");

const getAllUsers = async (req, res,next) => {
    const users = await db.User.findAll();
    if (!users) return res.status(204).json({ 'message': 'No users found' });
    res.json(users);
}

const deleteUser = async (req, res,next) => {
    if (!req?.body?.id) return res.status(400).json({ "message": 'User ID required' });
    const user = await db.User.findOne({ id: req.body.id }).exec();
    if (!user) {
        return res.status(204).json({ 'message': `User ID ${req.body.id} not found` });
    }
    const result = await user.delete({ id: req.body.id });
    res.json(result);
}

const getUser = async (req, res,next) => {
    if (!req?.body?.username) return res.status(400).json({ "message": 'User Username required' });
    const user = await db.User.findOne({ username: req.body.username }).exec();
    if (!user) {
        return res.status(204).json({ 'message': `User ID ${req.body.username} not found` });
    }
    res.json(user);
}
const getUserById=async (req, res,next) => {
    if (!req?.params?.id) return res.status(400).json({ "message": 'User ID required' });
    const user = await db.User.findOne({ id: req.params.id }).exec();
    if (!user) {
        return res.status(204).json({ 'message': `User ID ${req.params.id} not found` });
    }
    res.json(user);

}
module.exports = {
    getUserById,
    getAllUsers,
    deleteUser,
    getUser
}