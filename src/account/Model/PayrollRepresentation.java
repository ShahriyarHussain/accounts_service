package account.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayrollRepresentation {

    private String name;
    private String lastname;
    private String period;
    private String salary;
}
