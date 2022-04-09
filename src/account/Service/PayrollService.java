package account.Service;

import account.Model.AppUser;
import account.Model.Payroll;
import account.Model.PayrollRepresentation;
import account.Repository.PayrollRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.text.DateFormatSymbols;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final static Map<String, String> UPDATED_SUCCESSFULLY_MESSAGE = Map.of(
            "status", "Updated successfully!"
    );
    private final static Map<String, String> ADDED_SUCCESSFULLY_MESSAGE = Map.of(
            "status", "Added successfully!"
    );
    private final PayrollRepository payrollRepository;
    private final AppUserService appUserService;

    public PayrollService(PayrollRepository payrollRepository, AppUserService appUserService) {
        this.payrollRepository = payrollRepository;
        this.appUserService = appUserService;
    }

    @Transactional
    public Map<String, String> addPayrolls(List<Payroll> payrolls) throws ResponseStatusException {
        String errorMessage = saveAllValidPayrolls(payrolls);
        if (errorMessage != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return ADDED_SUCCESSFULLY_MESSAGE;
    }

    public Map<String, String> updateSalary(Payroll payroll) throws ResponseStatusException {
        Payroll updatedPayroll = payrollRepository
                .findPayrollByEmployeeIgnoreCaseAndPeriod(payroll.getEmployee(), payroll.getPeriod())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payroll doesn't exist"));
        updatedPayroll.setSalary(payroll.getSalary());
        payrollRepository.save(updatedPayroll);
        return UPDATED_SUCCESSFULLY_MESSAGE;
    }

    public ResponseEntity<Object> getPayrolls(String email, Optional<String> optionalPeriod) {
        return optionalPeriod
                .<ResponseEntity<Object>>map(period ->
                        ResponseEntity.ok(getPayrollByEmailAndPeriod(email, period)))
                .orElseGet(() -> ResponseEntity.ok(getPayrollsByEmail(email)));
    }

    public List<PayrollRepresentation> getPayrollsByEmail(String email) {
        List<Payroll> payrolls = payrollRepository.
                findPayrollByEmployeeIgnoreCaseOrderByPeriodDesc(email);
        return payrolls.stream()
                .map(this::getPayrollRepresentation)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public PayrollRepresentation getPayrollByEmailAndPeriod(String email,
                                                            String period) throws ResponseStatusException {
        Optional<Payroll> payroll = payrollRepository
                .findPayrollByEmployeeIgnoreCaseAndPeriod(email, period);
        return payroll
                .map(queriedPayroll -> getPayrollRepresentation(payroll.get()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No Payroll Found"));
    }

    public PayrollRepresentation getPayrollRepresentation(Payroll payroll) {
        AppUser appUser = appUserService.findUserByEmail(payroll.getEmployee());
        return new PayrollRepresentation(
                appUser.getName(), appUser.getLastname(),
                formatPeriod(payroll.getPeriod()), formatSalary(payroll.getSalary()));
    }

    public String formatPeriod(String period) {
        String[] periodArray = period.split("-");
        try {
            String month = new DateFormatSymbols()
                    .getMonths()[Integer.parseInt(periodArray[0]) - 1];
            return String.format("%s-%s", month, periodArray[1]);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Date");
        }
    }

    public String formatSalary(Long salary) {
        return String.format("%d dollar(s) %d cent(s)", salary / 100, salary % 100);
    }

    public boolean isPayrollUnique(String employee, String period) {
        return payrollRepository
                .findPayrollByEmployeeIgnoreCaseAndPeriod(employee, period).isEmpty();
    }

    @Transactional
    public String saveAllValidPayrolls(List<Payroll> payrolls) {
        for (Payroll payroll : payrolls) {
            if (!isPayrollUnique(payroll.getEmployee(), payroll.getPeriod())) {
                return "Payroll with same employee and period already exists";
            }
            int month = Integer.parseInt(payroll.getPeriod().split("-")[0]);
            if (month < 0 || month > 12) {
                return "Invalid date";
            }
            payrollRepository.save(payroll);
        }
        return null;
    }
}
