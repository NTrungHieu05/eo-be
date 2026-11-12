package exam.rest.question;

import exam.db.dto.ResponseObject;
import exam.db.dto.question.QuestionResponse;
import exam.db.dto.question.CreateQuestionRequest;
import exam.db.dto.question.DeleteQuestionRequest;
import exam.db.dto.question.UpdateQuestionRequest;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.question.QuestionService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {

	@Autowired
	private QuestionService questionSv;

	@PostMapping(Endpoints.QUESTION_URL)
	public final ResponseEntity<Object> create(@RequestBody CreateQuestionRequest request) {
		ResponseObject<QuestionResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(questionSv.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.QUESTION_URL)
	public final ResponseEntity<Object> update(@RequestBody UpdateQuestionRequest request) {
		ResponseObject<QuestionResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(questionSv.update(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.QUESTION_DELETE_MULTIPLE)
	public final ResponseEntity<Object> deleteMultiple(@RequestBody DeleteQuestionRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(questionSv.deleteMultiple(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.QUESTION_URL + Endpoints.PATH_VARIABLE_URL)
	public final ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<QuestionResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(questionSv.getDetail(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
