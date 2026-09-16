package exam.db.dto.exam;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ListExamResponse extends TotalResponse {
	private List<ExamResponse> exams = new ArrayList<>();

	public ListExamResponse(List<ExamResponse> exams, long total) {
		this.exams = exams;
		super.setTotal(total);
	}
}
