该项目的整体设计请查看：[路由分析引擎总体设计](https://github.com/suizhibo/RouteCheck/blob/master/%E8%B7%AF%E7%94%B1%E5%88%86%E6%9E%90%E5%BC%95%E6%93%8E%E6%80%BB%E4%BD%93%E8%AE%BE%E8%AE%A1.pptx
)
# 如何新增一个FactAnalyzer？

1. 实现一个类，名称形式如下XXFactAnalyzer，该类需继承AbstractFactAnalyzer，并添加注解FactAnalyzerAnnotations；

2. 为上述类无参构造函数 ；
3. 可以添加static 属性保存该FactAnalyzer分析的相关结果；

4. 实现public void prepare(Object object) {}、public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {}、public String getName() {}、public String getType() {}、public String getFactDescription() {}以及public String toString() {}方法。

5. prepare方法做一些前置条件的筛选，并根据筛选结果判断是否启用该FactAnalyzer（所有新增FactAnalyzer默认启用）；

6. analysis方法完成具体的事实分析。

```java
package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@FactAnalyzerAnnotations(
        name = "WebXmlFactAnalyzer"
)

/*
https://docs.oracle.com/cd/E13222_01/wls/docs81/webapp/web_xml.html
*/
public class WebXmlFactAnalyzer extends AbstractFactAnalyzer {

    static Map<String, Element> servlets = new HashMap<>();
    static Map<String, Set<Element>> servletMappings = new HashMap<>();

    public WebXmlFactAnalyzer() {
        super(WebXmlFactAnalyzer.class.getName(), "config", "");
    }

    @Override
    public void prepare(Object object) {
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            try{
                // TODO: 判断是否包含<web-app>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("web-app")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child ->{
                if(child.getName().equals("servlet")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    servlets.put(servletName, child);
                }else if(child.getName().equals("servlet-mapping")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    Set<Element> values = servletMappings.getOrDefault(servletName, new HashSet<Element>());
                    values.add(child);
                    servletMappings.put(servletName, values);
                }
            });
            if (servlets.size() > 0 && servletMappings.size() > 0) {
                servlets.forEach((name, servlet) -> {
                    Set<Element> servletMapping = servletMappings.getOrDefault(name, new HashSet<>());
                    servletMapping.forEach(sm ->{
                        Fact fact = new Fact();
                        String servletClass = servlet.getChildText("servlet-class", servlet.getNamespace());
                        fact.setClassNameMD5(Utils.getMD5Str(servletClass));
                        fact.setClassName(servletClass);
                        fact.setRoute(sm.getChildText("url-pattern", sm.getNamespace()));
                        fact.setDescription(String.format("从文件%s中提取出servlet和servlet-mapping", config.getFilePath()));
                        fact.setCredibility(3);
                        fact.setMethod("do*");
                        fact.setFactName(getName());
                        factChain.add(fact);
                    });
                });
            }
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }


    }

    public static void main(String[] args) throws Exception {
        // TODO: 解析web.xml
        SAXBuilder saxBuilder = new SAXBuilder(false);
        InputStream is = new FileInputStream(new File("C:\\Users\\ss\\Desktop\\test\\web.xml"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
    }
}


```

# Usage

```text
.______        ______    __    __  .___________. _______   ______  __    __   _______   ______  __  ___ 
|   _  \      /  __  \  |  |  |  | |           ||   ____| /      ||  |  |  | |   ____| /      ||  |/  / 
|  |_)  |    |  |  |  | |  |  |  | `---|  |----`|  |__   |  ,----'|  |__|  | |  |__   |  ,----'|  '  /  
|      /     |  |  |  | |  |  |  |     |  |     |   __|  |  |     |   __   | |   __|  |  |     |    <   
|  |\  \----.|  `--'  | |  `--'  |     |  |     |  |____ |  `----.|  |  |  | |  |____ |  `----.|  .  \  
| _| `._____| \______/   \______/      |__|     |_______| \______||__|  |__| |_______| \______||__|\__\ 
                                                                                                        
usage: java -jar RouteCheck.jar [-cp <arg>] [-h] [-lp <arg>] [-o <arg>] [-pn
       <arg>] [-pp <arg>] [-sp <arg>]
 -cp,--class-path <arg>     类文件地址
 -h,--help                  打印命令行帮助信息
 -lp,--lib-path <arg>       库文件地址
 -o,--outPut <arg>          结果保存目录
 -pn,--project-name <arg>   项目名称
 -pp,--project-path <arg>   项目路径
 -sp,--setting-path <arg>   设置文件地址
```