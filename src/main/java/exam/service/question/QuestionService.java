package exam.service.question;

import exam.db.dto.question.QuestionResponse;
import exam.db.dto.question.CreateQuestionRequest;
import exam.db.dto.question.DeleteQuestionRequest;
import exam.db.dto.question.ListQuestionRequest;
import exam.db.dto.question.ListQuestionResponse;
import exam.db.dto.question.UpdateQuestionRequest;
import exam.ultis.ExamBaseException;

public interface QuestionService {
	QuestionResponse create(CreateQuestionRequest request) throws ExamBaseException;

	QuestionResponse update(UpdateQuestionRequest request) throws ExamBaseException;

	Boolean deleteMultiple(DeleteQuestionRequest request) throws ExamBaseException;

	QuestionResponse getDetail(String id) throws ExamBaseException;

	ListQuestionResponse getListQuestionForAdmin(ListQuestionRequest request) throws ExamBaseException;
}
