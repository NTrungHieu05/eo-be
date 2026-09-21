package exam.service.history;

import exam.db.dto.history.HistoryRequest;
import exam.db.dto.user.ExamUser;
import exam.db.entity.ExamResult;
import exam.db.entity.History;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.history.HistoryRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	private HistoryRepository historyRepo;

	@Override
	public Boolean create(HistoryRequest request) throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Assert.notEmpty(request.getExamId(), ErrorInfo.EXAM_NOT_FOUND_ERROR);

		History history = historyRepo.findByUserIdAndDeletedIsFalse(examUser.getUserId());
		if (history == null) {
			history = new History();
			history.setUserId(examUser.getUserId());
			history.setStatus(request.getStatus());
			history.setExamResults(new ArrayList<>());
		}

		List<ExamResult> examResults = history.getExamResults() == null
				? new ArrayList<>()
				: history.getExamResults();
		boolean found = false;
		for (ExamResult examResult : examResults) {
			if (request.getExamId().equals(examResult.getId())) {
				examResult.setProgress(request.getProgress());
				found = true;
				break;
			}
		}
		if (!found) {
			ExamResult examResult = new ExamResult();
			examResult.setId(request.getExamId());
			examResult.setProgress(request.getProgress());
			examResults.add(examResult);
		}
		history.setExamResults(examResults);
		if (request.getStatus() != null) {
			history.setStatus(request.getStatus());
		}
		historyRepo.save(history);
		return Boolean.TRUE;
	}
}
