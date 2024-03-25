package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import project.entry.Config;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@FactAnalyzerAnnotations(
        name = "ApacheCXFFactAnalyzer"
)
public class ApacheCXFFactAnalyzer extends JAXRSFactAnalyzer{

    static Map<String, String> addressMap = new HashMap<>();
    static Map<String, String> beanMap = new HashMap<>();
    Map<String, String> mergedMap = new HashMap<>();

    public ApacheCXFFactAnalyzer(){
        super(ApacheCXFFactAnalyzer.class.getName(), "config", "");
    }

//    @Override
//    public void prepare(Object object) {
//        Map<String, Jar> jarMap = this.getProject().getJarMap();
//        SootClass sootClass = (SootClass) object;
//        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
//        if(jarMap.containsKey("cfx-rt") && visibilityAnnotationTag != null){
//            this.setEnable(true);
//        }else{
//            this.setEnable(false);
//        }
//    }

    @Override
    public void prepare(Object object) {

    }

    /**
     * 对Apache CXF配置文件的解析，目前这个不够通用
     * 	<bean id="GisBitmapServices" class="com.dahuatech.emap.mapbiz.bitmap.webservice.response.SyncGisBitmapServiceImpl">
     * 	</bean>
     * 	<jaxws:server id="bitmap" address="/gis/soap/bitmap">
     * 		<jaxws:serviceBean>
     * 			<ref bean="GisBitmapServices"/>
     * 		</jaxws:serviceBean>
     * 	</jaxws:server>
     *
     * 	另外，有的配置在applicationContext.xml中的可能用的是<jaxws:endpoint>标签
     * 		<bean id="itcBulletinServiceImpl" class="com.dahua.dssc.webservice.itcBulletin.ItcBulletinServiceImpl" />
     * 	<jaxws:endpoint id="itcBulletinService" implementor="#itcBulletinServiceImpl"
     * 		address="/itcBulletin" />
     */
    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            addressMap.clear();
            beanMap.clear();
            mergedMap.clear();
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析apache cxf配置文件，配置文件名称不固定，如cxf-servlet.xml等
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());

            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                if (child.getQualifiedName().equals("jaxws:server")) {
                    String address = child.getAttributeValue("address");
                    String refBean = null;
                    // 查找<jaxws:serviceBean>子元素
                    Namespace ns = Namespace.getNamespace("jaxws", "http://cxf.apache.org/jaxws");
                    Element serviceBean = child.getChild("serviceBean", ns);
                    if (serviceBean != null) {
                        Namespace ns1 = Namespace.getNamespace("bean", "http://www.springframework.org/schema/beans");
                        // 获取<ref>子元素
                        Element refElement = serviceBean.getChild("ref", ns1);
                        if (refElement != null) {
                            refBean = refElement.getAttributeValue("bean");
                        }
                    }
                    addressMap.put(refBean, address);
                } else if (child.getQualifiedName().equals("bean")) {
                    String refBean = child.getAttributeValue("id");
                    String className = child.getAttributeValue("class");
                    if (refBean != null & className != null) {
                        beanMap.put(refBean, className);
                    }
                } else if (child.getQualifiedName().equals("jaxws:endpoint")) {
                    String address = child.getAttributeValue("address");
                    // # 符号在Spring配置文件中用于引用Spring容器中的bean，这种配置方式允许在XML配置文件中直接引用Spring创建的Bean
                    String refBean = child.getAttributeValue("implementor");
                    refBean = refBean.replace("#", "");
                    addressMap.put(refBean, address);
                }
            });
            if (addressMap.size() > 0 && beanMap.size() > 0) {
                for (Map.Entry<String, String> entry : addressMap.entrySet()) {
                    String key = entry.getKey();
                    String address = entry.getValue();

                    // 如果 beanMap 中也包含相同的 key，则将两个 value 组成一个新的 value
                    if (beanMap.containsKey(key)) {
                        String className = beanMap.get(key);
                        mergedMap.put(className, address);
                    }
                }
            }
            mergedMap.forEach((key, value) ->{
                Fact fact = new Fact();
                fact.setClassName(key);
                fact.setRoute(value);
                fact.setDescription(String.format("从文件%s中提取出WebService API", config.getFilePath()));
                fact.setCredibility(3);
                /*考虑到这个doget dopost之类的这些方法解析不到，就默认设置了do*，没有把这个字段设置为空*/
                // TODO: 这里不应该设置成do*
                fact.setMethod("—");
                fact.setFactName(getName());
                factChain.add(fact);
            });
        }catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }
}
