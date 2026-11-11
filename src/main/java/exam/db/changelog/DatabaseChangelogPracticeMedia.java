package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.repository.question.QuestionRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;

@ChangeLog(order = "007")
@Slf4j
public class DatabaseChangelogPracticeMedia {

	@ChangeSet(id = "sample_part1_photo_audio", author = "system", order = "001")
	public void attachSamplePart1Media(QuestionRepository questionRepo) {
		Question question = questionRepo.findFirstByIdAndDeletedIsFalse(DatabaseChangelogSample.SAMPLE_QUESTION_ID);
		if (question == null || question.getQuestion() == null || question.getAnswer() == null) {
			log.warn("Sample Part 1 question is missing, skip media attach");
			return;
		}
		question.getQuestion().setText("Look at the picture. What is the man doing?");
		question.getQuestion().setImage("/data/images/part1-1.png");
		question.getQuestion().setSound("/data/sounds/part1-1.wav");
		Answer answer = question.getAnswer();
		answer.setChoices(Arrays.asList(
				"He is running in a park.",
				"He is cooking in a kitchen.",
				"He is driving a truck."));
		answer.setTexts(Collections.singletonList("He is sitting at a desk."));
		answer.setHint("A");
		questionRepo.save(question);
		log.info("Attached Part 1 photo and audio to sample question {}", question.getId());
	}
}
