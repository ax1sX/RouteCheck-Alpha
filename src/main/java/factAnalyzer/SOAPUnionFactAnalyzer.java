package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Element;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "SOAPUnionFactAnalyzer"
)
public class SOAPUnionFactAnalyzer extends AbstractFactAnalyzer {

    public SOAPUnionFactAnalyzer(){
        super(SOAPUnionFactAnalyzer.class.getName(), "union", "");
    }
    @Override
    public void prepare(Object object) {
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            List<String> urls = WebXmlFactAnalyzer.AxisUrls;
            Map<String, Element> services = WSDDFactAnalyzer.services;
            if (!urls.isEmpty()){
                for (String u : urls) {
                    if (u.endsWith("*")) {
                        services.forEach((key, service) -> {
                            try {
                                Fact fact = new Fact();
                                String serviceName = service.getAttributeValue("name");
                                String route = u.replace("*", serviceName);
                                fact.setRoute(route);
                                List<Element> params = service.getChildren();
                                params.forEach(param -> {
                                    if (param.getName().equals("parameter")) {
                                        String paramName = param.getAttributeValue("name");
                                        if (paramName.equals("className")) {
                                            String className = param.getAttributeValue("value");
                                            fact.setClassName(className);
                                            fact.setCredibility(3);
                                            fact.setFactName(getName());
                                            fact.setDescription(String.format("web.xml中发现AxisServlet, 路由:%s，WSDD中发现服务名:%s, 对应类:%s",
                                                    u, serviceName, fact.getClassName()));
                                            factChain.add(fact);
                                        } else if (paramName.equals("allowedMethods")) {
                                            String allowedMethods = param.getAttributeValue("value");
                                            if (allowedMethods.equals("*")) {
                                                allowedMethods = "*";
                                            }
                                            fact.setMethod(allowedMethods);
                                        }
                                    }
                                });
                            } catch (Exception ex) {
                            }

                        });
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new FactAnalyzerException(e.getMessage());
        }
    }
}
