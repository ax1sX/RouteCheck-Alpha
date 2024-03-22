package factAnalyzer;

import entry.Fact;
import exceptions.FactAnalyzerException;

import java.util.Collection;

public class SpringFactAnalyzer extends AbstractFactAnalyzer{

    public SpringFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public SpringFactAnalyzer() {
        super(SpringFactAnalyzer.class.getName(), "", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {

    }

    @Override
    public void prepare(Object object) {

    }
}
