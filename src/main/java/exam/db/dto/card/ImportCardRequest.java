package exam.db.dto.card;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportCardRequest {
	private String filePath;
}
