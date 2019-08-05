//
//  Groum.swift
//  helloworld
//
//  Created by sally on 2019/7/31.
//  Copyright © 2019 sally. All rights reserved.
//

import Foundation
class Groum : NSObject {
    public  override init(){
    }
    var groum_name: String = ""
    var vertx_list = [Node]()
    var edges=[Node:[Node]]()
    func print_vert_vars(){
        for ver in self.vertx_list{
                   print("$$$$$$$$$$$$$$$$print vertex list: ",ver.me_name, ver.var_list)
               }
    }
    func mod_groum_node_id(i:Int){
        for ver in self.vertx_list{
                   self.vertx_list[self.vertx_list.firstIndex(of: ver)!].id=i
        }
    }
    // 输出程序内部的存储名称
    func print_groum(){
        for ver in self.vertx_list{
            print("%%%%%%%print vertex: ",ver.me_name,ver.node_out)
        }
        //print("%%%%%%%print edge: ")
        for (key,value) in self.edges{
            for each_value in value{
                var new_key=self.vertx_list[self.vertx_list.firstIndex(of: key)!]
                               var old_value=self.vertx_list[self.vertx_list.firstIndex(of: each_value)!]
                print("%%%%%%%print edge: ", key.me_name,each_value.me_name)

            }
        }
    }
    //输出外部展示的节点名称
    func print_groum_new(){
        for ver in self.vertx_list{
                    print("%%%%%%%print vertex: ",ver.get_node_name())
        }
                //print("%%%%%%%print edge: ")
        for (key,value) in self.edges{
            for each_value in value{
                var new_key=self.vertx_list[self.vertx_list.firstIndex(of: key)!]
                var old_value=self.vertx_list[self.vertx_list.firstIndex(of: each_value)!]
                print("%%%%%%%print edge: ", key.get_node_name(),each_value.get_node_name(),"~~~",key.me_name,each_value.me_name)

            }
        }
    }
}
