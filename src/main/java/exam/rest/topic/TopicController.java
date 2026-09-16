package exam.rest.topic;

import exam.db.dto.ResponseObject;
import exam.db.dto.topic.CreateTopicRequest;
import exam.db.dto.topic.DeleteTopicRequest;
import exam.db.dto.topic.TopicResponse;
import exam.db.dto.topic.UpdateTopicRequest;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.topic.TopicService;
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
public class TopicController {

	@Autowired
	private TopicService topicSv;

	@PostMapping(Endpoints.TOPIC_URL)
	public ResponseEntity<Object> create(@RequestBody CreateTopicRequest request) {
		ResponseObject<TopicResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(topicSv.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.TOPIC_URL)
	public ResponseEntity<Object> update(@RequestBody UpdateTopicRequest request) {
		ResponseObject<TopicResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(topicSv.update(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.TOPIC_DELETE_MULTIPLE)
	public ResponseEntity<Object> deleteMultiple(@RequestBody DeleteTopicRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(topicSv.deleteMultiple(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.TOPIC_URL + Endpoints.PATH_VARIABLE_URL)
	public ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<TopicResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(topicSv.getDetail(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.TOPIC_EXAM_URL + Endpoints.PATH_VARIABLE_TOPIC_ID_URL)
	public ResponseEntity<Object> getAllExamByTopicId(@PathVariable(DBConst.TOPIC_ID) String topicId) {
		ResponseObject<TopicResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(topicSv.getAllExamByTopicId(topicId));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
