package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;

import java.util.Collection;

public class SOAPFactAnalyzer extends AbstractFactAnalyzer {

    public SOAPFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public SOAPFactAnalyzer() {
        super(SOAPFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {

    }

    @Override
    public void prepare(Object object) {

    }
}
