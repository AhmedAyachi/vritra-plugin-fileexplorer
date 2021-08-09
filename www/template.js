const exec=require("cordova/exec");


module.exports={
    coolAlert:(message)=>{
        exec(alert,alert,"template","coolAlert",[message]);
    },
};
