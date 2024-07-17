package reporting;

import exceptions.ReportingException;
import entry.Settings;
import project.entry.Project;

public interface ReportGenerator {
//    void initialize(Project project, Collection<Fact> factChain, Settings settings);
//    void initialize(Projects projects, Collection<Fact> factChain, Settings settings);

    void initialize(Project project, Settings settings);
    void write() throws ReportingException;
}
