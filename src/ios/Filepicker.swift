import Foundation;
import UniformTypeIdentifiers;


class Filepicker:FilePickerPlugin,UIDocumentPickerDelegate {

    var multiple=true;
    var showCommand:CDVInvokedUrlCommand?=nil;

    @objc(show:)
    func show(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? [AnyHashable:Any];
        if !(argument==nil){
            let props=argument!;
            self.showCommand=command;
            var pickerVC:UIDocumentPickerViewController;
            let type=props["type"] as? String ?? "*/*";
            if #available(iOS 14,*){
                let types=UTType.types(
                    tag:type,
                    tagClass:UTTagClass.mimeType,
                    conformingTo:nil
                );
                pickerVC=UIDocumentPickerViewController(forOpeningContentTypes:types,asCopy:false);
            }
            else {
                pickerVC=UIDocumentPickerViewController(documentTypes:"".split(separator:",").map({String($0)}),in:UIDocumentPickerMode.open);
            }
            self.multiple=props["multiple"] as? Bool ?? true;
            pickerVC.allowsMultipleSelection=multiple;
            pickerVC.delegate=self;
            self.viewController.present(pickerVC,animated:true);
        }
    }

    func documentPicker(_ pickerVC:UIDocumentPickerViewController,didPickDocumentsAt:[URL]){
        var entries:[[String:Any?]]=[];
        didPickDocumentsAt.forEach({url in
        let path=url.path;
        var entry:[String:Any?]=[
                "name":url.lastPathComponent,
                "path":path,
                "location":path.replacingCharacters(in:(path.lastIndex(of:"/") ?? path.startIndex)...path.index(before:path.endIndex),with:""),
                "absolutePath":"file://\(url.absoluteURL.path)",
                "canonicalPath":url.absoluteURL.path,
                "lastModified":0,
                "size":0,
            ];
            let resource=try? url.resourceValues(forKeys:[.fileSizeKey,.contentModificationDateKey]);
            if !(resource==nil){
                entry["lastModified"]=(resource!.contentModificationDate?.timeIntervalSince1970 ?? 0)*1000;
                entry["size"]=resource!.fileSize;
            }
            entries.append(entry);
        });
        multiple ? success(showCommand!,entries):success(showCommand!,entries[0]);
    }
        
    func documentPickerWasCancelled(_ pickerVC:UIDocumentPickerViewController){
        
    }

    @objc(useFileType:)
    func useFileType(command:CDVInvokedUrlCommand){
        let argument=command.arguments[0] as? String;
        if !(argument==nil){
            let path:NSString=argument as! NSString;
            if #available(iOS 14,*){
                let mimeType=UTType(filenameExtension:path.pathExtension)?.preferredMIMEType ?? "";
                success(command,mimeType);
            }
            else{
                success(command,path.mimeType());
            }
        }
    }
}