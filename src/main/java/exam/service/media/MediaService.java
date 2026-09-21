package exam.service.media;

import exam.db.dto.media.MediaResponse;
import exam.ultis.ExamBaseException;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
	MediaResponse uploadMedia(MultipartFile file, Integer type) throws ExamBaseException;

	String getDataBase64FromUrl(String fileURL);
}
