package reporting;

import java.util.Locale;

public class ReporterFactory {
    public static ReportGenerator getReportGenerator(String type){
        if(type.toLowerCase(Locale.ROOT).equals("html")){
            ReportGenerator reportGenerator = new HtmlReportGenerator();
            return reportGenerator;
        }
        if(type.toLowerCase(Locale.ROOT).equals("json")){
            ReportGenerator reportGenerator = new JsonReportGenerator();
            return reportGenerator;
        }
        return null;
    }
}
