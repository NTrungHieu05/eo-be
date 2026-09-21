package exam.service.exam;

import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.ultis.ExamBaseException;

public interface ExamService {
	ExamResponse create(CreateExamRequest request) throws ExamBaseException;

	ExamResponse update(UpdateExamRequest request) throws ExamBaseException;

	Boolean deleteMultiple(DeleteExamRequest request) throws ExamBaseException;

	ExamResponse getDetail(String id) throws ExamBaseException;

	ListExamResponse getListExamForAdmin(ListExamRequest request) throws ExamBaseException;

	ExamResponse getAllCardsByExamId(String examId) throws ExamBaseException;

	PracticeTestResponse getPracticeTest() throws ExamBaseException;
}
