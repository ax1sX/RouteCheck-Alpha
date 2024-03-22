package reporting;

import entry.Fact;
import entry.Settings;
import exceptions.ReportingException;
import org.codehaus.jettison.json.JSONObject;
import project.entry.Project;
import project.entry.Projects;
import utils.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonReportGenerator extends AbstractReportGenerator{
//    @Override
//    public void initialize(Project project, Collection<Fact> factChain, Settings settings) {
//        super.initialize(project, factChain, settings);
//    }


    @Override
    public void initialize(Projects projects, Settings settings) {
        super.initialize(projects, settings);
    }

    @Override
    public void write() throws ReportingException {
        try {
            String out = this.settings.getOutPutDirectory();
            Utils.mkDir(out);
            String filePath = out + File.separator + project.getName() + ".json";
            Map<String, Object> context = new HashMap<>();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
            String dataStr = df.format(date);
            context.put("project", this.project.getName());
            List<Map> facts = new ArrayList<>();
            this.factChain.forEach(fact -> {
                facts.add(Utils.objectToMap(fact));
            });
            context.put("facts", facts);
            context.put("date", dataStr);
            JSONObject jsonObject = new JSONObject(context);
            Utils.fileWriter(filePath, jsonObject.toString());
        }catch (Exception e){
            throw new ReportingException("");
        }


    }
}
