package exam.rest.exam;

import exam.db.dto.ResponseObject;
import exam.db.dto.exam.CreateExamRequest;
import exam.db.dto.exam.DeleteExamRequest;
import exam.db.dto.exam.ExamResponse;
import exam.db.dto.exam.PracticeTestResponse;
import exam.db.dto.exam.UpdateExamRequest;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.exam.ExamService;
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
public class ExamController {

	@Autowired
	private ExamService examService;

	@PostMapping(Endpoints.EXAM_URL)
	public ResponseEntity<Object> create(@RequestBody CreateExamRequest request) {
		ResponseObject<ExamResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.EXAM_URL)
	public ResponseEntity<Object> update(@RequestBody UpdateExamRequest request) {
		ResponseObject<ExamResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.update(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.EXAM_DELETE_MULTIPLE)
	public ResponseEntity<Object> deleteMultiple(@RequestBody DeleteExamRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.deleteMultiple(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.EXAM_URL + Endpoints.PATH_VARIABLE_URL)
	public ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<ExamResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.getDetail(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.EXAM_CARD_URL + Endpoints.PATH_VARIABLE_EXAM_ID_URL)
	public ResponseEntity<Object> getListCard(@PathVariable(DBConst.EXAM_ID) String examId) {
		ResponseObject<ExamResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.getAllCardsByExamId(examId));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.PRACTICE_TEST_URL)
	public ResponseEntity<Object> getPracticeTest() {
		ResponseObject<PracticeTestResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(examService.getPracticeTest());
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
