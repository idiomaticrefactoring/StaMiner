# -*- coding: utf-8 -*-
import sys,os
sys.path.append("../../")
class_name_list=["NSRegularExpression","String","NSString","ContiguousArray","Array","NSArray","ArraySlice","Set","NSSet",
"NSOrderedSet","Dictionary","NSMapTable","Calendar","NSCalendar","NSTimeZone","NSDateComponents","TextOutputStream",
"OutputStream","Character","Error","NSException","NSError","Int","Int64","UInt","UInt32","NSNumber","Double","Int32","Thread",
"Data","NSData","Stream","InputStream"]
parentdir = os.path.dirname(os.path.dirname(os.path.abspath("F:/ziliao/apidoc/code migration/code/utils.py")))
sys.path.insert(0,parentdir)
from utils import get_all_class_path,dump_json,load_json
dir_path="F:/ziliao/apidoc/code migration/code/new_code_beifen/data/StaMiner_data/swift-groum-json/groum-json/"
path_list=get_all_class_path(dir_path)
print("path_lsit: ",path_list)

for json_file in path_list:
    count=0
    pro_groum=load_json(json_file)
    for cla in pro_groum.keys():
        for me in   pro_groum[cla].keys():
            vertexs=pro_groum[cla][me]["vertxs"]
            for ver in vertexs:
                cla_name=ver[1]
                if cla_name in class_name_list:
#                    print("one vertex: ",json_file,cla,me,cla_name)
                    count=count+1
    print("count: ",json_file,count)
                
            
