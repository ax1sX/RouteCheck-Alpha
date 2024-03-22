package entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servlet {
    private String servletName;
    private String ServletClass;
    private String jspFile;
    private String loadOnStartup;
    private String runAs;
    private SecurityRoleRef securityRoleRef;
}
