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
import java.util.Collection;
import java.util.List;

@FactAnalyzerAnnotations(
        name = "Struts2ConfigXmlFactAnalyzer"
)
public class StrutsConfigXmlFactAnalyzer extends AbstractFactAnalyzer {

    public StrutsConfigXmlFactAnalyzer() {
        super(StrutsConfigXmlFactAnalyzer.class.getName(), "config", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析struts-config.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver()); // 隐蔽dtd验证
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                String childName = child.getName();
                if (childName.equals("form-beans")) {
                    List<Element> formBeans = child.getChildren();
                    formBeans.forEach(formBean -> {
                        try {
                            Fact fact = new Fact();
                            String formBeanName = formBean.getAttributeValue("name");
                            String formBeanType = formBean.getAttributeValue("type");
                            fact.setRoute(formBeanName);
                            fact.setClassName(formBeanType);
                            fact.setCredibility(3);
                            fact.setDescription(String.format("从文件%s中发现%s", config.getFilePath(), formBean.toString()));
                            fact.setFactName(getName());
                            factChain.add(fact);
                        } catch (Exception ex) {

                        }
                    });
                } else if (childName.equals("action-mappings")) {
                    List<Element> actions = child.getChildren();
                    actions.forEach(action -> {
                        try {
                            Fact fact = new Fact();
                            String path = action.getAttributeValue("path");
                            String actionType = action.getAttributeValue("type");
                            fact.setRoute(path);
                            fact.setClassName(actionType);
                            fact.setCredibility(3);
                            fact.setDescription(String.format("从文件%s中发现%s", config.getFilePath(), action.toString()));
                            fact.setFactName(getName());
                            factChain.add(fact);
                        } catch (Exception ex) {

                        }
                    });
                }
            });
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public void prepare(Object object) {
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析struts-config.xml
            try{
                // TODO: 判断是否包含<struts-config>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("struts-config")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }
    }
}
