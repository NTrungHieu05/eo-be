package exam.db.dto.exam;

import exam.db.dto.TotalResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListPracticeTestResponse extends TotalResponse {
	private List<PracticeTestResponse> exams = new ArrayList<>();

	public ListPracticeTestResponse(List<PracticeTestResponse> exams, long total) {
		this.exams = exams;
		super.setTotal(total);
	}
}
