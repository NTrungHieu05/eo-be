package exam.rest.admin;

import exam.db.dto.ResponseObject;
import exam.db.dto.card.ListCardRequest;
import exam.db.dto.card.ListCardResponse;
import exam.db.dto.exam.ListExamRequest;
import exam.db.dto.exam.ListExamResponse;
import exam.db.dto.skill.ListSkillRequest;
import exam.db.dto.skill.ListSkillResponse;
import exam.db.dto.topic.ListTopicRequest;
import exam.db.dto.topic.ListTopicResponse;
import exam.db.dto.user.BlockUserRequest;
import exam.db.dto.user.ListUserRequest;
import exam.db.dto.user.ListUserResponse;
import exam.db.dto.user.UnblockUserRequest;
import exam.rest.endpoint.Endpoints;
import exam.service.admin.AdminService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

	@Autowired
	private AdminService adminService;

	@GetMapping(Endpoints.ADMIN_USERS_URL)
	public ResponseEntity<Object> listSimpleUser(ListUserRequest request) {
		ResponseObject<ListUserResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(adminService.listUserForAdmin(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.ADMIN_USERS_BLOCK)
	public ResponseEntity<Object> blockUser(@RequestBody BlockUserRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(adminService.blockUser(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.ADMIN_USERS_UNBLOCK)
	public ResponseEntity<Object> unblockUser(@RequestBody UnblockUserRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(adminService.unblockUser(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.ADMIN_CARD_URL)
	public ResponseEntity<Object> getListCard(ListCardRequest request) {
		ResponseObject<ListCardResponse> responses = new ResponseObject<>();
		try {
			responses.setResponseData(adminService.getListCardForAdmin(request));
		} catch (ExamBaseException e) {
			responses.setError(e.getError());
		}
		return new ResponseEntity<>(responses, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.ADMIN_SKILL_URL)
	public ResponseEntity<Object> getListSkill(ListSkillRequest request) {
		ResponseObject<ListSkillResponse> responses = new ResponseObject<>();
		try {
			responses.setResponseData(adminService.getListSkillForAdmin(request));
		} catch (ExamBaseException e) {
			responses.setError(e.getError());
		}
		return new ResponseEntity<>(responses, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.ADMIN_TOPIC_URL)
	public ResponseEntity<Object> getListTopic(ListTopicRequest request) {
		ResponseObject<ListTopicResponse> responses = new ResponseObject<>();
		try {
			responses.setResponseData(adminService.getListTopicForAdmin(request));
		} catch (ExamBaseException e) {
			responses.setError(e.getError());
		}
		return new ResponseEntity<>(responses, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.ADMIN_EXAM_URL)
	public ResponseEntity<Object> getListExam(ListExamRequest request) {
		ResponseObject<ListExamResponse> responses = new ResponseObject<>();
		try {
			responses.setResponseData(adminService.getListExamForAdmin(request));
		} catch (ExamBaseException e) {
			responses.setError(e.getError());
		}
		return new ResponseEntity<>(responses, new HttpHeaders(), HttpStatus.OK);
	}
}
