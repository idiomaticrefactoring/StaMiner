package ast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiSequence {
    List<String> className = new ArrayList<>();
    List<String> objectName = new ArrayList<>();
    List<String> fatherMethod = new ArrayList<>();
    public ApiSequence(){}
    public void add(String className,String objectName,String fatherMethod){
        if(!(className.contains("java.")))
            return;
        this.className.add(className);
        this.objectName.add(objectName);
        this.fatherMethod.add(fatherMethod);
    }
    public void addAll(ApiSequence apiSequence)
    {
        this.className.addAll(apiSequence.className);
        this.objectName.addAll(apiSequence.objectName);
        this.fatherMethod.addAll(apiSequence.fatherMethod);
    }
    public void display()
    {
        for(int i = 0;i < className.size();i++)
        {
            System.out.println(className.get(i) + ',' + objectName.get(i)+'@' + fatherMethod.get(i));
        }
    }
    public void toJson(String className)
    {
        JSONObject classObject = new JSONObject();
        Set<String> checked = new HashSet<>();
        for(int i = 0;i < this.fatherMethod.size();i++)
        {
            if(!checked.contains(this.fatherMethod.get(i)))
            {
                String current = this.fatherMethod.get(i);
                JSONArray methodArray = new JSONArray();
                for(int j = i;j < this.fatherMethod.size();j++)
                {
                    if(this.fatherMethod.get(j).equals(current))
                    {
                        methodArray.add(this.className.get(j) + '.' + this.objectName.get(j));
                    }
                }
                classObject.put(current,methodArray);
                checked.add(current);
            }
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put(className,classObject);
        System.out.println(finalObject);
    }
}
