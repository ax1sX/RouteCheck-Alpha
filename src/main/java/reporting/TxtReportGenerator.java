package reporting;

import entry.Settings;
import exceptions.ReportingException;
import project.entry.Projects;
import utils.Utils;

import java.io.File;

public class TxtReportGenerator extends AbstractReportGenerator{
    @Override
    public void initialize(Projects projects, Settings settings) {
        super.initialize(projects, settings);
    }

/*    @Override
    public void write() throws ReportingException {
        try {
            String out = this.settings.getOutPutDirectory();
            Utils.mkDir(out);
            String fileName = out + File.separator + "test.txt";
            String html = render();
            Utils.fileWriter(fileName, html);
        }catch (Exception e){
            throw new ReportingException("");
        }

    }
    */

}
