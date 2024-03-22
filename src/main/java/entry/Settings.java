package entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    private Map<String, List<String>> factAnalyzers;
    private String outPutDirectory;
    private String tempDirectory;
    private String reportType;
}
