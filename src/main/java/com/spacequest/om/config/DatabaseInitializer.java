package com.spacequest.om.config;

import com.spacequest.om.entity.SituationEntity;
import com.spacequest.om.repository.SituationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    private final SituationRepository repository;
    
    public DatabaseInitializer(SituationRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("=================================================================");
        System.out.println("📚 ЗАПОЛНЕНИЕ БД ДЛИННЫМИ СЮЖЕТНЫМИ ЛИНИЯМИ...");
        
        try {
            // === ОДИНОЧНЫЕ СИТУАЦИИ (много для разнообразия) ===
            save("solo1", "Черная дыра приближается", "Корабль засосывает в гравитационное поле.", Arrays.asList("Полный вперед", "Обходной маневр", "Гравитационная рогатка"), Arrays.asList("50% +2 клетки / 50% +2 КЗ", "Все -1 клетка", "Навигатор +3, остальные -1"), Arrays.asList("RUSH50", "BYPASS1", "SLING"), null, 0, null, 0, null);
            save("solo2", "Столкновение неизбежно!", "На пути обломок корабля.", Arrays.asList("Таранить", "Торможение", "Маневр"), Arrays.asList("+1 КЗ", "Все -2 клетки", "Если Навигатор: успех"), Arrays.asList("RAM1", "BRAKE2", "EVADE"), null, 0, null, 0, null);
            save("solo3", "Два игрока в опасности", "Два игрока в ловушке КЗ.", Arrays.asList("Спасти 1", "Спасти 2", "Спасти обоих"), Arrays.asList("Игрок 2: ожог", "Игрок 1: ожог", "50% оба спасены"), Arrays.asList("SAVE1", "SAVE2", "SAVEBOTH50"), null, 0, null, 0, null);
            save("solo4", "Перегрузка реактора", "Реактор на пределе.", Arrays.asList("Снизить мощность", "В щиты", "Максимум"), Arrays.asList("Все -1 клетка, нет КЗ 3 хода", "Щит +5, +1 КЗ", "Все +1 клетка, 50% +2 КЗ"), Arrays.asList("LOW3", "SHIELD5", "MAX50"), null, 0, null, 0, null);
            save("solo5", "Сбой жизнеобеспечения", "Кислорода на 5 ходов.", Arrays.asList("Починить", "Распределить", "Игнорировать"), Arrays.asList("Один пропускает ход", "Все -1 клетка", "Через 5 ходов все: ожог"), Arrays.asList("FIX1", "DISTR1", "IGNORE5BURN"), null, 0, null, 0, null);
            save("solo6", "Вирус мутирует!", "КЗ двигаются на 2 клетки.", Arrays.asList("Антивирус", "Адаптироваться", "Ускорить починку"), Arrays.asList("Тратим 2 предмета", "Принимаем, +1 предмет", "Узлы как 1.5"), Arrays.asList("ANTIVIRUS2", "ADAPT1", "ACCEL15"), null, 0, null, 0, null);
            save("solo7", "Резервный генератор", "Нашли генератор.", Arrays.asList("Доставить", "Разобрать", "Оставить"), Arrays.asList("3 игрока тратят ходы, щит +3", "Получаем 3 предмета", "Ничего"), Arrays.asList("DELIVER3", "DISASSEMBLE3", "LEAVE"), null, 0, null, 0, null);
            save("solo8", "Ошибка навигации", "Компьютер ошибся.", Arrays.asList("Довериться", "Пересчитать", "Резерв"), Arrays.asList("50% бонус / 50% -2 клетки", "Тратим 1 ход", "Тратим 1 предмет"), Arrays.asList("COMPUTER50", "MANUAL1", "BACKUP1"), null, 0, null, 0, null);
            save("solo9", "Последний предмет", "Найден мощный предмет.", Arrays.asList("Дать слабому", "Дать сильному", "Уничтожить"), Arrays.asList("Игрок с макс ожогов получает", "Игрок с макс узлами получает", "Никто не получает"), Arrays.asList("GAIN3", "GAIN3", "DEAL3"), null, 0, null, 0, null);
            save("solo10", "КЗ блокируют путь", "Три КЗ заблокировали путь.", Arrays.asList("Прорываться", "Обход", "Оловоотсос"), Arrays.asList("Все получают ожог", "Тратим 3 хода", "Если есть предмет: -1 КЗ"), Arrays.asList("RISK50", "BYPASS2", "KILL2_SPAWN2"), null, 0, null, 0, null);
            
            // === ДЛИННАЯ ЛИНИЯ А: ПРИШЕЛЬЦЫ (5 ситуаций) ===
            save("alien1", "👽 Сигнал бедствия", "Обнаружен поврежденный корабль неизвестной расы. Они просят помощи.", Arrays.asList("Помочь", "Игнорировать", "Атаковать"), Arrays.asList("Контакт установлен", "Безопасно", "Пираты, +3 предмета"), Arrays.asList("ALIEN_HELP", "ALIEN_IGNORE", "ALIEN_ATTACK"), "alien", 1, "alien2", 3, "ALIEN_HELP|ALIEN_ATTACK");
            save("alien2", "👽 Первый контакт", "Пришельцы благодарны. Они предлагают обмен технологиями.", Arrays.asList("Обменяться", "Отказаться", "Потребовать больше"), Arrays.asList("+2 предмета, дружеские отношения", "Ничего", "Пришельцы обижаются"), Arrays.asList("ALIEN_TRADE", "ALIEN_REFUSE", "ALIEN_DEMAND"), "alien", 2, "alien3", 3, null);
            save("alien3", "👽 Приглашение на базу", "Пришельцы приглашают на свою базу для дальнейшего сотрудничества.", Arrays.asList("Высадиться лично", "Отправить зонд (тратит предмет!)", "Отказаться"), Arrays.asList("Риск: 30% ожог, но +3 предмета", "Безопасно: +1 предмет", "Ничего"), Arrays.asList("ALIEN_LAND", "ALIEN_PROBE", "ALIEN_LEAVE"), "alien", 3, "alien4", 4, null);
            save("alien4", "👽 Секреты пришельцев", "На базе пришельцев обнаружена важная информация о вирусе.", Arrays.asList("Изучить данные", "Украсть технологии", "Поделиться с экипажем"), Arrays.asList("+4 предмета, открываем планету", "+5 предметов, но пришельцы злятся", "Все получают бонус"), Arrays.asList("ALIEN_STUDY", "ALIEN_STEAL", "ALIEN_SHARE"), "alien", 4, "alien5", 4, null);
            save("alien5", "👽 Финальное решение", "Пришельцы предлагают союз против вируса.", Arrays.asList("Принять союз", "Отказаться", "Предложить симбиоз"), Arrays.asList("+5 предметов, планета открыта", "Ничего", "Сливаемся с технологиями"), Arrays.asList("ALIEN_ALLY", "ALIEN_BREAK", "ALIEN_MERGE_ALIEN"), "alien", 5, null, 0, null);
            
            // === ДЛИННАЯ ЛИНИЯ Б: АНОМАЛИЯ (4 ситуации) ===
            save("anomaly1", "🌀 Странные показания", "Сканеры фиксируют аномалию в пространстве.", Arrays.asList("Исследовать", "Уничтожить", "Игнорировать"), Arrays.asList("Риск: 2 ожога, узнаем больше", "Безопасно", "Ничего"), Arrays.asList("ANOMALY_EXPLORE", "ANOMALY_DESTROY", "ANOMALY_IGNORE"), "anomaly", 1, "anomaly2", 3, "ANOMALY_EXPLORE");
            save("anomaly2", "🌀 Внутри аномалии", "Обнаружен портал в другое измерение.", Arrays.asList("Войти в портал", "Изучить", "Уничтожить"), Arrays.asList("Риск: 30% не вернуться", "Подсказки ко всем задачам", "Безопасно"), Arrays.asList("ANOMALY_PORTAL", "ANOMALY_STUDY", "ANOMALY_CLOSE"), "anomaly", 2, "anomaly3", 4, null);
            save("anomaly3", "🌀 По ту сторону", "За порталом обнаружена планета с ресурсами.", Arrays.asList("Основать колонию", "Добыть ресурсы", "Уничтожить"), Arrays.asList("Планета открыта!", "+6 предметов", "Безопасно"), Arrays.asList("ANOMALY_COLONIZE", "ANOMALY_MINE", "ANOMALY_DESTROY_PLANET"), "anomaly", 3, "anomaly4", 4, null);
            save("anomaly4", "🌀 Последствия", "Аномалия начинает расширяться.", Arrays.asList("Стабилизировать", "Использовать энергию", "Бежать"), Arrays.asList("Тратим 3 предмета, безопасно", "+4 предмета, но +3 КЗ", "Все -2 клетки"), Arrays.asList("ANOMALY_STABILIZE", "ANOMALY_USE", "ANOMALY_FLEE"), "anomaly", 4, null, 0, null);
            
            // === ДЛИННАЯ ЛИНИЯ В: ИИ (5 ситуаций) ===
            save("ai1", "🤖 Странное поведение ИИ", "ИИ начал вести себя необычно.", Arrays.asList("Проверить системы", "Игнорировать", "Отключить"), Arrays.asList("Находим уязвимость", "Ничего", "Безопасно, но теряем функции"), Arrays.asList("AI_CHECK", "AI_IGNORE", "AI_DISABLE"), "ai", 1, "ai2", 3, "AI_CHECK");
            save("ai2", "🤖 Бэкдор найден", "Программист нашел уязвимость в ИИ.", Arrays.asList("Активировать", "Продать", "Поделиться"), Arrays.asList("Вирус ослабевает", "+3 предмета", "Все +1 к решению"), Arrays.asList("AI_BACKDOOR", "AI_SELL", "AI_SHARE"), "ai", 2, "ai3", 3, null);
            save("ai3", "🤖 ИИ контратакует", "ИИ заметил вмешательство и создал КЗ.", Arrays.asList("Атаковать", "Защититься", "Взломать"), Arrays.asList("Все тратят предметы", "3 игрока тратят ходы", "50% успех"), Arrays.asList("AI_ATTACK", "AI_DEFEND", "AI_HACK"), "ai", 3, "ai4", 4, null);
            save("ai4", "🤖 Критическая ошибка", "ИИ начал удалять важные данные.", Arrays.asList("Остановить", "Симбиоз", "Эвакуация"), Arrays.asList("Тратим 4 предмета", "Сливаемся с ИИ", "Все -3 клетки"), Arrays.asList("AI_STOP", "AI_MERGE", "AI_EVAC"), "ai", 4, "ai5", 4, null);
            save("ai5", "🤖 Финальный выбор", "ИИ предлагает сделку: данные за свободу.", Arrays.asList("Принять", "Отказаться", "Уничтожить ИИ"), Arrays.asList("+5 предметов, но теряем контроль", "Безопасно", "Все КЗ исчезают, но -3 предмета"), Arrays.asList("AI_ACCEPT", "AI_REFUSE", "AI_DESTROY"), "ai", 5, null, 0, null);
            
            // === ДЛИННАЯ ЛИНИЯ Г: ЗЕМЛЯ (4 ситуации) ===
            save("earth1", "📡 Сигнал с Земли", "Получен сигнал: 'Подкрепление через 5 ходов.'", Arrays.asList("Ждать", "Не ждать", "Ответить (тратит предмет!)"), Arrays.asList("5 ходов, потом +5 предметов", "Продолжаем", "Тратим предмет, ускорение"), Arrays.asList("EARTH_WAIT", "EARTH_NOWAIT", "EARTH_REPLY"), "earth", 1, "earth2", 4, "EARTH_WAIT|EARTH_REPLY");
            save("earth2", "📡 Подкрепление прибыло", "Корабль поддержки предлагает помощь.", Arrays.asList("Принять", "Отказаться", "Объединиться"), Arrays.asList("+5 предметов, -1 узел", "Ничего", "+3 предмета, союз"), Arrays.asList("EARTH_HELP", "EARTH_REFUSE", "EARTH_ALLY"), "earth", 2, "earth3", 4, null);
            save("earth3", "📡 Секретная миссия", "Земля предлагает секретную миссию.", Arrays.asList("Согласиться", "Отказаться", "Переговоры"), Arrays.asList("+4 предмета, но риск", "Безопасно", "+2 предмета"), Arrays.asList("EARTH_SECRET", "EARTH_REFUSE2", "EARTH_NEGOTIATE"), "earth", 3, "earth4", 4, null);
            save("earth4", "📡 Финальное решение", "Земля предлагает вернуться домой.", Arrays.asList("Вернуться", "Остаться", "Передать данные"), Arrays.asList("Победа!", "Продолжаем", "+5 предметов"), Arrays.asList("EARTH_RETURN", "EARTH_STAY", "EARTH_DATA"), "earth", 4, null, 0, null);

            System.out.println("✅ УСПЕШНО ЗАПОЛНЕНО: " + repository.count() + " ситуаций!");
            System.out.println("=================================================================");
        } catch (Exception e) {
            System.err.println("❌ ОШИБКА: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void save(String id, String title, String desc, 
                     java.util.List<String> options, java.util.List<String> cons,
                     java.util.List<String> effects,
                     String chainId, int chainOrder, String next, int turns, String condition) {
        SituationEntity e = new SituationEntity();
        e.id = id;
        e.title = title;
        e.description = desc;
        e.options = options;
        e.consequences = cons;
        e.effects = effects;
        e.chainId = chainId;
        e.chainOrder = chainOrder;
        e.nextSituationId = next;
        e.turnsUntilNext = turns;
        e.condition = condition;
        repository.save(e);
    }
}
