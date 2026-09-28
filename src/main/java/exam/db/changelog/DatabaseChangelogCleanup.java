package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Card;
import exam.db.entity.Exam;
import exam.db.entity.Skill;
import exam.db.entity.Topic;
import exam.db.repository.card.CardRepository;
import exam.db.repository.exam.ExamRepository;
import exam.db.repository.skill.SkillRepository;
import exam.db.repository.topic.TopicRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ChangeLog(order = "007")
@Slf4j
public class DatabaseChangelogCleanup {

	@ChangeSet(id = "dedupe_skills_and_link_part_exams", author = "system", order = "001")
	public void dedupeSkillsAndLinkPartExams(SkillRepository skillRepo, TopicRepository topicRepo,
			ExamRepository examRepo, CardRepository cardRepo) {
		int removed = 0;
		for (Skill skill : skillRepo.findAllByDeletedIsFalse()) {
			if (skill.getTopicIds() == null || skill.getTopicIds().isEmpty()) {
				skill.setDeleted(true);
				skillRepo.save(skill);
				removed++;
			}
		}
		log.info("Soft-deleted {} skill(s) without topics", removed);

		Exam practice = examRepo.findFirstByPracticeTestIsTrueAndDeletedIsFalse();
		if (practice == null) {
			log.warn("Practice test not found, skip part-exam link");
			return;
		}
		List<Card> practiceCards = cardRepo.findByExamIdAndDeletedIsFalse(practice.getId());
		Map<String, List<String>> cardIdsByTopic = new LinkedHashMap<>();
		for (Card card : practiceCards) {
			if (card.getTopicId() == null) {
				continue;
			}
			cardIdsByTopic.computeIfAbsent(card.getTopicId(), key -> new ArrayList<>()).add(card.getId());
		}

		List<Topic> topics = topicRepo.findAllByDeletedIsFalse();
		int index = 1;
		int created = 0;
		for (Topic topic : topics) {
			List<String> cardIds = cardIdsByTopic.get(topic.getId());
			if (cardIds == null || cardIds.isEmpty()) {
				index++;
				continue;
			}
			String examId = String.format("68cd0001000000000000000%d", index);
			if (examRepo.findFirstByIdAndDeletedIsFalse(examId) == null) {
				Exam exam = new Exam();
				exam.setId(examId);
				exam.setName("Luyện " + topic.getName());
				exam.setTopicId(topic.getId());
				exam.setPracticeTest(false);
				exam.setCardIds(cardIds);
				examRepo.save(exam);
				created++;
			}
			List<String> examIds = topic.getExamIds() == null ? new ArrayList<>() : new ArrayList<>(topic.getExamIds());
			if (!examIds.contains(examId)) {
				examIds.add(examId);
				topic.setExamIds(examIds);
				topicRepo.save(topic);
			}
			index++;
		}
		log.info("Linked part practice exams for {} topics, created {}", topics.size(), created);
	}
}
