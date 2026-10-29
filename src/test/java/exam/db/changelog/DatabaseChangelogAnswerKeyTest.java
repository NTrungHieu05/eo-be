package exam.db.changelog;

import exam.db.entity.Answer;
import exam.db.entity.Card;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseChangelogAnswerKeyTest {

	@Test
	void repairMovesTheSentenceAtTheLetterIndexIntoTheCorrectAnswer() {
		Answer answer = new Answer();
		answer.setChoices(Arrays.asList(
				"He is sitting at a desk.",
				"He is running in a park.",
				"He is cooking in a kitchen.",
				"He is driving a truck."));
		answer.setTexts(Collections.singletonList("A"));
		answer.setHint("A");

		assertThat(DatabaseChangelogAnswerKey.repairAnswer(answer)).isTrue();

		assertThat(answer.getTexts()).containsExactly("He is sitting at a desk.");
		assertThat(answer.getChoices()).containsExactly(
				"He is running in a park.",
				"He is cooking in a kitchen.",
				"He is driving a truck.");
		assertThat(answer.getHint()).isEqualTo("A");
	}

	@Test
	void repairSkipsAnswersThatAlreadyStoreTheSentence() {
		Answer answer = new Answer();
		answer.setChoices(Arrays.asList("He is running in a park.", "He is cooking in a kitchen."));
		answer.setTexts(Collections.singletonList("He is sitting at a desk."));

		assertThat(DatabaseChangelogAnswerKey.repairAnswer(answer)).isFalse();
		assertThat(answer.getTexts()).containsExactly("He is sitting at a desk.");
	}

	@Test
	void repairAlsoFixesChildCards() {
		Answer childAnswer = new Answer();
		childAnswer.setChoices(Arrays.asList("At the office.", "Tomorrow morning.", "Yes, she did."));
		childAnswer.setTexts(Collections.singletonList("(B)"));
		Card child = new Card();
		child.setAnswer(childAnswer);
		Card parent = new Card();
		parent.setChildCards(Collections.singletonList(child));

		assertThat(DatabaseChangelogAnswerKey.repairCard(parent)).isTrue();
		assertThat(childAnswer.getTexts()).containsExactly("Tomorrow morning.");
		assertThat(childAnswer.getChoices()).containsExactly("At the office.", "Yes, she did.");
		assertThat(childAnswer.getHint()).isEqualTo("B");
	}
}
