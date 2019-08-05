//
//  Node.swift
//  helloworld
//
//  Created by sally on 2019/7/31.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
import AST
import Parser
import Source
public class Node  {
    var id=0
    public var ast_node : ASTNode?
    public var node_flag=false
    public var node_in=false
    public var node_out=false
    public var node_type:Bool
    public var class_name=""
    public var remain_name=""
    public var me_name=""
    public var var_list = Set<String>()
    var node_in_list=[Node]()
        var node_out_list=[Node]()
    public func get_node_name()->String{
        return class_name+remain_name
    }
    public func get_sole_name()->String{
        return me_name+"_id"+String(id)+"_loc"+String(ast_node!.sourceRange.start.column)
        
    }
    public func get_node_in_list_name()->[String]{
        var a=[String]()
        for ver in node_in_list{
            a.append(ver.get_sole_name())
        }
        return a
    }
    public func get_node_out_list_name()->[String]{
            var a=[String]()
            for ver in node_out_list{
                a.append(ver.get_sole_name())
            }
        return a
        }
    /*func hash(into hasher: inout Hasher) {
        hasher.combine(class_name)
        hasher.combine(me_name)
    }
    var hashValue:Int{
        return (String(class_name)+String(me_name)).hashValue
    }
    static func == (lhs:Node,rhs:Node)->Bool{
        return lhs.class_name == rhs.class_name && lhs.me_name == rhs.me_name
        //return lhs.hashValue==rhs.hashValue
    }*/
    public init(){
        node_type=false
    }
}
 



extension Node:Equatable{
    public static func == (lhs:Node,rhs:Node)->Bool{
        
        return lhs.id == rhs.id && lhs.me_name == rhs.me_name && lhs.ast_node?.sourceRange.start == rhs.ast_node?.sourceRange.start && lhs.node_type == rhs.node_type
        //return lhs.ast_node?.sourceRange.end == rhs.ast_node?.sourceRange.end && lhs.ast_node?.sourceRange.start == rhs.ast_node?.sourceRange.start && lhs.node_type == rhs.node_type
            //return lhs.hashValue==rhs.hashValue
        }
}
extension Node:Hashable{
    public var hashValue:Int{
        return id.hashValue ^ me_name.hashValue ^ (ast_node?.sourceRange.start.hashValue)! ^ node_type.hashValue
        //return (ast_node!.sourceRange.end.hashValue) ^ (ast_node?.sourceRange.start.hashValue)! ^ node_type.hashValue
    }
    
}
