package exam.rest.media;

import exam.db.dto.ResponseObject;
import exam.rest.endpoint.Endpoints;
import exam.service.media.MediaService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MediaController {

	@Autowired
	private MediaService mediaSv;

	@PostMapping(path = Endpoints.UPLOAD_URL, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<Object> uploadImage(@RequestPart MultipartFile file, @RequestParam(required = false) Integer type) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			response.setResponseData(mediaSv.uploadMedia(file, type == null ? 0 : type));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.MEDIA_URL_BASE64)
	public ResponseEntity<Object> getBase64FromUrl(@RequestParam String fileURL) {
		ResponseObject<Object> response = new ResponseObject<>();
		response.setResponseData(mediaSv.getDataBase64FromUrl(fileURL));
		return ResponseEntity.ok(response);
	}
}
