package exam.rest.card;

import exam.db.dto.ResponseObject;
import exam.db.dto.card.CardResponse;
import exam.db.dto.card.CreateCardRequest;
import exam.db.dto.card.DeleteCardRequest;
import exam.db.dto.card.UpdateCardRequest;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.card.CardService;
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
public class CardController {

	@Autowired
	private CardService cardSv;

	@PostMapping(Endpoints.CARD_URL)
	public ResponseEntity<Object> create(@RequestBody CreateCardRequest request) {
		ResponseObject<CardResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(cardSv.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.CARD_URL)
	public ResponseEntity<Object> update(@RequestBody UpdateCardRequest request) {
		ResponseObject<CardResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(cardSv.update(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.CARD_DELETE_MULTIPLE)
	public ResponseEntity<Object> deleteMultiple(@RequestBody DeleteCardRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(cardSv.deleteMultiple(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.CARD_URL + Endpoints.PATH_VARIABLE_URL)
	public ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<CardResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(cardSv.getDetail(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
