//
//  AddVar.swift
//  helloworld
//
//  Created by sally on 2019/8/2.
//  Copyright © 2019 sally. All rights reserved.
//

import AST
import Parser
import Source
/*
 为控制节点 和actionnode 添加变q
 1.控制节点 访问其所有代码中变量 这里变量可能不涉及到变量声明但涉及到变量使用 所以用functionbody所有的变量list和控制节点代码片段的标识符列表做交集
 2. actionode  1. 方法调用中 变量名和形参名列表   2. x=o.m()的情况 （变量声明 或者 AssignmentOperatorExpression语句）
 */
class AddVarVisitor : ASTVisitor {
    var groum=Groum()
    var base_var_list=Set<String>()
    init(groum:Groum,base_var_list:Set<String>) {
        self.groum=groum
        self.base_var_list=base_var_list
    }
    func visit(_ switchStmt: SwitchStatement) throws -> Bool{
       var con_node=ControlNode()
       con_node.ast_node=switchStmt
       con_node.me_name="SWITCH"
       con_node.class_name="SWITCH"
        var varvis=VarVisitor()
                         if groum.vertx_list.contains(con_node){
                            
                             try varvis.traverse(switchStmt)
                              groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].var_list=varvis.iden_name_list.intersection(base_var_list)
                         }
        return true
    }
   func visit(_ repeaStmt: RepeatWhileStatement) throws -> Bool{
           var con_node=ControlNode()
           con_node.ast_node=repeaStmt
           con_node.me_name="REPEAT"
           con_node.class_name="REPEAT"
          var varvis=VarVisitor()
                   if groum.vertx_list.contains(con_node){
                       try varvis.traverse(repeaStmt)
                        print("******************test_Repeat vars: ",varvis.iden_name_list)
                        groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].var_list=varvis.iden_name_list.intersection(base_var_list)
                   }
    return true
    }
    func visit(_ forStmt: ForInStatement) throws -> Bool{
         var con_node=ControlNode()
         con_node.ast_node=forStmt
         con_node.me_name="For"
         con_node.class_name="For"
        var varvis=VarVisitor()
        if groum.vertx_list.contains(con_node){
            try varvis.traverse(forStmt)
             groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].var_list=varvis.iden_name_list.intersection(base_var_list)
        }
      
     return true
     }
     
     func visit(_ whiStmt: WhileStatement) throws -> Bool{
         var con_node=ControlNode()
             con_node.ast_node=whiStmt
             con_node.me_name="WHILE"
        con_node.class_name="WHILE"
         var varvis=VarVisitor()
                 if groum.vertx_list.contains(con_node){
                     try varvis.traverse(whiStmt)
                      groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].var_list=varvis.iden_name_list.intersection(base_var_list)
                 }
         return true
     }
      func visit(_ ifStmt: IfStatement) throws -> Bool {
      var con_node=ControlNode()
      con_node.ast_node=ifStmt
      con_node.me_name="IF"
      con_node.class_name="IF"
      var varvis=VarVisitor()
     if groum.vertx_list.contains(con_node){
        //print("---------------Come here----------------")
                          try varvis.traverse(ifStmt)
       // print("---------------varvis.var_name_list---------------: ",varvis.var_name_list)
                           groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].var_list=varvis.iden_name_list.intersection(base_var_list)
                      }
            return true
        }
        
    /* 填充方法调用和变量使用涉及的变量
         一般情况是 分配表达式
            有时是 变量或者常量的声明
     */
   
    func visit(_ expr: AssignmentOperatorExpression) throws -> Bool {
        //print("+++++++++++++++AssignmentOperatorExpression: ",expr.textDescription)
        var lef_exp=expr.leftExpression
        var rig_exp=expr.rightExpression
        var acno1=ActionNode()
        acno1.me_name = lef_exp.textDescription
        acno1.ast_node=lef_exp as! ASTNode
       // print("+++++++++++++++: ",lef_exp.textDescription)
        if groum.vertx_list.contains(acno1){
            print("+++++++++++++++It's important for field")
            var field_vis=VarVisitor()
            try field_vis.traverse(rig_exp)
            groum.vertx_list[groum.vertx_list.firstIndex(of: acno1)!].var_list=groum.vertx_list[groum.vertx_list.firstIndex(of: acno1)!].var_list.union(field_vis.iden_name_list.intersection(base_var_list))
        }
       
       
        var acno2=ActionNode()
        acno2.ast_node=rig_exp as! ASTNode
        acno2.me_name=String(rig_exp.textDescription.split(separator: "(")[0])
        //print("+++++++++++++++: ",rig_exp.textDescription)
         if groum.vertx_list.contains(acno2){
            //print("+++++++++++++++It's important for method")
            var func_vis=VarVisitor()
            try func_vis.traverse(lef_exp)
            groum.vertx_list[groum.vertx_list.firstIndex(of: acno2)!].var_list=groum.vertx_list[groum.vertx_list.firstIndex(of: acno2)!].var_list.union( func_vis.iden_name_list.intersection(base_var_list))

        }
        return true
    }
    //对于变量和常量的声明，将 额外的变量加入其中
    func add_cons_var(in_list:[PatternInitializer]){
        for ini in in_list{
                        //ini.initializerExpression
                        if ini.initializerExpression != nil{
                            var  var_act_vis=VarActionVisitor()
                            try! var_act_vis.traverse(ini.initializerExpression!)
                            for ver in groum.vertx_list{
                                for action in var_act_vis.groum.vertx_list{
                                    if ver==action{
                                        groum.vertx_list[groum.vertx_list.firstIndex(of: ver)!].var_list.insert(ini.pattern.textDescription)
                                    }
                                }
                            }
                           // print("+++++++++++++++ini: ",ini.textDescription)
                        }
                    }
    }
    func visit(_ con_dec: ConstantDeclaration) throws -> Bool{
            //var cons_vis=AddVarVisitor(groum: groum, base_var_list: Set<String>())
            
            var in_list=con_dec.initializerList
            add_cons_var(in_list: in_list)
            return true
        }

        func visit(_ var_dec: VariableDeclaration) throws -> Bool{
            var var_body=var_dec.body
            switch var_body {
                case .initializerList(let inits):
                    add_cons_var(in_list: inits)
            default:
                break
                }
            return true
        }
        func visit(_ funcalexp: FunctionCallExpression)throws -> Bool {
        
        var acno=ActionNode()
        acno.ast_node=funcalexp
        let pos_exp=funcalexp.postfixExpression
        acno.me_name=pos_exp.textDescription
        //acno.me_name=funcalexp.textDescription
        var func_vis=VarVisitor()
        try func_vis.traverse(funcalexp)
        groum.vertx_list[groum.vertx_list.firstIndex(of: acno)!].var_list=groum.vertx_list[groum.vertx_list.firstIndex(of: acno)!].var_list.union(func_vis.iden_name_list.intersection(base_var_list))

            return true
        }
    func visit(_ expmem: ExplicitMemberExpression)throws -> Bool {
       // print("ExplicitMemberExpression  lexicalParent",expmem.lexicalParent?.textDescription)
        //print("Come ExplicitMemberExpression: ",expmem.textDescription)
        var acno=ActionNode()
        acno.ast_node=expmem as! ASTNode
        acno.me_name=expmem.textDescription
        if groum.vertx_list.contains(acno){
            var var_name=String(expmem.textDescription.split(separator: ".")[0])
            if base_var_list.contains(var_name){
                groum.vertx_list[groum.vertx_list.firstIndex(of: acno)!].var_list.insert(var_name)
            }
        }
        return true
    }
}
