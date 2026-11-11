package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Question;
import exam.db.entity.Exam;
import exam.db.entity.Skill;
import exam.db.entity.Topic;
import exam.db.repository.question.QuestionRepository;
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
			ExamRepository examRepo, QuestionRepository questionRepo) {
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
		List<Question> practiceQuestions = questionRepo.findByExamIdAndDeletedIsFalse(practice.getId());
		Map<String, List<String>> questionIdsByTopic = new LinkedHashMap<>();
		for (Question question : practiceQuestions) {
			if (question.getTopicId() == null) {
				continue;
			}
			questionIdsByTopic.computeIfAbsent(question.getTopicId(), key -> new ArrayList<>()).add(question.getId());
		}

		List<Topic> topics = topicRepo.findAllByDeletedIsFalse();
		int index = 1;
		int created = 0;
		for (Topic topic : topics) {
			List<String> questionIds = questionIdsByTopic.get(topic.getId());
			if (questionIds == null || questionIds.isEmpty()) {
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
				exam.setQuestionIds(questionIds);
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

	@ChangeSet(id = "link_part_exams_from_practice_card_ids", author = "system", order = "002")
	public void linkPartExamsFromPracticeQuestionIds(TopicRepository topicRepo, ExamRepository examRepo,
			QuestionRepository questionRepo) {
		List<Exam> practices = examRepo.findAllByPracticeTestIsTrueAndDeletedIsFalse();
		if (practices == null || practices.isEmpty()) {
			log.warn("Practice tests not found, skip part-exam link from questionIds");
			return;
		}
		Exam practice = practices.get(0);
		List<String> practiceQuestionIds = practice.getQuestionIds();
		if (practiceQuestionIds == null || practiceQuestionIds.isEmpty()) {
			return;
		}
		List<Question> practiceQuestions = questionRepo.findByIdInAndDeletedIsFalse(practiceQuestionIds);
		Map<String, List<String>> questionIdsByTopic = new LinkedHashMap<>();
		for (Question question : practiceQuestions) {
			if (question.getTopicId() == null) {
				continue;
			}
			questionIdsByTopic.computeIfAbsent(question.getTopicId(), key -> new ArrayList<>()).add(question.getId());
		}

		List<Topic> topics = topicRepo.findAllByDeletedIsFalse();
		int index = 1;
		int created = 0;
		for (Topic topic : topics) {
			List<String> questionIds = questionIdsByTopic.get(topic.getId());
			if (questionIds == null || questionIds.isEmpty()) {
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
				exam.setQuestionIds(questionIds);
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
		log.info("Linked part exams from practice questionIds, created {}", created);
	}
}
