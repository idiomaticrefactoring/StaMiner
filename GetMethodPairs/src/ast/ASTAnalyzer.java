package ast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.json.JSONException;

/**
 * Create AST parser with respect to each java file.
 * Get method-level API usage patterns for each software library.
 * Get tokens around each API.
 * <p>
 * AST分析器
 * 为每个JAva文件创建AST解析器
 * 为每个软件库获取方法级别的API使用模式
 * 获取每个API周围的tokens
 */

public class ASTAnalyzer {

    private String[] sources = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/src"};
    private String[] classpath = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_212.jdk/Contents/Home/jre/lib/rt.jar"};


    public List<String> APIClassList = new ArrayList<>();
    public List<String> APINameList = new ArrayList<>();
    public CompilationUnit extractFunNameFormAST(String path) {

        // 读取源码文件内容
        byte[] input = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 建立AST解析器 Java 8版本
        ASTParser astParser = ASTParser.newParser(AST.JLS8); //Java 8
        astParser.setSource(new String(input).toCharArray());
        astParser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        astParser.setCompilerOptions(options);
        astParser.setBindingsRecovery(true);
        astParser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        astParser.setUnitName("any_name");
        CompilationUnit compUnit = (CompilationUnit) (astParser.createAST(null)); // 编译单元
        return compUnit;
    }

    /**
     * 从一个Java文件中提取相关信息
     *
     * @param path 一个Java文件的路径
     */
    public void extractInfoFormAST(String path) {

        // 读取源码文件内容
        byte[] input = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 建立AST解析器 Java 8版本
        ASTParser astParser = ASTParser.newParser(AST.JLS8); //Java 8
        astParser.setSource(new String(input).toCharArray());
        astParser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        astParser.setCompilerOptions(options);
        astParser.setBindingsRecovery(true);
        astParser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        astParser.setUnitName("any_name");
        CompilationUnit compUnit = (CompilationUnit) (astParser.createAST(null)); // 编译单元

        // 建立AST访问器
        ASTNodeVisitor visitor = new ASTNodeVisitor();
        compUnit.accept(visitor);

        // 获取 API 使用序列 （和其在文件中相应的位置）
        APIClassList = visitor.getAPIClassList();
        APINameList = visitor.getAPINameList();
    }

    /**
     * 显示获取的API信息
     */
    public void display() {
        for (int i = 0; i < APIClassList.size(); i++)
            System.out.println(APIClassList.get(i) + ": " + APINameList.get(i));
    }
    //获得一个project下的所有方法名 并保存
    public static HashMap<String,ArrayList<String>> get_pro_funs(String dir_project,String savepath) throws JSONException {
        ArrayList<String> clas_files=new  ArrayList<String>();

        TestFile.getAllFileName(dir_project,clas_files);
        //System.out.println(clas_files);
        TestFile file=new TestFile();
        for (String class_file:clas_files){
            ASTAnalyzer analyzer = new ASTAnalyzer();
            String path=class_file.substring(class_file.lastIndexOf('\\')+1);
            String cla_name=path.substring(0,path.lastIndexOf("."));
            System.out.println("path: "+class_file+"~~~~"+class_file.lastIndexOf('\\'));
            String testFilePath = class_file;
            CompilationUnit compUnit=analyzer.extractFunNameFormAST(testFilePath);
            FuncNameVis visitor = new FuncNameVis();
            compUnit.accept(visitor);
            System.out.println(visitor.funNamelist);
            file.funNames.put(cla_name,visitor.funNamelist);
        }
        file.saveJson(savepath);
       // file.loadJson(savepath);
        //System.out.println(file.funNames);
        return file.funNames;
    }
    public static void main(String[] args) throws IOException, JSONException {
        String dir_root_mappairs="F:\\ziliao\\apidoc\\code migration\\code\\new_code_beifen\\data\\StaMiner_data/project_method_pairs/";//保存对齐的方法对的根路径
        String dir_root="F:\\ziliao\\apidoc\\code migration\\code\\data\\j2sinferer_project\\";//项目的根路径
        String dir_root_save="F:\\ziliao\\apidoc\\code migration\\code\\new_code_beifen\\data\\StaMiner_data/Project_funcs/";//项目的所有方法的根路径
        List<String> java_project_list = Arrays.asList("swipe-android-master");
        List<String> swift_project_list=Arrays.asList("swipe-ios-master");
        for(int i=0;i<java_project_list.size();i++){
            //每个项目的路径
            String j_dir_project=dir_root+java_project_list.get(i)+"/";
            String s_dir_project=dir_root+swift_project_list.get(i)+"/";

            //每个项目的所有类的所有声明的方法路径
            String j_save_path=dir_root_save+java_project_list.get(i)+".json";
            String s_save_path=dir_root_save+swift_project_list.get(i)+".json";



            //获得java swift对应项目的所有方法
            HashMap<String,ArrayList<String>> java_pro =get_pro_funs(j_dir_project,j_save_path);
            TestFile file=new TestFile();
            file.loadJson(s_save_path);
            HashMap<String,ArrayList<String>>  swift_pro=file.funNames;
            //HashMap<String,ArrayList<String>> swift_pro =get_pro_funs(s_dir_project,s_save_path);

            //获得java swift对应项目的所有方法对
            MapPair mp=new MapPair();
            mp.MapMethod(java_pro,swift_pro);
            //保存项目对齐的方法对的路径
            mp.saveJson(dir_root_mappairs+"Map_Method_"+java_project_list.get(i)+".json");
        }
    }
}
