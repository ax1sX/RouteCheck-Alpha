package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@FactAnalyzerAnnotations(
        name = "SpringBeanFactAnalyzer"
)
public class SpringBeanFactAnalyzer extends SpringFactAnalyzer{

    public SpringBeanFactAnalyzer(){
        super(SpringBeanFactAnalyzer.class.getName(), "config", "");
    }

    public void analysis(String configPath, Collection<Fact> factChain) {
        /*
         * bean标签 https://blog.csdn.net/ZixiangLi/article/details/87937819
         * */
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(configPath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                try {
                    if (child.getName().equals("bean")) {
                        Fact fact = new Fact();
                        String clazz = child.getAttributeValue("class");
                        String name = child.getAttributeValue("name");
                        // 基于Spring实现的RMI, HttpInvoker，Hessian, JAX-RPC，从客户端提取访问url
                        if (clazz.equals("org.springframework.remoting.rmi.RmiProxyFactoryBean") ||
                                clazz.equals("org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean") ||
                                clazz.equals("org.springframework.remoting.caucho.HessianProxyFactoryBean")
                        ) {
                            List<Element> properties = child.getChildren();
                            properties.forEach(property -> {
                                if (property.getName().equals("property")) {
                                    if (property.getAttributeValue("name").equals("serviceUrl")) {
                                        String route = property.getChildText("value", property.getNamespace());
                                        fact.setDescription(String.format("从文件%s中提取出RMI的serviceUrl属性", configPath));
                                        fact.setRoute(route);
                                        factChain.add(fact);
                                    }
                                }
                            });
                            return;
                        } else if (clazz.equals("org.springframework.remoting.jaxrpc.JaxRpcPortProxyFactoryBean")) {
                            List<Element> properties = child.getChildren();
                            properties.forEach(property -> {
                                if (property.getName().equals("property")) {
                                    if (property.getAttributeValue("name").equals("wsdlDocumentUrl")) {
                                        String route = property.getChildText("value", property.getNamespace());
                                        fact.setDescription(String.format("从文件%s中提取出JAX-RPC的wsdlDocumentUrl属性", configPath));
                                        fact.setRoute(route);
                                        factChain.add(fact);
                                    }
                                }
                            });
                        }

                        if (name != null && name.contains("/")){ // 这个逻辑的案例—蓝凌EKP
                            fact.setRoute(name);
                            fact.setMethod("—");
                            fact.setDescription(String.format("从文件%s中提取出Bean的name属性", configPath));
                            fact.setClassName(clazz);
                            fact.setFactName(getName());
                            factChain.add(fact);
                        }
                    }
                } catch (Exception e) {

                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
           Config config = (Config) object;
           String configPath = config.getFilePath();
           analysis(configPath, factChain);
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public void prepare(Object object) {
        // TODO:判断xml文件中是否包含：beans
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析.xml
            try{
                // TODO: 判断是否包含<beans>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("beans")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }

    }

    public static void main(String[] args) throws Exception {
        }
}
