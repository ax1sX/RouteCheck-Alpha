package factAnalyzer;


import entry.StrutsAction;
import exceptions.FactAnalyzerException;
import entry.Settings;
import project.entry.Project;
import entry.Fact;

import java.util.Collection;

public interface FactAnalyzer {
    void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException;
    void analysis(Object object, Collection<Fact> factChain, Collection<StrutsAction> actionChain) throws FactAnalyzerException;
    String getName();
    String getType();
    String getFactDescription();
    void initialize(Project project, Settings settings);
    void prepare(Object object);
    boolean isEnable();
}
