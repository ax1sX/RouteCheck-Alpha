package reporting;

import exceptions.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Project;
import entry.Settings;
import entry.Fact;
import project.entry.Projects;

import java.util.Collection;

public class AbstractReportGenerator implements ReportGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportGenerator.class);

    public Settings settings;
    public Project project;
    /**
     * 新增projects
     */
    public Projects projects;
    public Collection<Fact> factChain;


    /**
     * 要改成多项目结构的Projects
     */
//
//    @Override
//    public void initialize(Project project, Collection<Fact> factChain, Settings settings) {
//        this.project = project;
//        this.factChain = factChain;
//        this.settings = settings;
//    }

    @Override
    public void initialize(Projects projects, Settings settings) {
        this.projects = projects;
        this.settings = settings;
    }


    @Override
    public void write() throws ReportingException {

    }
}
