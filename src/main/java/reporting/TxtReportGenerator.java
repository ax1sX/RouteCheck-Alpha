package reporting;

import entry.Settings;
import project.entry.Project;

public class TxtReportGenerator extends AbstractReportGenerator{
    @Override
    public void initialize(Project project, Settings settings) {
        super.initialize(project, settings);
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
