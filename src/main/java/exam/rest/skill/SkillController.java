package exam.rest.skill;

import exam.db.dto.ResponseObject;
import exam.db.dto.skill.CreateSkillRequest;
import exam.db.dto.skill.DeleteSkillRequest;
import exam.db.dto.skill.SkillResponse;
import exam.db.dto.skill.UpdateSkillRequest;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.skill.SkillService;
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

import java.util.List;

@RestController
public class SkillController {

	@Autowired
	private SkillService skillSv;

	@PostMapping(Endpoints.SKILL_URL)
	public ResponseEntity<Object> create(@RequestBody CreateSkillRequest request) {
		ResponseObject<SkillResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(skillSv.create(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.SKILL_URL)
	public ResponseEntity<Object> update(@RequestBody UpdateSkillRequest request) {
		ResponseObject<SkillResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(skillSv.update(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PutMapping(Endpoints.SKILL_DELETE_MULTIPLE)
	public ResponseEntity<Object> deleteMultiple(@RequestBody DeleteSkillRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(skillSv.deleteMultiple(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.SKILL_URL + Endpoints.PATH_VARIABLE_URL)
	public ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<SkillResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(skillSv.getDetail(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.SKILL_HEADER_URL)
	public ResponseEntity<Object> getAllSkillForHeader() {
		ResponseObject<List<SkillResponse>> response = new ResponseObject<>();
		response.setResponseData(skillSv.getAllSkillForHeader());
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
