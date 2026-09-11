package com.spacequest.om.model;
import java.util.*;

public class GameSession {
    public int currentPlayerIndex = 0;
    public List<Player> players = new ArrayList<>();
    public Cell[][] board = new Cell[12][12];
    public int fixedNodes = 0;
    public int kzCount = 0;
    public String gameLog = "Игра началась. Экипаж в Шлюзе (0,0).";
    
    public Deque<PuzzleCard> puzzleDeck = new ArrayDeque<>();
    public PuzzleCard currentPuzzle;
    
    public PuzzleCard analystFirstCard;
    public PuzzleCard analystSecondCard;
    public boolean analystChoosing = false;
    
    public String toolHint = null;
    public boolean showToolHint = false;
    
    public boolean isPuzzleActive = false;
    public boolean showResult = false;
    
    public String lastAnswer;
    public boolean lastAnswerCorrect;
    public String lastExplanation;
    
    public int turnsSinceLastKZ = 0;
    public int totalTurns = 0;
    
    public Deque<String> toolDeck = new ArrayDeque<>();
    public boolean skipThreatPhase = false;
    
    public VotingSituation currentVoting;
    public boolean isVotingActive = false;
    public Map<Integer, Integer> votes = new HashMap<>();
    
    public Map<String, Integer> activeChains = new HashMap<>();
    public Map<String, String> chainChoices = new HashMap<>();
    public Map<String, String> pendingChains = new HashMap<>();
    public List<String> votingHistory = new ArrayList<>();
    
    // === ФЛАГИ КОНЦОВОК ===
    public boolean alienContact = false;
    public boolean alienFriendly = false;
    public boolean alienAngry = false;
    public boolean planetDiscovered = false;
    public boolean pirateMode = false;
    public boolean aiMerged = false;
    public boolean captainSacrifice = false;
    public boolean escapePods = false;
    public boolean earthContact = false;
    public boolean earthAlliance = false;
    public boolean backdoorActive = false;
    public boolean anomalyExplored = false;
    public boolean portalEntered = false;
    
    // === ПОКАЗАТЬ КОНЦОВКУ ===
    public boolean showEnding = false;
    public String endingTitle = "";
    public String endingText = "";
    public String endingType = ""; // GOOD, NEUTRAL, BAD

