//
//  VarVisitor.swift
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
 获得指定  代码片段的 所有的变量
 变量声明和常量声明
 if语句中也会嵌入变量声明
 
 标识符中： 在变量声明和常量声明中也加入了标识符 如果这些变量没有使用需要加入。一般情况下 是需要使用的 
 */
class VarVisitor : ASTVisitor {
    var var_name_list=Set<String>()
    var  iden_name_list=Set<String>()
    /*func traverse(_ statement: Statement) throws -> Bool {
        switch statement{
        
        }
    }*/
    func visit(_ ifStmt: IfStatement) throws -> Bool {
       var con_list=ifStmt.conditionList
        for con in con_list{
            switch con {
                
                case let .let(pattern, expr):
                    var_name_list.insert(pattern.textDescription)
                  //print("&&&&&&&let: ","\(pattern) ~~~ \(expr)")
                  try self.traverse(expr)
                    break
                case let .var(pattern, expr):
                    var_name_list.insert(pattern.textDescription)
                  //return "var \(pattern) = \(expr)"
                    break
                default:
                    break
            }
           // print("&&&&&&&&&condition description: ",con.textDescription)
        }
        return true
    }

    func visit(_ ideti: IdentifierExpression) throws -> Bool{
        switch ideti.kind {
            case let .identifier(id, generic):
                iden_name_list.insert(id.textDescription)
               //var_name_list.insert(id.textDescription)
              //print("IdentifierExpression: ","\(id)\(generic?.textDescription ?? "")")
                break
            case let .implicitParameterName(i, generic):
            // print("implicitParameterName: ","$\(i)\(generic?.textDescription ?? "")")
                break
            case let .bindingReference(refVar):
            // print("implicitParameterName: ","$\(refVar)")
                break
            }
      //  print("IdentifierExpression: ",ideti.lexicalParent?.description,ideti.textDescription)
        return true
    }
    func visit(_ con_dec: ConstantDeclaration) throws -> Bool{
        var in_list=con_dec.initializerList
        for ini in in_list{
            var_name_list.insert(ini.pattern.textDescription)
            iden_name_list.insert(ini.pattern.textDescription)
           // print("&&&&&&&Con_pat: ",ini.pattern.textDescription)
        }
        //print("&&&&&&&&&con_dec: ",con_dec.textDescription)
        return true
    }

    func visit(_ var_dec: VariableDeclaration) throws -> Bool{
        var var_body=var_dec.body
        switch var_body {
            case .initializerList(let inits):
                for ini in inits{
                    iden_name_list.insert(ini.pattern.textDescription)
                    var_name_list.insert(ini.pattern.textDescription)
                   print("&&&&&&&&&variable: ", ini.pattern)
                }
              //print("&&&&&&&&&variable: ",inits.map({ $0.textDescription }).joined(separator: ", "))
        default:
            break
            }
       // print("&&&&&&&&&variable: ",var_dec.textDescription)
        return true
    }
    
    
    
}
