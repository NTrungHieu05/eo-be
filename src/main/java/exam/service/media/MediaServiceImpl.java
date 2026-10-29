package exam.service.media;

import exam.db.dto.media.MediaResponse;
import exam.db.dto.user.ExamUser;
import exam.db.entity.Media;
import exam.db.enums.Assert;
import exam.db.enums.ErrorInfo;
import exam.db.repository.media.MediaRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.FileUploadHelper;
import exam.ultis.SecurityContextService;
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
		requireAdmin();
		Assert.notNull(file, ErrorInfo.BAD_REQUEST);
		if (file.isEmpty()) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		String originalName = file.getOriginalFilename();
		Assert.notEmpty(originalName, ErrorInfo.BAD_REQUEST);
		String fileName = Paths.get(originalName).getFileName().toString();
		String safeName = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
		if (!StringUtils.hasText(safeName) || ".".equals(safeName) || "..".equals(safeName)) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		String storedName = System.currentTimeMillis() + "-" + safeName;

		String folder = folderFor(fileName, type);
		Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
		Path directory = root.resolve(folder).normalize();
		Path target = directory.resolve(storedName).normalize();
		if (!target.startsWith(root)) {
			throw new ExamBaseException(ErrorInfo.BAD_REQUEST);
		}
		try {
			Files.createDirectories(directory);
			File destination = target.toFile();
			file.transferTo(destination);

			String publicUrl = "/data/" + folder + "/" + storedName;
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
			Path root = Paths.get(uploadRoot).toAbsolutePath().normalize();
			String relative = fileURL.startsWith("/data/") ? fileURL.substring("/data/".length()) : fileURL;
			Path target = root.resolve(relative).normalize();
			if (!target.startsWith(root)) {
				return null;
			}
			return FileUploadHelper.downloadFileToBase64(target.toString());
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

	private void requireAdmin() throws ExamBaseException {
		ExamUser examUser = SecurityContextService.getUser();
		Assert.notNull(examUser, ErrorInfo.ACCESS_DENIED_ERROR);
		Assert.isTrue(examUser.isAdmin(), ErrorInfo.ACCESS_DENIED_ERROR);
	}
}
