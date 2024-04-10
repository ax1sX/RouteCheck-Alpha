package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import exceptions.FactAnalyzerException;
import entry.Fact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "UnionServletFactAnalyzer"
)
public class UnionServletFactAnalyzer extends AbstractFactAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnionServletFactAnalyzer.class);

    public UnionServletFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public UnionServletFactAnalyzer() {
        super(UnionServletFactAnalyzer.class.getName(), "union", "");
    }

    @Override
    public void prepare(Object object) {

    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        Map<String, Fact> xmlClass = new HashMap<>();
        Map<String, Fact> servletClass = new HashMap<>();

        for (Fact fact : factChain) {
            try{
                String factName = fact.getFactName();
                if (factName != null && Objects.equals(factName.trim(), "factAnalyzer.WebXmlFactAnalyzer")) {
                    xmlClass.put(fact.getClassName(), fact);
                } else if (factName != null && Objects.equals(factName.trim(), "factAnalyzer.HttpServletFactAnalyzer")) {
                    servletClass.put(fact.getClassName(), fact);
                }
            }catch (Exception e){
                LOGGER.info(e.getMessage());
            }
        }

        if (!xmlClass.isEmpty()){
            if (!servletClass.isEmpty()){
                Iterator<Map.Entry<String, Fact>> iterator = servletClass.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Fact> entry = iterator.next();
                    String key = entry.getKey();
                    if (xmlClass.containsKey(key)) {
                        factChain.remove(entry.getValue());
                    }
                }

            }
        }
    }
}
