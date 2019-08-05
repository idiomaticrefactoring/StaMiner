
//
//  AddClassVisitor.swift
//  helloworld
//
//  Created by sally on 2019/8/3.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
import AST
import Parser
import Source
/*
 用来替换方法名为类名xx.m  中替换xx为类名
 */
class AddClassVisitor : ASTVisitor {
    var groum=Groum()
    var newgroum=Groum()
    var dict_var_class=[String:String]()
    init(groum:Groum,dict_var_class:[String:String]) {
            self.groum=groum
            self.dict_var_class=dict_var_class
    }
    
    //修改node的class_name    remain_name
    func add_class_remain(){
        for node in groum.vertx_list{
            //var node=groum.vertx_list[index!]
            var index=groum.vertx_list.firstIndex(of: node)
            var pre_str=""
            var pos_str=""
            var array=node.me_name.split(separator: ".")
            //提取class_name    remain_name
            if array.count>1{
                pre_str=String(array[0])
                pos_str=array[1...].joined(separator: ".")
                
            }
            else{
                pre_str=String(array[0])
                pos_str=String(array[0])
            }
            if dict_var_class.keys.contains(String(array[0])) {
                node.class_name=dict_var_class[String(array[0])]!
                node.remain_name=pos_str
            }
            else{
                node.class_name=pre_str
                if !node.node_type{
                    node.remain_name=pos_str
                }
                
            }
            print("+++++++++++++++++++++++++++++node_name: ",node.get_node_name())
            //修改groum的node
            //groum.vertx_list[index!]=node
            newgroum.vertx_list.append(node)
            //print("&&&&&&&&&&&&&&&&&&&&modify node: ",node.get_node_name(),node.me_name)
        }
        for (key,value) in groum.edges{
                    for each_value in value{
                        var new_key=newgroum.vertx_list[newgroum.vertx_list.firstIndex(of: key)!]
                        var new_value=newgroum.vertx_list[newgroum.vertx_list.firstIndex(of: each_value)!]
                        if !newgroum.edges.keys.contains(key){
                                                newgroum.edges[key]=[]
                        }
                        newgroum.edges[new_key]!.append(new_value)
                    }
            }
    }
   
}
