package factAnalyzer;

import entry.Fact;
import entry.Settings;
import entry.StrutsAction;
import exceptions.FactAnalyzerException;
import project.entry.Project;

import java.util.Collection;


public abstract class AbstractFactAnalyzer implements FactAnalyzer {

    public String name;
    public String type;
    public String description;
    private Settings settings;
    private Project project;
    private Object object;
    private boolean enable = true;

    public AbstractFactAnalyzer(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain, Collection<StrutsAction> actionChain) throws FactAnalyzerException {
        this.analysis(object,factChain);
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Settings getSettings() {
        return settings;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public void initialize(Project project, Settings settings) {
        this.project = project;
        this.settings = settings;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getFactDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return getName() + "\n" + getFactDescription();
    }

}