    public void init() {
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                board[x][y] = new Cell(x, y, CellType.CORRIDOR);
            }
        }
        board[5][5].setType(CellType.CORE); board[5][6].setType(CellType.CORE);
        board[6][5].setType(CellType.CORE); board[6][6].setType(CellType.CORE);

        int nodesPlaced = 0;
        Random rnd = new Random();
        while(nodesPlaced < 9) {
            int x = rnd.nextInt(12), y = rnd.nextInt(12);
            if(board[x][y].type == CellType.CORRIDOR) {
                board[x][y].setType(CellType.NODE);
                nodesPlaced++;
            }
        }

        String[] roles = {"Главный Инженер", "Схемотехник", "Навигатор", "Программист", "Электрик", 
                          "Механик", "Аналитик", "Связист", "Техник безопасности", "Стажер"};
        String[] colors = {"#FF5252", "#448AFF", "#69F0AE", "#FFD740", "#E040FB", 
                           "#18FFFF", "#FF6E40", "#B9F6CA", "#FF4081", "#FFFFFF"};
        
        players.clear();
        for (int i = 0; i < 10; i++) {
            Player p = new Player("Игрок " + (i + 1), roles[i], colors[i]);
            p.x = 0; p.y = 0;
            players.add(p);
        }
        
        for (Player p : players) {
            board[0][0].playerColors.add(p.color);
        }
        
        fixedNodes = 0;
        kzCount = 0;
        currentPlayerIndex = 0;
        totalTurns = 0;
        isPuzzleActive = false;
        showResult = false;
        turnsSinceLastKZ = 0;
        lastAnswer = null;
        lastExplanation = null;
        skipThreatPhase = false;
        analystChoosing = false;
        showToolHint = false;
        toolHint = null;
        isVotingActive = false;
        currentVoting = null;
        votes.clear();
        activeChains.clear();
        chainChoices.clear();
        pendingChains.clear();
        votingHistory.clear();
        
        // Сброс флагов концовок
        alienContact = false; alienFriendly = false; alienAngry = false;
        planetDiscovered = false; pirateMode = false; aiMerged = false;
        captainSacrifice = false; escapePods = false; earthContact = false;
        earthAlliance = false; backdoorActive = false; anomalyExplored = false;
        portalEntered = false; showEnding = false;
        
        initPuzzleDeck();
        initToolDeck();
        gameLog = "🚀 Игра перезапущена. Сюжетные цепочки активированы.";
    }
    
    private void initPuzzleDeck() {
        List<PuzzleCard> allPuzzles = new ArrayList<>(Arrays.asList(
            new PuzzleCard("Источник 24V, резистор 480 Ом. Какой ток (в мА)?", "50", Arrays.asList("20", "50", "100", "200"), "По закону Ома: I = U/R = 24/480 = 0.05 А = 50 мА.", 120),
            new PuzzleCard("Полосы: КОРИЧНЕВЫЙ-ЧЕРНЫЙ-КРАСНЫЙ-ЗОЛОТОЙ. Сопротивление?", "1000 Ом", Arrays.asList("100 Ом", "1000 Ом", "10000 Ом", "470 Ом"), "Коричневый=1, Черный=0, Красный=×100. Получаем 10×100 = 1000 Ом.", 120),
            new PuzzleCard("A и B → AND → NOT. A=1, B=0. Выход?", "1", Arrays.asList("0", "1", "Неопределено", "Ошибка"), "AND(1,0) = 0. NOT(0) = 1.", 120),
            new PuzzleCard("R1=6 Ом, R2=3 Ом, R3=2 Ом параллельно. Общее сопротивление?", "1 Ом", Arrays.asList("1 Ом", "3 Ом", "11 Ом", "0.5 Ом"), "1/R = 1/6 + 1/3 + 1/2 = 1. Значит R = 1 Ом.", 120),
            new PuzzleCard("Перевести число 173 в двоичный код (8 бит)", "10101101", Arrays.asList("10101101", "11010101", "10110101", "10101011"), "173 = 128 + 32 + 8 + 4 + 1.", 120),
            new PuzzleCard("C=100 мкФ, R=10 кОм. Постоянная времени τ?", "1 секунда", Arrays.asList("0.1 секунды", "1 секунда", "10 секунд", "100 секунд"), "τ = R×C = 10000 × 0.0001 = 1 секунда.", 120),
            new PuzzleCard("Iк=100 мА, hFE=50. Ток базы?", "2 мА", Arrays.asList("0.5 мА", "2 мА", "5 мА", "50 мА"), "Iб = Iк/hFE = 100/50 = 2 мА.", 120),
            new PuzzleCard("C1=20 мкФ, C2=30 мкФ последовательно. Общая емкость?", "12 мкФ", Arrays.asList("12 мкФ", "25 мкФ", "50 мкФ", "6 мкФ"), "1/C = 1/20 + 1/30 = 5/60. C = 12 мкФ.", 120),
            new PuzzleCard("R=100 Ом, I=0.2 А. Мощность?", "4 Вт", Arrays.asList("2 Вт", "4 Вт", "20 Вт", "0.2 Вт"), "P = I²×R = 0.04 × 100 = 4 Вт.", 120),
            new PuzzleCard("R1=6 кОм, R2=3 кОм, U=9V. Напряжение на R2?", "3V", Arrays.asList("3V", "6V", "9V", "1.5V"), "U2 = U × R2/(R1+R2) = 9 × 3/9 = 3V.", 120),
            new PuzzleCard("2A в десятичной системе?", "42", Arrays.asList("20", "42", "28", "32"), "2A (hex) = 2×16 + 10 = 42.", 120),
            new PuzzleCard("R=3 Ом, X=4 Ом. Импеданс Z?", "5 Ом", Arrays.asList("1 Ом", "5 Ом", "7 Ом", "12 Ом"), "Z = √(R² + X²) = √(9 + 16) = 5 Ом.", 120),
            new PuzzleCard("1101 в десятичной?", "13", Arrays.asList("11", "13", "15", "9"), "1101 (bin) = 8 + 4 + 0 + 1 = 13.", 120),
            new PuzzleCard("Куда подключать плюс светодиода?", "К длинной", Arrays.asList("К длинной", "К короткой", "Не важно", "К обеим"), "Длинная ножка — анод (+).", 120),
            new PuzzleCard("3 батарейки по 1.5V последовательно. Общее напряжение?", "4.5V", Arrays.asList("1.5V", "3.0V", "4.5V", "6.0V"), "1.5 + 1.5 + 1.5 = 4.5V.", 120),
            new PuzzleCard("Какой цвет провода — плюс?", "Красный=+, Черный=-", Arrays.asList("Красный=+, Черный=-", "Красный=-, Черный=+", "Оба одинаковые", "Зависит от устройства"), "Красный — плюс (+), черный — минус (-).", 120),
            new PuzzleCard("Система потребляет 2А. Какой предохранитель?", "3А", Arrays.asList("1А", "3А", "10А", "Любой"), "Предохранитель должен быть больше рабочего тока.", 120),
            new PuzzleCard("1010 в десятичной?", "10", Arrays.asList("8", "10", "12", "2"), "1010 (bin) = 8 + 0 + 2 + 0 = 10.", 120),
            new PuzzleCard("A=1, B=0, параллельно (ИЛИ). Откроется?", "Да", Arrays.asList("Да", "Нет", "Неопределено", "Ошибка"), "Логика ИЛИ: если хотя бы один = 1, результат = 1.", 120),
            new PuzzleCard("Какой символ обозначает конденсатор?", "Две линии", Arrays.asList("Зигзаг", "Две линии", "Круг", "Треугольник"), "Две параллельные линии (обкладки).", 120),
            new PuzzleCard("U=10V, R=5 Ом. Ток?", "2А", Arrays.asList("0.5А", "2А", "5А", "50А"), "I = U/R = 10/5 = 2А.", 120),
            new PuzzleCard("Провода отошли. Что нужно?", "Паяльник", Arrays.asList("Пассатижи", "Паяльник", "Отвертка", "Молоток"), "Для пайки используется паяльник.", 120),
            new PuzzleCard("Нужно замкнуть цепь. Что использовать?", "Скрепку", Arrays.asList("Ручку", "Изоленту", "Скрепку", "Бумагу"), "Скрепка металлическая и проводит ток.", 120),
            new PuzzleCard("RS-триггер: S=1, R=0. Состояние Q?", "Q=1", Arrays.asList("Q=0", "Q=1", "Неопределено", "Ошибка"), "S=1 устанавливает Q=1.", 120),
            new PuzzleCard("Найти неправильно ориентированный диод.", "D4", Arrays.asList("D1", "D2", "D3", "D4"), "D4 нарушает последовательность.", 120),
            new PuzzleCard("L=1 мГн, C=1 мкФ. Резонансная частота?", "~5 кГц", Arrays.asList("~1 кГц", "~5 кГц", "~10 кГц", "~50 кГц"), "f = 1/(2π√(LC)) ≈ 5 кГц.", 120),
            new PuzzleCard("Rос=100 кОм, Rвх=10 кОм. Коэффициент усиления?", "-10", Arrays.asList("10", "-10", "100", "0.1"), "K = -Rос/Rвх = -10.", 120),
            new PuzzleCard("Uст=5.1V, R=470 Ом, Uпит=12V. Ток?", "~14.7 мА", Arrays.asList("~5 мА", "~14.7 мА", "~25 мА", "~100 мА"), "I = (12-5.1)/470 ≈ 14.7 мА.", 120),
            new PuzzleCard("Rтерм=3 кОм, R=10 кОм, U=5V. Напряжение на термисторе?", "~1.15V", Arrays.asList("~0.5V", "~1.15V", "~2.5V", "~3.5V"), "U = 5 × 3/13 ≈ 1.15V.", 120),
            new PuzzleCard("f=1 кГц, duty=25%. Длительность импульса?", "250 мкс", Arrays.asList("100 мкс", "250 мкс", "500 мкс", "1000 мкс"), "T = 1000 мкс. 1000 × 0.25 = 250 мкс.", 120)
        ));
        Collections.shuffle(allPuzzles);
        puzzleDeck = new ArrayDeque<>(allPuzzles);
    }
    
    private void initToolDeck() {
        List<String> allTools = Arrays.asList(
            "Мультиметр", "Оловоотсос", "Флюс", "Паяльная станция", "Термоусадка",
            "Осциллограф", "Антистатический браслет", "Тестер цепей", "Экстренный ремонт",
            "Резервное питание", "Wi-Fi модуль", "Аварийный генератор"
        );
        Collections.shuffle(allTools);
        toolDeck = new ArrayDeque<>(allTools);
    }
}
