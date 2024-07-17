package factAnalyzer;


import entry.StrutsAction;
import exceptions.FactAnalyzerException;
import entry.Settings;
import project.entry.Module;
import entry.Fact;

import java.util.Collection;

public interface FactAnalyzer {
    void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException;
    void analysis(Object object, Collection<Fact> factChain, Collection<StrutsAction> actionChain) throws FactAnalyzerException;

    void resetModule(Module module);

    String getName();
    String getType();
    String getFactDescription();
    void initialize(Module module, Settings settings);
    void prepare(Object object);
    boolean isEnable();
}
