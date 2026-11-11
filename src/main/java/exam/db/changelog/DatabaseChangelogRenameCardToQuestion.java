package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;

@ChangeLog(order = "009")
public class DatabaseChangelogRenameCardToQuestion {

	@ChangeSet(id = "rename_card_collection_to_question", author = "system", order = "001")
	public void renameCardToQuestion(MongockTemplate mongoTemplate) {
		MongoDatabase db = mongoTemplate.getDb();
		if (hasCollection(db, "card") && !hasCollection(db, "question")) {
			db.getCollection("card").renameCollection(new MongoNamespace(db.getName(), "question"));
		}
		if (!hasCollection(db, "question")) {
			return;
		}
		MongoCollection<Document> questions = db.getCollection("question");
		questions.updateMany(new Document(), Updates.rename("childCards", "childQuestions"));
		questions.updateMany(new Document("_class", "exam.db.entity.Card"),
				Updates.set("_class", "exam.db.entity.Question"));
		if (hasCollection(db, "exam")) {
			db.getCollection("exam").updateMany(new Document(), Updates.rename("cardIds", "questionIds"));
		}
	}

	private boolean hasCollection(MongoDatabase db, String name) {
		return db.listCollectionNames().into(new ArrayList<>()).contains(name);
	}
}
