package com.spacequest.om.model;
import java.util.List;

public class PuzzleCard {
    public String question;
    public String correctAnswer;
    public List<String> answerOptions; // null => вопрос с самостоятельным вводом ответа
    public String explanation;
    public int timeSeconds;

    public PuzzleCard(String q, String correct, List<String> options, String expl, int t) {
        this.question = q;
        this.correctAnswer = correct;
        this.answerOptions = options;
        this.explanation = expl;
        this.timeSeconds = t;
    }

    /** Конструктор для вопросов с открытым ответом (ввод с клавиатуры). */
    public PuzzleCard(String q, String correct, String expl, int t) {
        this(q, correct, null, expl, t);
    }

    public boolean isOpenEnded() {
        return answerOptions == null || answerOptions.isEmpty();
    }

    /** Нормализация ответа: регистр, лишние пробелы, варианти написания. */
    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim()
                .replaceAll("\\s+", " ")
                .replace("'", "")
                .replace("´", "")
                .replace("`", "");
    }

    /**
     * Проверка ответа на вопрос с самостоятельным вводом.
     * Правильным считается ответ, точно совпадающий с эталоном
     * или содержащий все ключевые слова эталонного ответа (например,
     * "иначе if" засчитывается как верный ответ на "if-else").
     */
    public boolean checkOpenAnswer(String userAnswer) {
        String user = collapse(normalize(userAnswer));
        if (user.isEmpty()) return false;
        String ref = normalize(correctAnswer);
        if (user.equals(collapse(ref))) return true;
        // Эталон может содержать альтернативы через "|": достаточно совпадения
        // с любой из них. Внутри альтернативы части через ";" должны
        // присутствовать в ответе все (с учётом возможных пробелов у игрока).
        for (String altRaw : ref.split("\\|")) {
            String alt = altRaw.trim();
            if (alt.isEmpty()) continue;
            boolean allPartsFound = true;
            for (String part : alt.split("[;]")) {
                String p = collapse(part);
                if (!p.isEmpty() && !user.contains(p)) { allPartsFound = false; break; }
            }
            if (allPartsFound) return true;
        }
        return false;
    }

    /** Убирает все пробелы (для толерантной проверки ответов вида «temperature < 0»). */
    private static String collapse(String s) {
        return s.replaceAll(" ", "");
    }
}
