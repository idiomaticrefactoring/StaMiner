package ast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodAnnotation {
    List<String> name = new ArrayList<>();
    List<String> annotation = new ArrayList<>();
    public void add(String name,String annotation)
    {
        if(name == null || name.equals("null"))
            return;
        if(annotation == null || annotation.equals(null))
            return;
        this.name.add(name);
        this.annotation.add(annotation);
    }
    public void addAll(MethodAnnotation methodAnnotation)
    {
        this.name.addAll(methodAnnotation.name);
        this.annotation.addAll(methodAnnotation.annotation);
    }
    public void display(String fileName)
    {
        System.out.println("*************");
        System.out.println(fileName + ".java");
        for(int i = 0;i < name.size();i++)
        {
            System.out.println(name.get(i)+":"+annotation.get(i));
        }
        System.out.println("*************");
    }
    public JSONObject toJson(String className)
    {
        JSONObject classObject = new JSONObject();
        Set<String> checked = new HashSet<>();
        for(int i = 0;i < this.name.size();i++)
        {
            String modifiedAnnotation = new String(annotation.get(i));
            modifiedAnnotation = modifiedAnnotation.replace("*","");
            modifiedAnnotation = modifiedAnnotation.replace("\n","");
            modifiedAnnotation = modifiedAnnotation.replace("/","");
            modifiedAnnotation = modifiedAnnotation.replaceAll("\\{.*?\\}","");
            //System.out.println(modifiedAnnotation);
            modifiedAnnotation = modifiedAnnotation.replaceAll("@[a-zA-Z]+","");
            //System.out.println(modifiedAnnotation);
            modifiedAnnotation = modifiedAnnotation.replace("<p>","");
            //System.out.println(modifiedAnnotation);
            int dotPosition = modifiedAnnotation.indexOf(".");
            if(dotPosition >= 0 && dotPosition < modifiedAnnotation.length())
                modifiedAnnotation = modifiedAnnotation.substring(0,dotPosition);
            int startPosition = 0;
            for(int j = 0;j < modifiedAnnotation.length();j++)
            {
                if(modifiedAnnotation.charAt(j) != ' ')
                {
                    startPosition = j;
                    break;
                }
            }
            modifiedAnnotation = modifiedAnnotation.substring(startPosition,modifiedAnnotation.length());
            classObject.put(name.get(i),modifiedAnnotation);
        }
        JSONObject finalObject = new JSONObject();
        finalObject.put(className,classObject);
        return finalObject;
    }
}
