package exam.ultis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public final class FileUploadHelper {

	private FileUploadHelper() {
	}

	public static String downloadFileToBase64(String fileURL) throws IOException {
		File file = new File(fileURL);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		try (InputStream is = new FileInputStream(file);
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, read);
			}
			baos.flush();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
	}
}
