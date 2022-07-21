
const User = require('../model/User');
const path = require("path");
const getAllUsers = async (req, res) => {
    const user = await User.findOne({ _id: req.body.id})
    if (!user) return res.status(204).json({ 'message': 'No users found' });
    res.json(user);
}

const deleteUser = async (req, res) => {
    if (!req?.body?.id) return res.status(400).json({ "message": 'User ID required' });
    const user = await User.findOne({ _id: req.body.id }).exec();
    if (!user) {
        return res.status(204).json({ 'message': `User ID ${req.body.id} not found` });
    }
    const result = await User.deleteOne({ _id: req.body.id });
    res.json(result);
}

const getUser = async (req, res) => {
    if (!req?.params?.id) return res.status(400).json({ "message": 'User ID required' });
    const user = await User.findOne({ _id: req.params.id }).exec();
    if (!user) {
        return res.status(204).json({ 'message': `User ID ${req.params.id} not found` });
    }
    res.json(user);
}
exports. homePage =async (req, res) => {
    res.sendFile(path.join(__dirname, '..', 'views', 'homePage.html'));

}
module.exports = {
    getAllUsers,
    deleteUser,
    getUser
}