package reporting;

import exceptions.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Project;
import entry.Settings;
import entry.Fact;
import project.entry.Projects;
import utils.Command;

import java.util.Collection;

public class AbstractReportGenerator implements ReportGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportGenerator.class);

    public Command command;
    public Settings settings;
    public Project project;
    public Projects projects;
    public Collection<Fact> factChain;

    @Override
    public void initialize(Projects projects, Settings settings) {
        this.projects = projects;
        this.settings = settings;
    }


    @Override
    public void write() throws ReportingException {

    }
}
