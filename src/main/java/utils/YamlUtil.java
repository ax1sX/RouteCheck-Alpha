package utils;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class YamlUtil {
    private YamlUtil() {}

    /**
     * 将 Java 对象写到 yaml 文件
     *
     * @param object   对象
     * @param yamlPath 文件路径
     */
    public static void writeYaml(Object object, String yamlPath) {
        try {
            YamlWriter writer = new YamlWriter(new FileWriter(yamlPath));
            writer.write(object);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 yaml 文件读取转到 Java 对象
     * @param yamlPath 文件路径
     * @param clazz    目标类.class
     * @param <T>      目标类
     * @return 目标类对象
     */
    public static <T> T readYaml(String yamlPath, Class<T> clazz) {
        try {
            YamlReader reader = new YamlReader(new FileReader(yamlPath));
            try {
                return reader.read(clazz);
            } catch (YamlException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
