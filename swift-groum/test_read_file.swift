//
//  test_read_file.swift
//  helloworld
//
//  Created by sally on 2019/8/3.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
class Testfile{
   static func  loadJson(forFilename fileName: String) -> NSDictionary? {
    let url = NSURL(fileURLWithPath: fileName)
    var data = NSData(contentsOfFile: url.path!)
    let dictionary = try! JSONSerialization.jsonObject(with: data! as Data, options: .allowFragments) as? NSDictionary
    

   return dictionary
    }
    static func saveJson(fileName:String,data:[String:Any]){
        var file_man=FileManager.default
        file_man.createFile(atPath: fileName, contents: nil, attributes:nil)
        let file=FileHandle(forWritingAtPath: fileName)
        let jsondata=try! JSONSerialization.data(withJSONObject: data, options: JSONSerialization.WritingOptions())
        if file==nil{
            print("the file is nil")
        }
        var a=String(data: jsondata, encoding: String.Encoding.utf8)
        print("the data: ",a)
        file?.write(jsondata)
    }

}
