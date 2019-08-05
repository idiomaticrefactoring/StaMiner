package ast;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.util.*;

import static java.util.Map.*;

public class TestFile {
    public HashMap<String,ArrayList<String>> funNames = new HashMap<String,ArrayList<String>>();//存储一个project的所有类的所有方法名 key 是类名   value列表，每个元素是一个方法名
    public void saveJson(String path) throws JSONException {
        JSONObject json = new JSONObject();

        for (Entry entry : this.funNames.entrySet()) {
            json.put((String) entry.getKey(),entry.getValue());
        }

        String jsonStr = json.toString(); //将JSON对象转化为字符串
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(path))));
            writer.write(jsonStr);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void loadJson(String path) throws JSONException, IOException {
        String content = FileUtils.readFileToString(new File(path),"utf-8");
        JSONObject json = new JSONObject(content);
        for (Iterator it = json.keys(); it.hasNext(); ) {
            String key = (String) it.next();
            JSONArray jarr= (JSONArray) json.get(key);
            ArrayList<String> me_list=new ArrayList<String>();
            //System.out.println("key 对应的value： "+.getClass());
            for(int i=0;i<jarr.length();i++){
                me_list.add((String) jarr.get(i));
            }
            //System.out.println("key 对应的value： "+me_list.toString());
            this.funNames.put(key, me_list);
        }
    }
    /*
   将指定下的路径下的所有的java或者swift代码的文件对象添加到listFileName
    */
    public static void getAllFileName(String path,ArrayList<String> listFileName){
        File file = new File(path);
        File [] files = file.listFiles();

        for(File a:files){
            if(a.isDirectory()){//如果文件夹下有子文件夹，获取子文件夹下的所有文件全路径。
                getAllFileName(a.getAbsolutePath()+"\\",listFileName);
            }
            else{
                String name =  a.getPath();

                //System.out.println(name);
                String suffix = name.substring(name.lastIndexOf(".") + 1);
                if (suffix.equals("java") ) {
                    listFileName.add(name);
                    //System.out.println(name);
                }
            }
        }
    }
}
