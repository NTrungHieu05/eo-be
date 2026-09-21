package exam.db.dto.history;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HistoryRequest {
	private String userId;
	private Integer progress;
	private Integer status;
	private String examId;

	public void setProgress(Object progress) {
		if (progress == null) {
			this.progress = null;
			return;
		}
		if (progress instanceof Number) {
			this.progress = ((Number) progress).intValue();
			return;
		}
		this.progress = Integer.parseInt(progress.toString());
	}
}
