//
//  test_ast.swift
//  helloworld
//
//  Created by sally on 2019/7/26.
//  Copyright © 2019 sally. All rights reserved.
//
import AST
import Parser
import Source

class MyVisitor : ASTVisitor {
    var groum = Groum()
   
    func set_con_node(yes_flag_out:Bool,yes_flag_in:Bool,con_node:Node){
            if !self.groum.vertx_list.contains(con_node){
                           self.groum.vertx_list.append(con_node)
            }
           if yes_flag_out{
               self.groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].node_out=true
           }
           if yes_flag_in{
                          self.groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].node_in=true
           }
                       
        }
    //X=>Y=>REPEAT=>Y
    func visit(_ repeaStmt: RepeatWhileStatement) throws -> Bool{
        var repea_codeblo=repeaStmt.codeBlock.statements
        var cond_exp=repeaStmt.conditionExpression
        var con_node=ControlNode()
        con_node.ast_node=repeaStmt
        con_node.me_name="REPEAT"
        con_node.class_name="REPEAT"
        if !self.groum.vertx_list.contains(con_node){
                                               self.groum.vertx_list.append(con_node)
        }
        var rep_coblo_groum=Groum()
        rep_coblo_groum=make_block(stats:repea_codeblo)
        var con_vis_1=MyVisitor()
        try con_vis_1.traverse(cond_exp)
        rep_coblo_groum=merge_g1_g2(g1:rep_coblo_groum , g2: con_vis_1.groum)
        var yes_flag_in=merge_in(groum2: rep_coblo_groum, node: con_node)
        con_node.node_in_list.append(contentsOf: rep_coblo_groum.vertx_list)
        var con_vis_2=MyVisitor()
        try con_vis_2.traverse(cond_exp)
        con_vis_2.groum.mod_groum_node_id(i: 1)
        var  yes_flag_out=merge_out(node: con_node, groum1: con_vis_2.groum)
        con_node.node_out_list.append(contentsOf: con_vis_2.groum.vertx_list)
        set_con_node(yes_flag_out: yes_flag_out,yes_flag_in: yes_flag_in,con_node: con_node)
        return true
        
    }
    func visit(_ switchStmt: SwitchStatement) throws -> Bool{
        var con=switchStmt.expression
        var con_node=ControlNode()
        con_node.ast_node=switchStmt
        con_node.me_name="SWITCH"
        con_node.class_name="SWITCH"
        if !self.groum.vertx_list.contains(con_node){
                                       self.groum.vertx_list.append(con_node)
                        }
        var vis_switch_coll=MyVisitor()
        try vis_switch_coll.traverse(con)
        var yes_flag_in=merge_in(groum2: vis_switch_coll.groum, node: con_node)
        con_node.node_in_list.append(contentsOf: vis_switch_coll.groum.vertx_list)
        var cases=switchStmt.cases
        var all_case_groum=Groum()
        for each_case in cases{
            var case_groum=Groum()
            switch each_case {
                
                case let .case(itemList, stmts):
                    case_groum=make_block(stats:stmts)
                case .default(let stmts):
                  case_groum=make_block(stats:stmts)
                
            }
            all_case_groum=merge(groum1: all_case_groum,groum2: case_groum)
        }
        var  yes_flag_out=merge_out(node: con_node, groum1: all_case_groum)
        con_node.node_out_list.append(contentsOf: all_case_groum.vertx_list)
        set_con_node(yes_flag_out: yes_flag_out,yes_flag_in: yes_flag_in,con_node: con_node)
        return true
           
       }
   // for  collec=>for=>statments
   func visit(_ forStmt: ForInStatement) throws -> Bool{
        var con_node=ControlNode()
        con_node.ast_node=forStmt
        con_node.me_name="For"
        con_node.class_name="For"
        if !self.groum.vertx_list.contains(con_node){
                               self.groum.vertx_list.append(con_node)
                }
        var for_coll=forStmt.collection
        var vis_fo_coll=MyVisitor()
        try vis_fo_coll.traverse(for_coll)
        var yes_flag_in=merge_in(groum2: vis_fo_coll.groum, node: con_node)
        var for_cod_blo=forStmt.codeBlock
        var for_codbloc_groum=Groum()
    for_codbloc_groum=make_block(stats:for_cod_blo.statements)
        var  yes_flag_out=merge_out(node: con_node, groum1: for_codbloc_groum)
        set_con_node(yes_flag_out: yes_flag_out,yes_flag_in: yes_flag_in,con_node: con_node)
    return true
    }
    
    func visit(_ whiStmt: WhileStatement) throws -> Bool{
        var con_node=ControlNode()
            con_node.ast_node=whiStmt
            con_node.me_name="WHILE"
            con_node.class_name="WHILE"
        if !self.groum.vertx_list.contains(con_node) {
            if !self.groum.vertx_list.contains(con_node){
                                                   self.groum.vertx_list.append(con_node)
                                    }
            let con=whiStmt.conditionList
            var vis_con=MyVisitor()
            con_vist(con: con,vis_con: vis_con)
            var yes_flag_in=merge_in(groum2: vis_con.groum, node: con_node)
            con_node.node_in_list.append(contentsOf: vis_con.groum.vertx_list)
            let code_blo=whiStmt.codeBlock
            var whi_codbloc=Groum()
            whi_codbloc=make_block(stats:code_blo.statements)
            var  yes_flag_out=merge_out(node: con_node, groum1: whi_codbloc)
            con_node.node_out_list.append(contentsOf: whi_codbloc.vertx_list)
            set_con_node(yes_flag_out: yes_flag_out,yes_flag_in: yes_flag_in,con_node: con_node)
           /* if !self.groum.vertx_list.contains(con_node){
                self.groum.vertx_list.append(con_node)
            }
            if yes_flag_out{
                self.groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].node_out=true
            }
            if yes_flag_in{
                           self.groum.vertx_list[groum.vertx_list.firstIndex(of: con_node)!].node_out=true
            }*/
            
            
        }
        return true
    }
    
    func con_vist(con:ConditionList,vis_con:MyVisitor){
        
                
                for each_con in con{
        switch each_con {
                        case .expression(let expr):
                            //print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ con: ",expr.textDescription)
                           try! vis_con.traverse(expr)
                           // self.groum=merge(groum1: self.groum,groum2: vis_con.groum)
                            break
                        case .availability(let availabilityCondition):
                           // print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ availabilityCondition: ",availabilityCondition.textDescription)
                          break
                        case let .case(pattern, expr):
                           // print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ case: ",pattern.textDescription,"~~~",expr.textDescription)
                          break
                        case let .let(pattern, expr):
                         // print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ let: ",pattern.textDescription,"~~~",expr.textDescription)
                          try! vis_con.traverse(expr)
                          //self.groum=merge(groum1: self.groum,groum2: vis_con.groum)
                            break
                        case let .var(pattern, expr):
                            
                         // print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ var: ",pattern.textDescription,"~~~",expr.textDescription)
                          break
                    }
        }
                    
        // return vis_con
    }
    func merge_g1_g2( g1:Groum,g2:Groum)->Groum{
            var g3 = Groum()
            g3=merge(groum1: g1, groum2: g2)
          /*  print("~~~~~~~~~~~~~~~~~~~~~~test_ merge g1")
            print(g1.print_groum())
            print(g2.print_groum())
            print("~~~~~~~~~~~~~~~~~~~~~~test_ merge g1 g2")
            print(g3.print_groum())
        print("~~~~~~~~~~~~~~~~~~~~~~test_ merge g1 g2")*/
            //g3=merge(groum1: g3, groum2: g2)
            for key in g1.vertx_list{
                //print("~~~~~~~~~~~~~~~~~~~~~~test_merge ",key.me_name ,key.node_out)
                if key.me_name=="SwipePath.regexSVG.matches"{
                    
                 //   print("~~~~~~~~~~~~~~~~~~~~~~test_merge ",key.node_out)
                                                            }
                
                if !key.node_out{
                    for key2 in g2.vertx_list{
                        if key.me_name=="string.index"{
                                  //print("~~~~~~~~~~~~~~~~~~~~~~test_merge ",key.node_out,key2.node_in,key2.me_name)
                                        }
                        if !key2.node_in{
                            if !g3.edges.keys.contains(key){
                                g3.edges[key]=[]
                            }
                            g3.vertx_list[g3.vertx_list.firstIndex(of: key)!].node_out=true
                            g3.edges[key]!.append(key2)
                            g3.vertx_list[g3.vertx_list.firstIndex(of: key2)!].node_in=true

                        }
                    }
                }
                
            }
          
            return g3
        }
    //处理else
    func make_elseclause(elseclau:IfStatement.ElseClause)->Groum{
        var new_groum=Groum()
        switch elseclau {
                        case .else(let codeBlock):
                            
                            new_groum=make_block(stats:codeBlock.statements)
                            //try! vis_con_blo.traverse(codeBlock)
                            //self.groum=merge(groum1: self.groum,groum2: vis_con_blo.groum)
                            break
                        case .elseif(let ifStmt):
                            //try! vis_con_blo.traverse(ifStmt)
                            //self.groum=merge(groum1: self.groum,groum2: vis_con_blo.groum)
                            break
                }
        return new_groum
    }
    //处理codeblock x=>y=>z
    func make_block(stats:Statements)->Groum{
       // var stats=code_blo.statements .statements
        var code_groum=Groum()
       // var code_vis=MyVisitor()
        for st in stats{
            
                    var sta_vis=MyVisitor()
                    try! sta_vis.traverse(st)
            code_groum=merge_g1_g2(g1: code_groum,g2: sta_vis.groum)
          //  print("new_g")
           // print(code_groum.print_groum())
                    
        }
        //self.groum=merge(groum1: self.groum, groum2: code_groum)
        return code_groum
    }
    /*cond->if->if_body+else_body
     1.获得if的条件语句 生成groum   建立groum到if的边
    2. 获得if block 生成groum   if_codbloc
    3. 获得else block 生成groum vis_con_blo.groum
     4. 合并 2，3的groum if_else_groum  建立if 到groum的边
     5. 添加控制节点到self.groum
      */
    func visit(_ ifStmt: IfStatement) throws -> Bool {
    var con_node=ControlNode()
    con_node.ast_node=ifStmt
    con_node.me_name="IF"
    con_node.class_name="IF"
   
    if !self.groum.vertx_list.contains(con_node) {
        if !self.groum.vertx_list.contains(con_node){
                                           self.groum.vertx_list.append(con_node)
                            }
        //print("nnnnnnnnnnode type: ",ifStmt.textDescription)
        let con=ifStmt.conditionList
        var vis_con=MyVisitor()
        con_vist(con: con,vis_con: vis_con)
        var yes_flag=merge_in(groum2: vis_con.groum, node: con_node)
        
        con_node.node_in_list.append(contentsOf: vis_con.groum.vertx_list)
        

        //print("¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥,if")
        //print(self.groum.print_groum())
        /*if yes_flag{
           con_node.node_in=true
        }*/
        let code_blo=ifStmt.codeBlock
        //print("***************************if code_block: ",code_blo.textDescription)
        var if_codbloc=Groum()
        if_codbloc=make_block(stats:code_blo.statements)
        var else_groum=Groum()
        if (ifStmt.elseClause != nil){
            let elseclau:IfStatement.ElseClause=ifStmt.elseClause!
            else_groum=make_elseclause(elseclau: elseclau)
        }
        var if_else_groum=Groum()
        if_else_groum=merge(groum1: if_codbloc, groum2: else_groum)
         var   yes_flag_out=merge_out(node: con_node, groum1: if_else_groum)
         con_node.node_out_list.append(contentsOf:if_else_groum.vertx_list)
        set_con_node(yes_flag_out: yes_flag_out, yes_flag_in: yes_flag, con_node: con_node)
    }
    return true
  }

    //合并groum1 和groum2的节点和边
    func merge( groum1:Groum,groum2:Groum)->Groum{
        var groum3=Groum()
        groum3=copy_groum(groum1: groum1)
        //if groum2的顶点不在里面。if groum的边不在里面 添加到groum3
        for key in groum2.vertx_list{
            if !groum3.vertx_list.contains(key){
                groum3.vertx_list.append(key)
                //groum3.edges[key]=groum2.edges[key]
            }
        }
        for key in groum2.edges.keys{
            if !groum3.edges.keys.contains(key){
                groum3.edges[key]=[]
            }
            for edge in groum2.edges[key]!{
                if !(groum3.edges[key]?.contains(edge))!{
                    groum3.edges[key]?.append(edge)
                }
            }
        }
        return groum3
    }
     func copy_groum(groum1:Groum)->Groum{
        var groum3=Groum()
        for key in groum1.vertx_list{
            groum3.vertx_list.append(key)
        }
                        
                        for key1 in groum1.edges.keys{
                            if !groum3.edges.keys.contains(key1){
                                groum3.edges[key1]=[]
                            }
                            for value in groum1.edges[key1]!{
                                groum3.edges[key1]?.append(value)
                            }
                        }
                return groum3
    }
    //获得groum2的所有没有出边的节点 连线到node 返回新的  node之所以没有事先加入self.groum 是因为functioncall中存在两次调用merge_in
    func merge_in(groum2:Groum,node:Node)->Bool{
        var yes_flag=false
        var vertexs=groum2.vertx_list
        //print(groum2.print_groum())
        //var groum1=Groum()
        //groum1=copy_groum(groum1: self.groum)
        self.groum=merge(groum1: self.groum,groum2: groum2)
       // print("--------------merge--------------------")
        //print(self.groum.print_groum())
        for ver in vertexs{
            if node.me_name=="WHILE"{
              print("test information WHILE: ",ver.node_out,node.node_in,ver==node)
            }
            /*if !groum.vertx_list.contain s(ver){
                groum.vertx_list.append(ver)
            }*/
            if !ver.node_out && !node.node_in && ver != node{
                yes_flag=true
                if !self.groum.edges.keys.contains(ver){
                                        self.groum.edges[ver]=[]
                                    }
                if !((self.groum.edges[ver]?.contains(node))!){
                    
                    self.groum.edges[ver]?.append(node)
                }
                //self.groum.edges.updateValue(node, forKey: ver)
                if self.groum.vertx_list.contains(ver){
                    self.groum.vertx_list[groum.vertx_list.firstIndex(of: ver)!].node_out=true
                }
                else{
                    ver.node_out=true
                    self.groum.vertx_list.append(ver)
                }
                
            }
        }
        return yes_flag
    }
    //获得groum1的所有没有进边的节点 将node 连到groum1
    func merge_out(node:Node,groum1:Groum)->Bool{
            var yes_flag=false
            var vertexs=groum1.vertx_list
        self.groum=merge(groum1: self.groum,groum2: groum1)
            for ver in vertexs{
                /*if !groum.vertx_list.contains(ver){
                                groum.vertx_list.append(ver)
                }*/
                if !ver.node_in && !node.node_out && ver != node{
                    yes_flag=true
                    if !self.groum.edges.keys.contains(node){
                        self.groum.edges[node]=[]
                    }
                    if !((self.groum.edges[node]?.contains(ver))!){
                        self.groum.edges[node]?.append(ver)
                    }
                    
                    //self.groum.edges.updateValue(ver, forKey: node)
                    if self.groum.vertx_list.contains(ver){
                        self.groum.vertx_list[self.groum.vertx_list.firstIndex(of: ver)!].node_in=true
                    }
                    else{
                        ver.node_in=true
                        self.groum.vertx_list.append(ver)
                    }
                    
                }
            }
            return yes_flag
        }
    /*主要处理成员变量
     1. 处理方法调用中包含成员变量的使用：对每个类的成员变量
                            if 不在groum中，添加节点到groum中
     2. 处理了及联调用 SwipePath.regexSVG.matches
     3. 处理了二元表达式
 */
        
    func visit(_ expmem: ExplicitMemberExpression)throws -> Bool {
       // print("ExplicitMemberExpression  lexicalParent",expmem.lexicalParent?.textDescription)
        //print("Come ExplicitMemberExpression: ",expmem.textDescription)
        var acno=ActionNode()
        acno.ast_node=expmem as! ASTNode
        acno.me_name=expmem.textDescription
        if !self.groum.vertx_list.contains(acno){
        switch expmem.kind{
            case let .tuple(postfixExpr, index):
                    //    print("~~~~~~~explict mem tuple: ",   "\(postfixExpr.textDescription)~~~\(index)")
                break
            case let .namedType(postfixExpr, identifier):
                        //print("~~~~~~~explict mem namedType: ", "\(postfixExpr.textDescription)~~~\(identifier)")
                        //self.visit(postfixExpr as! PostfixExpression)
                        //try self.traverse(postfixExpr)
                        var mem_vis=MyVisitor()
                        try mem_vis.traverse(postfixExpr)
                        var yes_flag=merge_in(groum2: mem_vis.groum, node: acno)
                        set_con_node(yes_flag_out: false, yes_flag_in: yes_flag, con_node: acno)
                break
            case let .generic(postfixExpr, identifier, genericArgumentClause):
                  //print("~~~~~~~explict mem generic: ","\(postfixExpr.textDescription).\(identifier)" +
                    //"\(genericArgumentClause.textDescription)")
                break
            case let .argument(postfixExpr, identifier, argumentNames):
                var textDesc = "\(postfixExpr.textDescription).\(identifier)"
                      if !argumentNames.isEmpty {
                        let argumentNamesDesc = argumentNames.map({ "\($0):" }).joined()
                        textDesc += "(\(argumentNamesDesc))"
                      }
             //   print("~~~~~~~explict mem argument: ",textDesc)
            default:
                break
        }
    }
        return true
    }
    
