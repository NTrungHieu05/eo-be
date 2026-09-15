package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Skill;
import exam.db.entity.Topic;
import exam.db.repository.skill.SkillRepository;
import exam.db.repository.topic.TopicRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ChangeLog(order = "003")
@Slf4j
public class DatabaseChangelogSkill {

	@ChangeSet(id = "skill_default_linked", author = "system", order = "001")
	public void initializerSkill(SkillRepository skillRepo, TopicRepository topicRepo) {
		Map<String, String> topicIdByName = topicRepo.findAll().stream()
				.collect(Collectors.toMap(Topic::getName, Topic::getId));

		Skill listening = new Skill("Luyện Nghe");
		listening.setTopicIds(ids(topicIdByName,
				"Phần 1:Mô Tả Tranh",
				"Phần 2:Hỏi - Đáp",
				"Phần 3:Đoạn Hội Thoại",
				"Phần 4:Bài Nói Ngắn"));

		Skill reading = new Skill("Luyện Đọc");
		reading.setTopicIds(ids(topicIdByName,
				"Phần 5:Hoàn Thành Câu",
				"Phần 6:Hoàn Thành Đoạn Văn",
				"Phần 7:Đọc Hiểu - Đoạn Đơn",
				"Phần 7:Đọc Hiểu - Đoạn Kép",
				"Phần 7:Đọc Hiểu - Đoạn Ba"));

		skillRepo.saveAll(Arrays.asList(listening, reading));
		log.info("Seeded skills and linked Part 1-4 to Listening, Part 5-7 to Reading");
	}

	private List<String> ids(Map<String, String> topicIdByName, String... names) {
		return Arrays.stream(names)
				.map(name -> {
					String id = topicIdByName.get(name);
					if (id == null) {
						throw new IllegalStateException("Missing topic: " + name);
					}
					return id;
				})
				.collect(Collectors.toList());
	}
}
