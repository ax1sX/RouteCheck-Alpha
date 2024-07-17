package factAnalyzer;

import entry.Fact;
import entry.Settings;
import entry.StrutsAction;
import exceptions.FactAnalyzerException;
import project.entry.Module;

import java.util.Collection;


public abstract class AbstractFactAnalyzer implements FactAnalyzer {

    public String name;
    public String type;
    public String description;
    private Settings settings;
    private Module module;
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

    public Module getModule() {
        return module;
    }

    @Override
    public void initialize(Module module, Settings settings) {
        this.module = module;
        this.settings = settings;
    }

    @Override
    public void resetModule(Module module){
        this.module = module;
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
