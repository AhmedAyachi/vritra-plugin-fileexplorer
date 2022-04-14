

class FilePickerPlugin:CDVPlugin {

    //Object callback
    func success(_ command:CDVInvokedUrlCommand,_ message:[AnyHashable:Any]?,_ keep:NSNumber=false){
        if(message==nil){
            success(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:[AnyHashable:Any]?,_ keep:NSNumber=false){
        if(message==nil){
            error(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }

    //Array callback
    func success(_ command:CDVInvokedUrlCommand,_ message:[Any]?,_ keep:NSNumber=false){
        if(message==nil){
            success(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:[Any]?,_ keep:NSNumber=false){
        if(message==nil){
            error(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }

    //Boolean callback
    func success(_ command:CDVInvokedUrlCommand,_ message:Bool?,_ keep:NSNumber=false){
        let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message==nil ?false:message!);
        if(!(result==nil)){
            result!.keepCallback=keep;   
        }
        self.commandDelegate.send(result,callbackId:command.callbackId);
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:Bool?,_ keep:NSNumber=false){
        let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message==nil ?false:message!);
        if(!(result==nil)){
            result!.keepCallback=keep;   
        }
        self.commandDelegate.send(result,callbackId:command.callbackId);
    }

    //Number callback
    func success(_ command:CDVInvokedUrlCommand,_ message:Double?,_ keep:NSNumber=false){
        if(message==nil){
            success(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:Double?,_ keep:NSNumber=false){
        if(message==nil){
            error(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func success(_ command:CDVInvokedUrlCommand,_ message:Int?,_ keep:NSNumber=false){
        if(message==nil){
            success(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:Int?,_ keep:NSNumber=false){
        if(message==nil){
            error(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }

    //String callback
    func success(_ command:CDVInvokedUrlCommand,_ message:String?,_ keep:NSNumber=false){
        if(message==nil){
            success(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_OK,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }
    func error(_ command:CDVInvokedUrlCommand,_ message:String?,_ keep:NSNumber=false){
        if(message==nil){
            error(command,false,keep);
        }
        else{
            let result=CDVPluginResult(status:CDVCommandStatus_ERROR,messageAs:message!);
            if(!(result==nil)){
                result!.keepCallback=keep;   
            }
            self.commandDelegate.send(result,callbackId:command.callbackId);
        }
    }

    class Error:LocalizedError {
        private var message:String="";
        init(_ message:String){
            self.message=message;
        }
        public var errorDescription:String?{
            return self.message; 
        }
    }
}
