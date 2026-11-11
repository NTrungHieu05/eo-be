package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.QuestionContent;
import exam.db.entity.Role;
import exam.db.entity.Topic;
import exam.db.entity.User;
import exam.db.enums.RoleEnum;
import exam.db.repository.question.QuestionRepository;
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
	public static final String SAMPLE_QUESTION_ID = "68ca00020000000000000001";
	public static final String SAMPLE_USER_ID = "68ca00030000000000000001";
	public static final String SAMPLE_USER_EMAIL = "user@toeic.local";
	public static final String SAMPLE_USER_PASSWORD = "User@123";
	public static final String SAMPLE_TOPIC_NAME = "Phần 1:Mô Tả Tranh";

	@ChangeSet(id = "sample_exam_card_user", author = "system", order = "001")
	public void seedSampleContent(TopicRepository topicRepo, ExamRepository examRepo, QuestionRepository questionRepo,
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
			exam.setQuestionIds(new ArrayList<>(Collections.singletonList(SAMPLE_QUESTION_ID)));
			examRepo.save(exam);

			ArrayList<String> examIds = part1.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(part1.getExamIds());
			if (!examIds.contains(SAMPLE_EXAM_ID)) {
				examIds.add(SAMPLE_EXAM_ID);
				part1.setExamIds(examIds);
				topicRepo.save(part1);
			}
			log.info("Seeded sample exam {}", SAMPLE_EXAM_ID);
		}

		if (questionRepo.findFirstByIdAndDeletedIsFalse(SAMPLE_QUESTION_ID) == null) {
			QuestionContent content = new QuestionContent();
			content.setText("What is in the picture?");
			content.setHint("Look at the photo");
			content.setImage("");
			content.setSound("");

			Answer answer = new Answer();
			answer.setChoices(Arrays.asList(
					"A woman is walking.",
					"A child is running.",
					"A dog is sleeping."));
			answer.setTexts(Collections.singletonList("A man is sitting."));
			answer.setHint("A");
			answer.setImage("");

			Question question = new Question();
			question.setId(SAMPLE_QUESTION_ID);
			question.setExamId(SAMPLE_EXAM_ID);
			question.setTopicId(part1.getId());
			question.setQuestion(content);
			question.setAnswer(answer);
			question.setIsQuestionGroup(false);
			questionRepo.save(question);
			log.info("Seeded sample question {}", SAMPLE_QUESTION_ID);
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
