//
//  FuncVisitor.swift
//  helloworld
//
//  Created by sally on 2019/8/2.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
import AST
import Parser
import Source
/*
 1.对每个function 建立groum MyVisitor
 2. 对每个groum节点收集变量列表 AddVarVisitor
    2.1 每个function 的所有变量名VarVisitor
    2.2 每个节点的所有变量名   所有的identifier与VarVisitor的变量交集
 3. 数据依赖 对groum同类型的节点的相同的变量名的交进行连线  func add_data_depend
 4. groum的function call的所有节点的方法名和类名修正
 */
class FuncVisitor : ASTVisitor {
    var groumList=[Groum]()
    //var test=Set<String>()
    var dict_var_class=[String:String]()//一个类的所有变量名和变量类型的字典
    init(dict_var_class:[String:String]){
        self.dict_var_class=dict_var_class
    }
    func addtest(groum:Groum)->Groum{
        groum.vertx_list.append(Node())
        return groum
    }
    func add_data_depend(groum:Groum)->Groum{
        for index in 0..<groum.vertx_list.count{
            for index2 in index..<groum.vertx_list.count{
                 var node_out=groum.vertx_list[index]
                 var node_in=groum.vertx_list[index2]
                 if node_out.node_type == node_in.node_type && node_out != node_in{
                    if !node_out.var_list.isDisjoint(with: node_in.var_list) && groum.edges.keys.contains(node_out) {
                        if (!groum.edges[node_out]!.contains(node_in)){
                            groum.edges[node_out]!.append(node_in)
                        }
                    }
                }
            }
                
        }
        return groum
    }
        
    
    func visit(_ funcDecl: FunctionDeclaration) throws -> Bool {
        print(">>>>>>>>>>>>>>>>>begin parse function***********: ",funcDecl.name)
        var funBod=funcDecl.body
        var fun_vis=MyVisitor()
        if funBod != nil{
            //建立groum
            try fun_vis.traverse(funBod!)
            
            print("*************begin one function*****************")
            print(fun_vis.groum.print_groum())
            print("*************end one function*****************")
            
            //收集funBod所有的变量
            var var_vis=VarVisitor()
            try var_vis.traverse(funBod!)
           print( "******iden*************",var_vis.iden_name_list)
            //收集funDeclare的形参变量
            var para_list=funcDecl.signature.parameterList
            for para in para_list{
              // print("*************para local name: ",para.localName)
                var_vis.var_name_list.insert(para.localName.textDescription)
            }
            //print()
            //添加groum每个节点的变量
            var add_vis=AddVarVisitor(groum: fun_vis.groum,base_var_list: var_vis.var_name_list)
            try add_vis.traverse(funBod!)
            print("********begin var add************")
            print("********local name list*********",var_vis.var_name_list)
            
            //添加数据依赖
            add_vis.groum = add_data_depend(groum:add_vis.groum)
            print("*******data dependency*************")
            add_vis.groum.print_groum()
            
            //所有节点的方法名和类名修正
            var addclass_vis=AddClassVisitor(groum: add_vis.groum,dict_var_class:dict_var_class)
            addclass_vis.add_class_remain()
           
            print("*************final groum*******************")
            addclass_vis.newgroum.print_groum()
            addclass_vis.newgroum.groum_name=funcDecl.name.textDescription
            groumList.append(addclass_vis.newgroum)
            //addclass_vis.newgroum.print_groum_new()
        }
        print("<<<<<<<<<<<<<<<<<<<<end parse function***********: ",funcDecl.name.textDescription)
        return true
    }
}
