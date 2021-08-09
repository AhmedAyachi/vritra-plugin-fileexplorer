const exec=require("cordova/exec");


module.exports={
    coolAlert:(message)=>{
        exec(alert,alert,"Custom","coolAlert",[message]);
    },
};
