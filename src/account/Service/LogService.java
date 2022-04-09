package account.Service;

import account.Model.LogEntity;
import account.Model.LogEvents;
import account.Repository.LogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void saveLog(LogEvents action, String subject, String object, String path) {

        logRepository.save(LogEntity.builder().action(action).subject(subject).object(object).path(path).build());
    }

    public List<LogEntity> showAllLogs() {
        return logRepository.findAll();
    }
}
