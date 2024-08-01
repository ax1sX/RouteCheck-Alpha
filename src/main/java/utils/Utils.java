package utils;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static final ClassLoader FACTANALYZER_CLASSLOADER = new CoreClassLoader(Utils.class.getClassLoader());

    public static Command command = null;
    static final public int MAGIC = 0xCAFEBABE;
    public static final List<String> CONFIG_SUFFIXES = Arrays.asList("xml", "yaml", "wsdl", "wsdd");
    public static final List<String> CONFIG_BLACK_LIST = Arrays.asList("pom.xml");

    public static boolean mkDir(String path) {
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                return file.mkdirs();
            } else {
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
            digest = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }

    public static String fileReader(String filePath) {
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
        } catch (Exception e) {

        }
        return "";
    }

    public static Map<?, ?> objectToMap(Object obj) {
        if (obj == null)
            return null;
        return new org.apache.commons.beanutils.BeanMap(obj);
    }

    // 用于获取给定类所在的 JAR 文件路径
    public static String getJarFileByClass(Class cs) {
        String fileString = null;
        if (cs != null) {
            String tempString = cs.getProtectionDomain().getCodeSource().getLocation().getFile();
            if (tempString.endsWith(".jar")) {
                try {
                    fileString = URLDecoder.decode(tempString, "utf-8");
                } catch (UnsupportedEncodingException exception) {
                    fileString = URLDecoder.decode(tempString);
                }
            }
        }
        return fileString;
    }


    public static String getFullyQualifiedName(String classFilePath) {
        try {
            ClassParser parser = null;
            if (classFilePath.contains("jar!")) {
                String jarName = classFilePath.split("jar\\!")[0] + "jar";
                String className = classFilePath.split("jar\\!")[1];
                parser = new ClassParser(jarName, className);
            } else {
                parser = new ClassParser(classFilePath);
            }
            JavaClass javaClass = parser.parse();
            String className = javaClass.getClassName();
            return className;
        } catch (Exception e) {
            LOGGER.warn("获取 " + classFilePath + "的FullyQualifiedName出现错误： " + e.getMessage());
        }
        return "";
    }

    public static List<String> getClassDirs(List<String> classFilePaths) {
        HashSet<String> classDir = new HashSet<>();
        for (String classFilePath :
                classFilePaths) {
            classDir.add(getClassDir(classFilePath));
        }
        return new ArrayList<String>(classDir);
    }

    public static String getClassDir(String classFilePath) {
        String classDir = "";
        try {
            String className = getFullyQualifiedName(classFilePath);
            String classNameTemp = className.replace(".", File.separator);
            classDir = classFilePath.substring(0, classFilePath.lastIndexOf(classNameTemp) - 1);
        } catch (Exception ex) {

        }
        return classDir;
    }

    public static void scanClass(URI uri, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            String jarFileString;
            // 项目生成的RouteCheck.jar文件，找到factAnalyzer文件夹路径，扫描FactAnalyzer所有的子类，生成子类列表destList
            if ((jarFileString = Utils.getJarFileByClass(Utils.class)) != null) {
                scanClassByJar(new File(jarFileString), packageName, parentClass, annotationClass, destList);
            } else {
                File file = new File(uri);
                File[] file2 = file.listFiles();
                for (int i = 0; i < file2.length; i++) {
                    File objectClassFile = file2[i];
                    if (objectClassFile.getPath().endsWith(".class"))
                        try {
                            String objectClassName = String.format("%s.%s", new Object[]{packageName, objectClassFile.getName().substring(0, objectClassFile.getName().length() - ".class".length())});
                            Class<?> objectClass = Class.forName(objectClassName, true, FACTANALYZER_CLASSLOADER);
                            if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent((Class) annotationClass)) {
                                destList.add(objectClass);
                            }
                        } catch (Exception e) {
                            LOGGER.debug(String.format("When scan class %s occur error: %s", new Object[]{objectClassFile, e.getMessage()}));
                        }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void scanClassByJar(File srcJarFile, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            JarFile jarFile = new JarFile(srcJarFile);
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            packageName = packageName.replace(".", "/");
            while (jarFiles.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarFiles.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(packageName) && name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    name = name.substring(0, name.length() - 6);
                    Class objectClass = Class.forName(name, true, FACTANALYZER_CLASSLOADER);
                    try {
                        if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent(annotationClass)) {
                            destList.add(objectClass);
                        }
                    } catch (Exception e) {
                        LOGGER.debug(String.format("When scan class %s occur error: %s", new Object[]{objectClass, e.getMessage()}));
                    }
                }
            }
            jarFile.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static void scanClassAndConfigByJarPath(String srcJarFilePath, String packageName, ArrayList<String> destClass, ArrayList<Config> destConfig) {
        Utils.scanClassAndConfigByJar(new File(srcJarFilePath), packageName, destClass, destConfig);
    }

    public static void scanClassAndConfigByJar(File srcJarFile, String packageName, ArrayList<String> destClass, ArrayList<Config> destConfig) {
        try {
            JarFile jarFile = new JarFile(srcJarFile);
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            packageName = packageName.replace(".", "/");
            while (jarFiles.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarFiles.nextElement();
                String name = jarEntry.getName();
                String suffix = name.substring(name.lastIndexOf(".") + 1);
                if (name.startsWith(packageName) && name.endsWith(".class")) {
                    try {
                        destClass.add(srcJarFile.getAbsolutePath() + "!" + name);
                    } catch (Exception e) {
                        LOGGER.debug(String.format("When scan class %s occur error: %s", new Object[]{name, e.getMessage()}));
                    }
                } else if (CONFIG_SUFFIXES.contains(suffix)) {
                    try {
                        String[] names = name.split("/");
                        String fileName = names[names.length -1];
                        if(!CONFIG_BLACK_LIST.contains(fileName)){
                            destConfig.add(new Config(name, srcJarFile.getAbsolutePath() + "!" + name, suffix, true));

                        }
                    } catch (Exception e) {
                        LOGGER.debug(String.format("When scan config %s occur error: %s", new Object[]{name, e.getMessage()}));
                    }
                }
            }
            jarFile.close();
        } catch (Exception ex) {
            LOGGER.debug(String.format("When scan jar %s occur error: %s", new Object[]{srcJarFile.getAbsoluteFile().getName(), ex.getMessage()}));

        }
    }

    public static InputStream getInputStreamByConfig(Config config) {
        InputStream is = null;
        try {
            if (config.is_jar()) {
                String zipFile = config.getFilePath().split("!")[0];
                String fileName = config.getFilePath().split("!")[1];
                ZipFile zip = null;

                zip = new ZipFile(zipFile);
                ZipEntry entry = zip.getEntry(fileName);
                is = new DataInputStream(new BufferedInputStream(zip.getInputStream(entry), 8192));

            } else {
                is = new FileInputStream(new File(config.getFilePath()));
            }
        } catch (IOException e) {
            LOGGER.debug("");
        }

        return is;

    }

    public static String getModulePath(String classDir) {
        return classDir.replace(File.separator + "WEB-INF" + File.separator + "classes", "")
                .replace(File.separator + "target" + File.separator + "classes", "")
                .replace(File.separator + "classes", "");
    }
}
