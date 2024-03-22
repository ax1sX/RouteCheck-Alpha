package reporting;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import exceptions.ReportingException;
import project.entry.Project;
import entry.Settings;
import project.entry.Projects;
import utils.Utils;
import entry.Fact;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HtmlReportGenerator extends AbstractReportGenerator{
    private final String TEMPLATE_PATH = "report.html";

    protected String render() throws IOException {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Maps.newHashMap();
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        String dateStr = df.format(date);
        context.put("projects", this.projects);
        context.put("date", dateStr);
        String template = Resources.toString(Resources.getResource(TEMPLATE_PATH), Charsets.UTF_8);
        String renderedTemplate = jinjava.render(template, context);
        return renderedTemplate;
    }

    @Override
    public void initialize(Projects projects, Settings settings) {
        super.initialize(projects, settings);
    }

    @Override
    public void write() throws ReportingException {
        try {
            String out = this.settings.getOutPutDirectory();
            Utils.mkDir(out);
            String fileName = out + File.separator + "test.html";
            String html = render();
            Utils.fileWriter(fileName, html);
        }catch (Exception e){
            throw new ReportingException("");
        }

    }
}

// report.html测试代码
//        Set<Map.Entry<Project, List<Fact>>> projectEntries = projects.getAllProjectsAndFactChains();
//        String prevProjectName = null;
//        for (Map.Entry<Project, List<Fact>> entry : projectEntries) {
//            Project project = entry.getKey();
//            List<Fact> facts = entry.getValue();
//            if (!Objects.equals(prevProjectName, project.getName())) {
//                System.out.println(project.getName());
//                prevProjectName=project.getName();
//            }
//            System.out.println(project.getName());
//            for (Fact fact : facts) {
//                System.out.println(" - Routes: " + fact.getRoutes());
//                System.out.println(" - Class Name: " + fact.getClassName());
//                System.out.println(" - Method: " + fact.getMethod());
//                System.out.println(" - Description: " + fact.getDescription());
//                System.out.println(" - Fact Name: " + fact.getFactName());
//            }
//        }