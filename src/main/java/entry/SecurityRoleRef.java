package entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityRoleRef {
    private String roleName;
    private String roleLink;
    private String description;
}
