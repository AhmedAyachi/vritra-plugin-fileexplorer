const proxy=require("cordova/exec/proxy");


module.exports={
    coolAlert:alert,
}

proxy.add("Template",module.exports);
