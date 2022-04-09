package account.Controller;

import account.Model.Payroll;
import account.Service.PayrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AccountsController {

    private final PayrollService payrollService;

    public AccountsController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @ExceptionHandler({ConstraintViolationException.class, org.hibernate.exception.ConstraintViolationException.class})
    public void springHandleNotFound(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    @PostMapping("/acct/payments")
    public Map<String, String> uploadPayrolls(@RequestBody List<@Valid Payroll> payrolls) {
        return payrollService.addPayrolls(payrolls);
    }

    @PutMapping("/acct/payments")
    public Map<String, String> changeSalary(@RequestBody @Valid Payroll payroll) {
        return payrollService.updateSalary(payroll);
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<Object> showPayrollByUserAndPeriod(@AuthenticationPrincipal UserDetails userDetails,
                                                             @RequestParam Optional<String> period) {
        return payrollService.getPayrolls(userDetails.getUsername(), period);
    }

}
