package exam.service.history;

import exam.db.dto.history.HistoryRequest;
import exam.ultis.ExamBaseException;

public interface HistoryService {
	Boolean create(HistoryRequest request) throws ExamBaseException;
}
