//
//  main.swift
//  helloworld
//
//  Created by sally on 2019/7/26.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
import AST
import Parser
import Source
print("i come")
do {
  
    var dict_project_vars=Testfile.loadJson(forFilename: "/Users/sally/Downloads/groum_extractor-master/mycode/sil-json/swipe-ios-master.json")
    var s = dict_project_vars!["SwipePath"] as! NSDictionary
    print("all vars: ",s)
    var groum_save = [String:Any]()
   
    for class_name in dict_project_vars!.allKeys{
        var cla_name=class_name as! String
        var read_filename="/Users/sally/Downloads/groum_extractor-master/mycode/"+cla_name+".swift"
        var file_man=FileManager.default
        if file_man.fileExists(atPath: read_filename){
            let sourceFile = try SourceReader.read(at:read_filename)//SwipePath_test test_fun.swift swift_control_vars.swift test_vars.swift  SwipePath testDatadependency.swift switch_case repeat_while
            
            let parser = Parser(source: sourceFile)
            let topLevelDecl = try parser.parse()
            var func_vis=FuncVisitor(dict_var_class: s as! [String : String])
            try func_vis.traverse(topLevelDecl)
            var me_groum=[String:Any]()
            for groum in func_vis.groumList{
                
                var a=[String:Any]()
                var  vers=groum.vertx_list
                var vertexs_save = [Any]()
                var edge_save=[String:String]()
                for ver in vers{
                    if ver.node_type{
                        vertexs_save.append([ver.get_sole_name(),ver.class_name,ver.remain_name,ver.node_type,ver.get_node_in_list_name(),ver.get_node_out_list_name()])
                    }
                    else{
                        vertexs_save.append([ver.get_sole_name(),ver.class_name,ver.remain_name,ver.node_type,[String](),[String]()])
                    }
                }
                
                for (key,value) in groum.edges{
                    for each_value in value{
                        edge_save[key.get_sole_name()]=each_value.get_sole_name()
                    }
                }
                a["vertxs"]=vertexs_save
                a["edges"]=edge_save
                me_groum[groum.groum_name]=a
            }
            groum_save[cla_name]=me_groum
        } 
    }
    Testfile.saveJson(fileName: "/Users/sally/Downloads/groum_extractor-master/mycode/test_save.json", data: groum_save)
} catch {
  print("erro")// handle errors
}


