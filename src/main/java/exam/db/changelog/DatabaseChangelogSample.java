package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Question;
import exam.db.entity.Role;
import exam.db.entity.Topic;
import exam.db.entity.User;
import exam.db.enums.RoleEnum;
import exam.db.repository.card.CardRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.role.RoleRepository;
import exam.db.repository.topic.TopicRepository;
import exam.db.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@ChangeLog(order = "005")
@Slf4j
public class DatabaseChangelogSample {

	public static final String SAMPLE_EXAM_ID = "68ca00010000000000000001";
	public static final String SAMPLE_CARD_ID = "68ca00020000000000000001";
	public static final String SAMPLE_USER_ID = "68ca00030000000000000001";
	public static final String SAMPLE_USER_EMAIL = "user@toeic.local";
	public static final String SAMPLE_USER_PASSWORD = "User@123";
	public static final String SAMPLE_TOPIC_NAME = "Phần 1:Mô Tả Tranh";

	@ChangeSet(id = "sample_exam_card_user", author = "system", order = "001")
	public void seedSampleContent(TopicRepository topicRepo, ExamRepository examRepo, CardRepository cardRepo,
			UserRepository userRepo, RoleRepository roleRepo) {
		Topic part1 = topicRepo.findAllByDeletedIsFalse().stream()
				.filter(topic -> SAMPLE_TOPIC_NAME.equals(topic.getName()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Missing topic: " + SAMPLE_TOPIC_NAME));

		if (examRepo.findFirstByIdAndDeletedIsFalse(SAMPLE_EXAM_ID) == null) {
			Exam exam = new Exam();
			exam.setId(SAMPLE_EXAM_ID);
			exam.setName("Exam mẫu Part 1");
			exam.setTopicId(part1.getId());
			exam.setCardIds(new ArrayList<>(Collections.singletonList(SAMPLE_CARD_ID)));
			examRepo.save(exam);

			ArrayList<String> examIds = part1.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(part1.getExamIds());
			if (!examIds.contains(SAMPLE_EXAM_ID)) {
				examIds.add(SAMPLE_EXAM_ID);
				part1.setExamIds(examIds);
				topicRepo.save(part1);
			}
			log.info("Seeded sample exam {}", SAMPLE_EXAM_ID);
		}

		if (cardRepo.findFirstByIdAndDeletedIsFalse(SAMPLE_CARD_ID) == null) {
			Question question = new Question();
			question.setText("What is in the picture?");
			question.setHint("Look at the photo");
			question.setImage("");
			question.setSound("");

			Answer answer = new Answer();
			answer.setChoices(Arrays.asList(
					"A man is sitting.",
					"A woman is walking.",
					"A child is running.",
					"A dog is sleeping."));
			answer.setTexts(Collections.singletonList("A"));
			answer.setHint("A");
			answer.setImage("");

			Card card = new Card();
			card.setId(SAMPLE_CARD_ID);
			card.setExamId(SAMPLE_EXAM_ID);
			card.setTopicId(part1.getId());
			card.setQuestion(question);
			card.setAnswer(answer);
			card.setIsQuestionGroup(false);
			cardRepo.save(card);
			log.info("Seeded sample card {}", SAMPLE_CARD_ID);
		}

		if (userRepo.findByEmailIgnoreCase(SAMPLE_USER_EMAIL) == null) {
			Role userRole = roleRepo.findByName(RoleEnum.ROLE_USER.getName());
			if (userRole == null) {
				throw new IllegalStateException("ROLE_USER must be seeded before sample user");
			}
			User user = new User();
			user.setUserId(SAMPLE_USER_ID);
			user.setEmail(SAMPLE_USER_EMAIL);
			user.setUsername(SAMPLE_USER_EMAIL);
			user.setPassword(new BCryptPasswordEncoder().encode(SAMPLE_USER_PASSWORD));
			user.setDisplayName("Tester");
			user.setFirstName("Test");
			user.setLastName("User");
			user.setEnabled(true);
			user.setAccountNonExpired(true);
			user.setAccountNonLocked(true);
			user.setCredentialsNonExpired(true);
			user.setOtp(null);
			user.getRoles().add(userRole);
			userRepo.save(user);
			log.info("Seeded sample user {}", SAMPLE_USER_EMAIL);
		}
	}
}
