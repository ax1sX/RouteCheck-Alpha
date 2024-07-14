package utils;

import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

public class Utils {

    public static Command command = null;
    static final public int MAGIC = 0xCAFEBABE;

    public static boolean mkDir(String path){
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                return file.mkdirs();
            }
            else{
                return false;
            }
        } catch (Exception e) {
        } finally {
            file = null;
        }
        return false;
    }

    public static void fileWriter(String filepath, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(filepath, "UTF-8")) {
            writer.println(content);
        }
    }

    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest  = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }

    public static String fileReader(String filePath){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            String content = stringBuilder.toString();
            return content;
        }catch (Exception e){

        }
       return "";
    }

    public static Map<?, ?> objectToMap(Object obj) {
        if (obj == null)
            return null;
        return new org.apache.commons.beanutils.BeanMap(obj);
    }

    // 用于获取给定类所在的 JAR 文件路径
    public static String getJarFileByClass(Class cs){
        String fileString = null;
        if (cs != null) {
            String tempString = cs.getProtectionDomain().getCodeSource().getLocation().getFile();
            if(tempString.endsWith(".jar")){
                try{
                    fileString = URLDecoder.decode(tempString, "utf-8");
                }catch (UnsupportedEncodingException exception){
                    fileString = URLDecoder.decode(tempString);
                }
            }
        }
        return fileString;
    }



    public static String getFullyQualifiedName(String classFilePath) {
        try {
            ClassParser parser = new ClassParser(classFilePath);
            JavaClass javaClass = parser.parse();
            String className = javaClass.getClassName();
            return className;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<String> getClassPath(List<String> classFilePaths) {
        HashSet<String> classDir = new HashSet<>();
        for (String classFilePath:
             classFilePaths) {
            String className = getFullyQualifiedName(classFilePath);
            String classNameTemp = className.replace(".", File.separator);
            classDir.add(classFilePath.substring(0, classFilePath.lastIndexOf(classNameTemp) - 1));
        }
        return new ArrayList<String>(classDir);
    }
}
