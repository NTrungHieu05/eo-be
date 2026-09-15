package exam.db.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Question {
	private String sound;
	private String image;
	private String hint;
	private String text;
}
