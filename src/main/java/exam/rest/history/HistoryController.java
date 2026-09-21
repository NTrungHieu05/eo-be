package exam.rest.history;

import exam.db.dto.ResponseObject;
import exam.db.dto.history.HistoryRequest;
import exam.rest.endpoint.Endpoints;
import exam.service.history.HistoryService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryController {

	@Autowired
	private HistoryService historyService;

	@PostMapping(Endpoints.HISTORY_URL)
	public ResponseEntity<Object> create(@RequestBody HistoryRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(historyService.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
