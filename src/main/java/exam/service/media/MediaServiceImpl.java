package exam.service.media;

import exam.db.dto.media.MediaResponse;
import exam.db.entity.Media;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.media.MediaRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.FileUploadHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MediaServiceImpl implements MediaService {

	@Value("${app.upload.root:D:/DATN/eo-be/data}")
	private String uploadRoot;

	@Autowired
	private MediaRepository mediaRepo;

	@Override
	public MediaResponse uploadMedia(MultipartFile file, Integer type) throws ExamBaseException {
		Assert.notNull(file, ErrorInfo.BAD_REQUEST);
		if (file.isEmpty()) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		String originalName = file.getOriginalFilename();
		Assert.notEmpty(originalName, ErrorInfo.BAD_REQUEST);
		String fileName = Paths.get(originalName).getFileName().toString();

		String folder = folderFor(fileName, type);
		Path directory = Paths.get(uploadRoot, folder);
		try {
			Files.createDirectories(directory);
			File destination = directory.resolve(fileName).toFile();
			file.transferTo(destination);

			String publicUrl = "/data/" + folder + "/" + fileName;
			Media media = new Media();
			media.setMediaUrl(publicUrl);
			media.setType(type);
			mediaRepo.save(media);

			MediaResponse response = new MediaResponse();
			response.setId(media.getId());
			response.setUserId(media.getUserId());
			response.setMediaUrl(publicUrl);
			response.setFilePath(destination.getAbsolutePath().replace('\\', '/'));
			return response;
		} catch (IOException e) {
			throw new ExamBaseException(ErrorInfo.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public String getDataBase64FromUrl(String fileURL) {
		if (!StringUtils.hasText(fileURL)) {
			return null;
		}
		try {
			String resolved = fileURL;
			if (fileURL.startsWith("/data/")) {
				resolved = Paths.get(uploadRoot, fileURL.substring("/data/".length())).toString();
			}
			return FileUploadHelper.downloadFileToBase64(resolved);
		} catch (Exception ex) {
			return null;
		}
	}

	private String folderFor(String fileName, Integer type) {
		String lower = fileName.toLowerCase();
		if (type != null && type == 1) {
			return "images";
		}
		if (type != null && type == 2) {
			return "sounds";
		}
		if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
				|| lower.endsWith(".gif") || lower.endsWith(".webp")) {
			return "images";
		}
		if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")
				|| lower.endsWith(".ogg")) {
			return "sounds";
		}
		return "files";
	}
}
