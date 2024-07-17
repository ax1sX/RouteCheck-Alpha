package reporting;

import exceptions.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Project;
import project.entry.Module;
import entry.Settings;
import entry.Fact;
import utils.Command;

import java.util.Collection;

public class AbstractReportGenerator implements ReportGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportGenerator.class);

    public Command command;
    public Settings settings;
    public Module module;
    public Project project;
    public Collection<Fact> factChain;

    @Override
    public void initialize(Project project, Settings settings) {
        this.project = project;
        this.settings = settings;
    }


    @Override
    public void write() throws ReportingException {

    }
}
