package exam.db.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Answer {
	private List<String> choices;
	private String hint;
	private String image;
	private List<String> texts;
}
