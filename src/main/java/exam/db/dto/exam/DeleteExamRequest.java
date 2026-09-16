package exam.db.dto.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DeleteExamRequest {
	private List<String> ids;
}
