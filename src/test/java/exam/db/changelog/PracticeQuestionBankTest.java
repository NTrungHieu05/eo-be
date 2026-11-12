package exam.db.changelog;

import exam.db.entity.Answer;
import exam.db.entity.Question;
import exam.db.entity.QuestionContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PracticeQuestionBankTest {

	@Test
	void threePracticeExamsUseDistinctOfficialCounts() {
		PracticeQuestionBank bank = PracticeQuestionBank.load();
		bank.validate();

		assertThat(bank.parentTexts(0)).hasSize(103);
		assertThat(bank.parentTexts(0)).doesNotContainAnyElementsOf(bank.parentTexts(1));
		assertThat(bank.parentTexts(1)).doesNotContainAnyElementsOf(bank.parentTexts(2));
		assertThat(bank.parentTexts(0).get(54)).doesNotContain("submit the report");
		assertThat(bank.parentTexts(0).get(6)).doesNotContain("When will the meeting start?");
	}

	@Test
	void applyKeepsThePartShapeAndPointsEachExamAtItsOwnAudio() {
		PracticeQuestionBank bank = PracticeQuestionBank.load();
		List<Question> exam1 = skeleton();
		bank.apply(exam1, 0);

		assertThat(exam1.get(0).getQuestion().getText()).isEqualTo("What is on the desk?");
		assertThat(exam1.get(0).getQuestion().getImage()).isEqualTo("/data/images/part1-1.png");
		assertThat(exam1.get(0).getQuestion().getSound()).isEqualTo("/data/sounds/part1-1.wav");
		assertThat(exam1.get(0).getAnswer().getTexts()).containsExactly("A lamp and a sheet of paper are on the desk.");
		assertThat(exam1.get(0).getAnswer().getChoices()).doesNotContain("A lamp and a sheet of paper are on the desk.");
		assertThat(exam1.get(0).getAnswer().getHint()).isEqualTo("A");

		assertThat(exam1.get(6).getQuestion().getSound()).isEqualTo("/data/sounds/part2-1.wav");
		assertThat(exam1.get(6).getAnswer().getChoices()).hasSize(2);
		assertThat(exam1.get(31).getChildQuestions()).hasSize(3);
		assertThat(exam1.get(31).getQuestion().getSound()).isEqualTo("/data/sounds/part3-1.wav");
		assertThat(exam1.get(54).getAnswer().getTexts()).containsExactly("on");
		assertThat(exam1.get(88).getChildQuestions()).hasSize(2);
		assertThat(exam1.get(96).getChildQuestions()).hasSize(4);
		assertThat(exam1.get(98).getChildQuestions()).hasSize(5);
		assertThat(exam1.get(102).getChildQuestions()).hasSize(5);
		assertThat(exam1.get(54).getQuestion().getSound()).isNull();

		List<Question> exam2 = skeleton();
		bank.apply(exam2, 1);
		assertThat(exam2.get(0).getQuestion().getText()).isNotEqualTo(exam1.get(0).getQuestion().getText());
		assertThat(exam2.get(0).getQuestion().getImage()).isEqualTo("/data/images/part1-1.png");
		assertThat(exam2.get(0).getQuestion().getSound()).isEqualTo("/data/sounds/exam2/part1-1.wav");
		assertThat(exam2.get(30).getQuestion().getSound()).isEqualTo("/data/sounds/exam2/part2-25.wav");
	}

	private List<Question> skeleton() {
		List<Question> questions = new ArrayList<>();
		addSingles(questions, 6);
		addSingles(questions, 25);
		addGroups(questions, 13, 3);
		addGroups(questions, 10, 3);
		addSingles(questions, 30);
		addGroups(questions, 4, 4);
		for (int count : PracticeQuestionBank.PART7_SINGLE_COUNTS) {
			questions.add(grouped(count));
		}
		addGroups(questions, 2, 5);
		addGroups(questions, 3, 5);
		return questions;
	}

	private void addSingles(List<Question> questions, int count) {
		for (int i = 0; i < count; i++) {
			questions.add(single());
		}
	}

	private void addGroups(List<Question> questions, int count, int children) {
		for (int i = 0; i < count; i++) {
			questions.add(grouped(children));
		}
	}

	private Question single() {
		Question question = new Question();
		question.setQuestion(new QuestionContent());
		question.setAnswer(new Answer());
		question.setIsQuestionGroup(false);
		question.setChildQuestions(new ArrayList<>());
		return question;
	}

	private Question grouped(int children) {
		Question question = single();
		question.setIsQuestionGroup(true);
		List<Question> childQuestions = new ArrayList<>();
		for (int i = 0; i < children; i++) {
			Question child = new Question();
			child.setQuestion(new QuestionContent());
			child.setAnswer(new Answer());
			childQuestions.add(child);
		}
		question.setChildQuestions(childQuestions);
		return question;
	}
}
