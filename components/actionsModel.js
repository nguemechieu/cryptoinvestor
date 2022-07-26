
let message="No message";


exports._getAll = (model) => {
    return model.findAll();
}

exports._getById = async (model, id) =>{
    const user = await model.findByPk(id);
    if (!user) throw  message='User not found!';
    return user;
}

exports._getByUserName=async(model,username)=>{

    const user = await model.findByPk(username);
    if (!user.username) throw message='User not found !';
    return user;


}
exports._getOrderById=async(model,order)=>{

    const myOrder = await model.findByPk(order);
    if (! myOrder ) throw 'Order not found!';
    return  myOrder ;


}
