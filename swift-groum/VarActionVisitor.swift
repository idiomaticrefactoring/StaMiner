//
//  VarActionVisitor.swift
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
 用来处理 x=o.m  只截取第一个functiona call
 */
class VarActionVisitor : ASTVisitor {
    var groum = Groum()
    var count=0
   /* func visit(_ expmem: ExplicitMemberExpression)throws -> Bool {
           // print("ExplicitMemberExpression  lexicalParent",expmem.lexicalParent?.textDescription)
            //print("Come ExplicitMemberExpression: ",expmem.textDescription)
            var acno=ActionNode()
            acno.ast_node=expmem as! ASTNode
            acno.me_name=expmem.textDescription
        groum.vertx_list.append(acno)
        return true
    }*/
    func visit(_ funcalexp: FunctionCallExpression)throws -> Bool {
        if  count < 1{
            let args=funcalexp.argumentClause
            let pos_exp=funcalexp.postfixExpression
            
            
            var acno=ActionNode()
            acno.ast_node=funcalexp
            acno.me_name=pos_exp.textDescription
            groum.vertx_list.append(acno)
            count=count+1
        }
        
        return true
    }
}
