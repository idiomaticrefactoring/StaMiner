package ast;

import org.json.JSONException;
import org.json.JSONObject;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
//import org.simmetrics.StringMetrics;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 对一个project对，找到所有的方法对
 * 1.找到类对
 * 2.基于类对找方法对
 */
public class MapPair {
    public HashMap<String,ArrayList<ArrayList<String>>> mapPairs = new HashMap<String,ArrayList<ArrayList<String>>>();//第一个key是类名，value是双重list 第一个元素是swift的类名 之后的元素依次是 java的方法名 和swift的方法名
    public void saveJson(String path) throws JSONException {
        JSONObject json = new JSONObject();

        for (Map.Entry entry : this.mapPairs.entrySet()) {
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
    public void MapMethod(HashMap<String,ArrayList<String>> java_pro,HashMap<String,ArrayList<String>> swift_pro) throws JSONException, IOException {
        ArrayList<ArrayList<String>> all_class_pair=new  ArrayList<ArrayList<String>>();//存放所有的类对  一个元素 是 java和swift对应的类对
        /*
        找类对
         */
        StringMetric metric = StringMetrics.cosineSimilarity();
        for( String java_cla:java_pro.keySet()){
            //对任意一个java类找Swift对应类
            float max_score=-1;
            String j_s="";
            for ( String swift_cla:swift_pro.keySet()){

                float score = metric.compare(java_cla, swift_cla);
                if (score > max_score && score > 0.5) {
                    max_score = score;
                    j_s=swift_cla;
                }
            }
            //添加对应的类对
            mapPairs.put(java_cla,new  ArrayList<ArrayList<String>>());

            if (j_s!=""){
                ArrayList<String> class_pair=new ArrayList<String>();
                class_pair.add(java_cla);
                class_pair.add(j_s);
                all_class_pair.add(class_pair);

                ArrayList<String> class_s=new ArrayList<String>();
                class_s.add(j_s);
                mapPairs.get(java_cla).add(class_s);
            }
        }
     /*
     对每一个类对 收集方法对
      */

        for ( ArrayList<String> cla_pair:all_class_pair){
            ArrayList<ArrayList<String>> all_me_pair=new  ArrayList<ArrayList<String>>();
            for(String j_methd:java_pro.get(cla_pair.get(0))){
                float max_score=-1;
                String j_s="";
                System.out.println("class_pair: "+cla_pair);

                for (String s_methd:swift_pro.get(cla_pair.get(1))){

                    float score = metric.compare(j_methd, s_methd);
                    if (score > max_score && score > 0.5) {
                        max_score = score;
                        j_s=s_methd;
                    }
                }
                if (j_s!=""){
                    ArrayList<String> me_pair=new ArrayList<String>();
                    me_pair.add(j_methd);
                    me_pair.add(j_s);
                    all_me_pair.add(me_pair);
                }
            }
            //添加所有对应的方法对
            mapPairs.get(cla_pair.get(0)).addAll(all_me_pair);
        }

    }
}
