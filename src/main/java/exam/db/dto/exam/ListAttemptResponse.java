package exam.db.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListAttemptResponse {
	private List<AttemptSummaryResponse> attempts = new ArrayList<>();
	private long total;
}
