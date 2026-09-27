package com.spacequest.om.model;
import java.util.*;

public class GameSession {
    public int currentPlayerIndex = 0;
    public List<Player> players = new ArrayList<>();
    public Cell[][] board = new Cell[12][12];
    public int fixedNodes = 0;
    public int kzCount = 0;
    public String gameLog = "Выберите тему и количество игроков для начала.";
    
    // === НОВЫЕ ПОЛЯ ДЛЯ НАСТРОЙКИ ИГРЫ ===
    public String topic = "electronics"; // electronics, cpp, python
    public int playerCount = 10;        // от 1 до 10
    public boolean gameStarted = false; // флаг, началась ли игра
    
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
    
    // Флаги концовок
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
    
    public boolean showEnding = false;
    public String endingTitle = "";
    public String endingText = "";
    public String endingType = "";

    // Метод инициализации с параметрами
    public void init(String selectedTopic, int selectedPlayerCount) {
        this.topic = selectedTopic;
        this.playerCount = Math.max(1, Math.min(10, selectedPlayerCount));
        this.gameStarted = true;
        
        // Сброс состояния
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
        // Создаем только выбранное количество игроков
        for (int i = 0; i < this.playerCount; i++) {
            Player p = new Player("Игрок " + (i + 1), roles[i % roles.length], colors[i % colors.length]);
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
        gameLog = "🚀 Игра началась! Тема: " + getTopicName(topic) + ", Игроков: " + this.playerCount;
    }
    
    private String getTopicName(String t) {
        switch(t) {
            case "cpp": return "C++ Синтаксис";
            case "python": return "Python Синтаксис";
            default: return "Электроника и Пайка";
        }
    }
    
    private void initPuzzleDeck() {
        List<PuzzleCard> allPuzzles = new ArrayList<>();
        
        // === ВОПРОСЫ ПО ЭЛЕКТРОНИКЕ (Оригинальные) ===
        if (topic.equals("electronics")) {
            allPuzzles.addAll(Arrays.asList(
                new PuzzleCard("Источник 24V, резистор 480 Ом. Какой ток (в мА)?", "50", Arrays.asList("20", "50", "100", "200"), "I = U/R = 24/480 = 0.05 А = 50 мА.", 120),
                new PuzzleCard("Полосы: КОРИЧНЕВЫЙ-ЧЕРНЫЙ-КРАСНЫЙ-ЗОЛОТОЙ. Сопротивление?", "1000 Ом", Arrays.asList("100 Ом", "1000 Ом", "10000 Ом", "470 Ом"), "10 × 100 = 1000 Ом.", 120),
                new PuzzleCard("A=1, B=0 → AND → NOT. Выход?", "1", Arrays.asList("0", "1", "Неопределено", "Ошибка"), "AND(1,0)=0, NOT(0)=1.", 120),
                new PuzzleCard("R1=6, R2=3, R3=2 параллельно. Общее R?", "1 Ом", Arrays.asList("1 Ом", "3 Ом", "11 Ом", "0.5 Ом"), "1/R = 1/6+1/3+1/2 = 1. R=1.", 120),
                new PuzzleCard("173 в двоичном коде (8 бит)?", "10101101", Arrays.asList("10101101", "11010101", "10110101", "10101011"), "128+32+8+4+1 = 173.", 120),
                new PuzzleCard("C=100мкФ, R=10кОм. Постоянная τ?", "1 секунда", Arrays.asList("0.1 с", "1 с", "10 с", "100 с"), "τ = RC = 10000×0.0001 = 1с.", 120),
                new PuzzleCard("Iк=100мА, hFE=50. Ток базы?", "2 мА", Arrays.asList("0.5 мА", "2 мА", "5 мА", "50 мА"), "Iб = Iк/hFE = 100/50 = 2мА.", 120),
                new PuzzleCard("C1=20мкФ, C2=30мкФ последовательно. Общая C?", "12 мкФ", Arrays.asList("12 мкФ", "25 мкФ", "50 мкФ", "6 мкФ"), "C = (20×30)/(20+30) = 12мкФ.", 120),
                new PuzzleCard("R=100 Ом, I=0.2 А. Мощность?", "4 Вт", Arrays.asList("2 Вт", "4 Вт", "20 Вт", "0.2 Вт"), "P = I²R = 0.04×100 = 4Вт.", 120),
                new PuzzleCard("Делитель: R1=6к, R2=3к, U=9V. U на R2?", "3V", Arrays.asList("3V", "6V", "9V", "1.5V"), "U2 = 9×3/(6+3) = 3V.", 120)
            ));
        }
        // === ВОПРОСЫ ПО C++ ===
        else if (topic.equals("cpp")) {
            allPuzzles.addAll(Arrays.asList(
                // === БАЗОВЫЕ ПОНЯТИЯ И СИНТАКСИС ===
                new PuzzleCard("Как называется текст программы, написанный программистом?", "Исходный код", Arrays.asList("Машинный код", "Исходный код", "Бинарный файл", "Скрипт"), "Программисты пишут исходный код, чтобы объяснить компьютеру, что делать.", 120),
                new PuzzleCard("Из чего состоит машинный код, понятный процессору?", "Нули и единицы", Arrays.asList("Латинские буквы", "Нули и единицы", "Шестнадцатеричные числа", "Слова английского языка"), "Машинный код — это последовательность нулей и единиц.", 120),
                new PuzzleCard("Какое расширение обычно имеют исполняемые файлы в Windows?", ".exe", Arrays.asList(".txt", ".cpp", ".exe", ".doc"), "Исполняемые программы в Windows имеют окончание .exe.", 120),
                new PuzzleCard("Какая директива подключает библиотеку ввода-вывода?", "#include <iostream>", Arrays.asList("#include <stdio.h>", "#include <iostream>", "using namespace std;", "import io;"), "Для использования cout нужно подключить <iostream>.", 120),
                new PuzzleCard("Какой объект используется для вывода текста в консоль?", "std::cout", Arrays.asList("std::cin", "std::out", "std::cout", "print"), "cout означает console output.", 120),

                // === ВЫВОД И КОММЕНТАРИИ ===
                new PuzzleCard("Что делает манипулятор std::endl?", "Перенос строки + очистка буфера", Arrays.asList("Только перенос строки", "Перенос строки + очистка буфера", "Ставит точку", "Завершает программу"), "endl добавляет символ новой строки и сбрасывает буфер.", 120),
                new PuzzleCard("Как обозначается однострочный комментарий в C++?", "//", Arrays.asList("/*", "#", "//", "--"), "Две косые черты // делают остаток строки комментарием.", 120),
                new PuzzleCard("Как начать многострочный комментарий?", "/*", Arrays.asList("//", "/*", "#", "<!--"), "Многострочный комментарий начинается с /* и заканчивается */.", 120),
                new PuzzleCard("Что выведет: cout << \"Hi\" << \" \" << \"World\";", "Hi World", Arrays.asList("HiWorld", "Hi World", "Ошибка", "Hi"), "Оператор << позволяет конкатенировать вывод.", 120),

                // === ТИПЫ ДАННЫХ (ПРОСТЫЕ) ===
                new PuzzleCard("Какой тип данных предназначен для хранения целых чисел?", "int", Arrays.asList("float", "int", "char", "bool"), "int происходит от integer (целый).", 120),
                new PuzzleCard("Какой тип данных используется для коротких дробных чисел?", "float", Arrays.asList("int", "float", "double", "string"), "float имеет точность до 6 цифр.", 120),
                new PuzzleCard("Сколько значений может принимать переменная типа bool?", "2", Arrays.asList("1", "2", "10", "256"), "bool принимает только true или false.", 120),
                new PuzzleCard("Какой тип данных хранит один символ?", "char", Arrays.asList("string", "text", "char", "symbol"), "char происходит от character (символ).", 120),
                new PuzzleCard("Какую библиотеку нужно подключить для работы со string?", "<string>", Arrays.asList("<iostream>", "<string>", "<cmath>", "<vector>"), "std::string не является встроенным типом и требует <string>.", 120),

                // === ПЕРЕМЕННЫЕ И ИМЕНОВАНИЕ ===
                new PuzzleCard("Чем отличается \\n от std::endl?", "endl очищает буфер", Arrays.asList("Ничем", "endl очищает буфер", "\\n быстрее", "\\n ставит пробел"), "endl добавляет перенос и сбрасывает буфер вывода.", 120),
                new PuzzleCard("Может ли имя переменной начинаться с цифры?", "Нет", Arrays.asList("Да", "Нет", "Только если это int", "Только в комментариях"), "Имя переменной не может начинаться с цифры.", 120),
                new PuzzleCard("Что содержится в неинициализированной переменной?", "Мусор", Arrays.asList("Ноль", "Пустота", "Мусор", "Единица"), "Без инициализации переменная содержит случайное значение (мусор).", 120),
                new PuzzleCard("Какой символ обязательно ставится в конце каждой инструкции?", ";", Arrays.asList(".", ",", ":", ";"), "Точка с запятой завершает инструкцию в C++.", 120),

                // === СЛОЖНЫЕ ВОПРОСЫ (СИНТАКСИС И ЛОГИКА) ===
                new PuzzleCard("Что выведет: int a = 5; cout << ++a;", "6", Arrays.asList("5", "6", "4", "Ошибка"), "Префиксный инкремент ++a сначала увеличивает, потом использует.", 120),
                new PuzzleCard("Что выведет: int a = 5; cout << a++;", "5", Arrays.asList("5", "6", "4", "Ошибка"), "Постфиксный инкремент b++ сначала использует, потом увеличивает.", 120),
                new PuzzleCard("Какой тип данных займет больше всего памяти?", "double", Arrays.asList("char", "int", "float", "double"), "double (8 байт) больше чем float (4 байта) и int (обычно 4 байта).", 120),
                new PuzzleCard("Что такое 'raw string literal' в C++?", "Строка с R\"(...\"", Arrays.asList("Строка с R\"(...\"", "Обычная строка", "Массив символов", "Комментарий"), "Raw строки R\"(...\" позволяют использовать спецсимволы без экранирования.", 120),
                new PuzzleCard("Как правильно объявить переменную string name?", "string name;", Arrays.asList("str name;", "string name;", "text name;", "char name[];"), "Для строк используется тип std::string.", 120),
                new PuzzleCard("Что произойдет при делении int на int (5/2)?", "2", Arrays.asList("2.5", "2", "3", "Ошибка"), "Целочисленное деление отбрасывает дробную часть.", 120),
                new PuzzleCard("Какое значение у bool(false) в числовом виде?", "0", Arrays.asList("1", "0", "-1", "null"), "false приводится к 0, true к 1.", 120),
                new PuzzleCard("Что выведет: cout << (int)'A';", "65", Arrays.asList("A", "65", "97", "Ошибка"), "Символ 'A' имеет ASCII код 65.", 120),
                new PuzzleCard("Как правильно записать число 1000000 в коде для читаемости?", "1'000'000", Arrays.asList("1_000_000", "1'000'000", "1,000,000", "1 000 000"), "В современном C++ можно использовать апостроф как разделитель.", 120),
                new PuzzleCard("Что такое 'using namespace std;'?", "Подключение пространства имен", Arrays.asList("Библиотека", "Подключение пространства имен", "Функция", "Переменная"), "Позволяет писать cout вместо std::cout.", 120)
            ));
        }
        
        // === ВОПРОСЫ ПО PYTHON ===
        else if (topic.equals("python")) {
            allPuzzles.addAll(Arrays.asList(
                new PuzzleCard("print(type([])) выведет?", "<class 'list'>", Arrays.asList("<class 'tuple'>", "<class 'list'>", "<class 'array'>", "list"), "[] — это список.", 120),
                new PuzzleCard("a = [1,2,3]; a[1:] = ?", "[2, 3]", Arrays.asList("[1, 2]", "[2, 3]", "[3]", "[1]"), "Срез с индекса 1 до конца.", 120),
                new PuzzleCard("Как проверить тип переменной?", "type(x)", Arrays.asList("typeof(x)", "type(x)", "x.type()", "isinstance(x)"), "type() возвращает тип.", 120),
                new PuzzleCard("print(2 ** 3) выведет?", "8", Arrays.asList("6", "8", "9", "5"), "** — возведение в степень.", 120),
                new PuzzleCard("Что вернет len('Hello')?", "5", Arrays.asList("4", "5", "6", "Ошибка"), "5 символов.", 120),
                new PuzzleCard("Как добавить элемент в список?", "append()", Arrays.asList("add()", "append()", "push()", "insert()"), "append() добавляет в конец.", 120),
                new PuzzleCard("x = {}; type(x) = ?", "dict", Arrays.asList("set", "dict", "list", "tuple"), "{} — пустой словарь.", 120),
                new PuzzleCard("print('Hi' * 3) выведет?", "HiHiHi", Arrays.asList("Hi3", "Hi Hi Hi", "HiHiHi", "Ошибка"), "* повторяет строку.", 120),
                new PuzzleCard("Как получить ключи словаря?", ".keys()", Arrays.asList(".keys()", ".getKeys()", ".items()", ".values()"), ".keys() возвращает ключи.", 120),
                new PuzzleCard("bool('False') = ?", "True", Arrays.asList("True", "False", "None", "Ошибка"), "Непустая строка всегда True.", 120)
            ));
        }
        
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
