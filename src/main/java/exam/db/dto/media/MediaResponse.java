package exam.db.dto.media;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MediaResponse {
	protected String id;
	protected String userId;
	protected String mediaUrl;
	protected String filePath;
}
