package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Topic;
import exam.db.repository.topic.TopicRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ChangeLog(order = "002")
@Slf4j
public class DatabaseChangelogTopic {

	@ChangeSet(id = "topic_default", author = "system", order = "001")
	public void initializerTopic(TopicRepository topicRepo) {
		List<Topic> topics = new ArrayList<>();
		topics.add(new Topic("Phần 1:Mô Tả Tranh"));
		topics.add(new Topic("Phần 2:Hỏi - Đáp"));
		topics.add(new Topic("Phần 3:Đoạn Hội Thoại"));
		topics.add(new Topic("Phần 4:Bài Nói Ngắn"));
		topics.add(new Topic("Phần 5:Hoàn Thành Câu"));
		topics.add(new Topic("Phần 6:Hoàn Thành Đoạn Văn"));
		topics.add(new Topic("Phần 7:Đọc Hiểu - Đoạn Đơn"));
		topics.add(new Topic("Phần 7:Đọc Hiểu - Đoạn Kép"));
		topics.add(new Topic("Phần 7:Đọc Hiểu - Đoạn Ba"));
		topicRepo.saveAll(topics);
		log.info("Seeded TOEIC topics Part 1-7");
	}
}
