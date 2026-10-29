package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.repository.card.CardRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;

@ChangeLog(order = "007")
@Slf4j
public class DatabaseChangelogPracticeMedia {

	@ChangeSet(id = "sample_part1_photo_audio", author = "system", order = "001")
	public void attachSamplePart1Media(CardRepository cardRepo) {
		Card card = cardRepo.findFirstByIdAndDeletedIsFalse(DatabaseChangelogSample.SAMPLE_CARD_ID);
		if (card == null || card.getQuestion() == null || card.getAnswer() == null) {
			log.warn("Sample Part 1 card is missing, skip media attach");
			return;
		}
		card.getQuestion().setText("Look at the picture. What is the man doing?");
		card.getQuestion().setImage("/data/images/part1-1.png");
		card.getQuestion().setSound("/data/sounds/part1-1.wav");
		Answer answer = card.getAnswer();
		answer.setChoices(Arrays.asList(
				"He is running in a park.",
				"He is cooking in a kitchen.",
				"He is driving a truck."));
		answer.setTexts(Collections.singletonList("He is sitting at a desk."));
		answer.setHint("A");
		cardRepo.save(card);
		log.info("Attached Part 1 photo and audio to sample card {}", card.getId());
	}
}