/* 在访问访问调用的时候添加节点 并添加需要的边
     1. 处理的方法调用 对api如果没有访问
        在每访问一个形参时，继续遍历生成新的groum，连接到api
        添加到节点列表
     
 */
    func visit(_ funcalexp: FunctionCallExpression)throws -> Bool {
    
    let args=funcalexp.argumentClause
    let pos_exp=funcalexp.postfixExpression
    
    
    var acno=ActionNode()
    acno.ast_node=funcalexp
    acno.me_name=pos_exp.textDescription
       // acno.me_name=funcalexp.textDescription
    if !self.groum.vertx_list.contains(acno){
        var last_yes_flag=false
        var vis2=MyVisitor()
        //print("+++++++++++++++++come function call",funcalexp.textDescription)
        for each_arg in args!{
            
            switch each_arg{
                case .expression(let expr) :
                    //print( "each para expression---","&\(expr.textDescription)")
                    //try self. visit(expr as! FunctionCallExpression) //visit(expr)
                    //self.visit(ExpressionPattern)
                    
                    try vis2.traverse(expr)
                    //self.groum=merge(groum1: self.groum,groum2: vis2.groum)
                    break
                case let .namedExpression(identifier, expr):
                    //try self.visit(expr asFunctionCallExpression)//try self.visit(expr as Expression)
                    try vis2.traverse(expr)
                   // self.groum=merge(groum1: self.groum,groum2: vis2.groum)
                  //print( "each para namedExpression---","\(identifier)~~~\(expr.textDescription)")
                  break
                case .memoryReference(let expr):
                  //print( "memoryReference---","&\(expr.textDescription)")
                    break
                case let .namedMemoryReference(name, expr):
                  //print("namedMemoryReference---", "\(name)~~~ &\(expr.textDescription)")
                  break
                case .operator(let op):
                  break
                case let .namedOperator(identifier, op):
                  break
            }
            
        }
        var yes_flag1=merge_in(groum2: vis2.groum,node: acno)
                    if yes_flag1{
                        last_yes_flag=true
                    }
        /*处理X.m()
         */
        var vis1=MyVisitor()
        try vis1.traverse(pos_exp)
        var yes_flag=merge_in(groum2: vis1.groum,node: acno)
       if yes_flag{
                        last_yes_flag=true
        }
        //print("<<<<<<<<edge: ")
        //print(self.groum.print_groum())
        
        if !self.groum.vertx_list.contains(acno){
            self.groum.vertx_list.append(acno)
        }
        if last_yes_flag{
            self.groum.vertx_list[groum.vertx_list.firstIndex(of: acno)!].node_in=true
        }
    }
    
        return true
    }

}
