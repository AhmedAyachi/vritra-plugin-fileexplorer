const exec=require("cordova/exec");

exports={
    coolAlert:(message)=>{
        exec(alert,alert,"Custom","coolAlert",[message]);
    },
};

