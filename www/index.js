const exec=require("cordova/exec");


module.exports={
    show:(props)=>{
        const {onPick}=props;
        exec(onPick,null,"FilePicker","show",[props]);
    },
};
