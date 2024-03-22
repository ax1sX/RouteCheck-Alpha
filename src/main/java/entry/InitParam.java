package entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitParam {
    private String paramName;
    private String paramValue;
    private String description;
}
