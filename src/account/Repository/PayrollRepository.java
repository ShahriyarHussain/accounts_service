package account.Repository;

import account.Model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findPayrollByEmployeeIgnoreCaseAndPeriod(String employee, String period);

    List<Payroll> findPayrollByEmployeeIgnoreCaseOrderByPeriodDesc(String employee);
}
