package com.spacequest.om.service;
import com.spacequest.om.model.*;
import com.spacequest.om.entity.SituationEntity;
import com.spacequest.om.repository.SituationRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class GameEngine {
    private final GameSession session = new GameSession();
    private final SituationRepository situationRepo;
    private final Random random = new Random();

    public GameEngine(SituationRepository situationRepo) { this.situationRepo = situationRepo; }
    public GameSession getSession() { return session; }
    public void startGame() { session.init(); }

    private void applyBurn(Player p) {
        if (p.x == -1) return;
        p.burns++;
        session.gameLog += " 🔥 " + p.name + " получил ожог (" + p.burns + "/3)";
        if (p.burns >= 3) {
            session.gameLog += " 💀 " + p.name + " ПОГИБ!";
            if (p.x >= 0 && p.x < 12 && p.y >= 0 && p.y < 12) session.board[p.x][p.y].playerColors.remove(p.color);
            p.x = -1; p.y = -1;
        }
    }

    private int[] findNearestNode(Player p) {
        int[] nearest = null; int minDist = Integer.MAX_VALUE;
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 12; y++) {
                if (session.board[x][y].type == CellType.NODE) {
                    int dist = Math.abs(x - p.x) + Math.abs(y - p.y);
                    if (dist < minDist) { minDist = dist; nearest = new int[]{x, y}; }
                }
            }
        }
        return nearest;
    }

    private int countAlivePlayers() {
        int count = 0;
        for (Player p : session.players) if (p.x != -1) count++;
        return count;
    }

    private boolean hasTool(Player p) { return !p.tools.isEmpty(); }
    private void consumeTool(Player p) {
        if (!p.tools.isEmpty()) { session.gameLog += " (🛠️ потрачен: " + p.tools.remove(0) + ")"; }
    }
    private void giveTools(int count) {
        Player p = session.players.get(session.currentPlayerIndex);
        for (int i = 0; i < count && !session.toolDeck.isEmpty(); i++) p.tools.add(session.toolDeck.poll());
        session.gameLog += " 🎁 +" + count + " предметов.";
    }

    public void movePlayer(int playerId, int dx, int dy) {
        Player p = session.players.get(playerId);
        if (p.skipNextTurn) { p.skipNextTurn = false; session.gameLog = "⏭️ " + p.name + " пропустил ход."; nextTurn(); return; }
        int bonus = p.role.equals("Навигатор") ? 1 : 0;
        int newX = p.x + dx, newY = p.y + dy;
        if (newX >= 0 && newX < 12 && newY >= 0 && newY < 12) {
            Cell target = session.board[newX][newY];
            if (target.type != CellType.DEAD_END) {
                session.board[p.x][p.y].playerColors.remove(p.color);
                p.x = newX; p.y = newY;
                target.playerColors.add(p.color);
                String logMsg = p.name + " переместился на [" + newX + "," + newY + "]";
                if (bonus > 0) logMsg += " (🧭 +1)";
                session.gameLog = logMsg;
                checkKZCollision(p);
            }
        }
    }

    public void checkKZCollision(Player p) {
        if (p.x == -1) return;
        Cell cell = session.board[p.x][p.y];
        if (cell.hasKZ) {
            if (p.role.equals("Техник безопасности") && !p.abilityUsed) { p.abilityUsed = true; session.gameLog = "🛡️ " + p.name + " защитился от КЗ!"; }
            else if (p.shieldActive) { p.shieldActive = false; session.gameLog = "🛡️ " + p.name + " использовал Термоусадку!"; }
            else { applyBurn(p); }
        }
    }

    public void startPuzzle() {
        if (session.puzzleDeck.isEmpty()) { session.gameLog = "⚠️ Все головоломки решены!"; return; }
        Player p = session.players.get(session.currentPlayerIndex);
        Cell cell = session.board[p.x][p.y];
        if (cell.type == CellType.NODE) {
            session.currentPuzzle = session.puzzleDeck.poll();
            session.isPuzzleActive = true;
            session.currentPuzzle.timeSeconds += p.role.equals("Схемотехник") ? 60 : 0;
            session.gameLog = "⚡ " + p.name + " решает головоломку!";
        } else { session.gameLog = "⚠️ Здесь нет узла!"; }
    }

    public void submitPuzzleAnswer(String selectedAnswer) {
        boolean correct = selectedAnswer.trim().equalsIgnoreCase(session.currentPuzzle.correctAnswer.trim());
        session.lastAnswer = selectedAnswer; session.lastAnswerCorrect = correct; session.lastExplanation = session.currentPuzzle.explanation;
        session.showResult = true; session.isPuzzleActive = false;
        if (correct) {
            session.board[session.players.get(session.currentPlayerIndex).x][session.players.get(session.currentPlayerIndex).y].setType(CellType.CORRIDOR);
            session.fixedNodes++;
            session.gameLog = "✅ УСПЕХ! Осталось: " + (9 - session.fixedNodes);
        } else { spawnKZ(); session.gameLog = "❌ ПРОВАЛ! +1 КЗ"; }
        checkWinLoss(); checkEndings();
    }

    public void closeResult() { session.showResult = false; session.currentPuzzle = null; session.lastAnswer = null; session.lastExplanation = null; }

    public void threatPhase() {
        if (session.skipThreatPhase) { session.gameLog = "⚡ Аварийный генератор: фаза пропущена!"; session.skipThreatPhase = false; nextTurn(); return; }
        session.totalTurns++; session.turnsSinceLastKZ++;
        int roll = random.nextInt(6) + 1;
        String log = "🎲 Кубик угрозы: " + roll + ". ";
        if (roll <= 2) {
            if (session.turnsSinceLastKZ >= 3) { spawnKZ(); session.turnsSinceLastKZ = 0; log += "⚠️ +1 КЗ!"; }
            else { log += "🛡️ Защита сдержала (через " + (3 - session.turnsSinceLastKZ) + " хода)."; }
        } else if (roll <= 4) { log += "КЗ приближаются."; }
        else if (roll == 5) { log += "КЗ к ЯДРУ!"; }
        else { log += "✨ Удача!"; }
        session.gameLog = log; nextTurn(); checkWinLoss(); checkVotingTrigger(); checkEndings();
    }

    private void checkVotingTrigger() {
        List<String> readyChains = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : session.activeChains.entrySet()) {
            int newTurns = entry.getValue() - 1;
            if (newTurns <= 0) readyChains.add(entry.getKey()); else entry.setValue(newTurns);
        }
        for (String chainId : readyChains) {
            session.activeChains.remove(chainId);
            String nextId = session.pendingChains.remove(chainId);
            if (nextId != null) {
                SituationEntity next = situationRepo.findById(nextId).orElse(null);
                if (next != null && checkCondition(next.condition)) { startVoting(VotingSituation.fromEntity(next)); return; }
            }
        }
        if (!session.isVotingActive) {
            if (session.totalTurns == 3 || (session.totalTurns > 3 && random.nextInt(3) == 0)) {
                List<SituationEntity> available = new ArrayList<>();
                available.addAll(situationRepo.findByChainIdIsNull());
                available.addAll(situationRepo.findByChainIdIsNotNullAndChainOrder(1));
                if (!available.isEmpty()) {
                    startVoting(VotingSituation.fromEntity(available.get(random.nextInt(available.size()))));
                    session.gameLog += " | 🗳️ Наступило время голосования!";
                }
            }
        }
    }
    
    private boolean checkCondition(String condition) {
        if (condition == null || condition.isEmpty()) return true;
        for (String c : condition.split("\\|")) if (session.chainChoices.containsValue(c.trim())) return true;
        return false;
    }

    public void startVoting(VotingSituation situation) { session.currentVoting = situation; session.isVotingActive = true; session.votes.clear(); session.gameLog = "🗳️ КРИТИЧЕСКАЯ СИТУАЦИЯ: " + situation.title; }
    public void castVote(int playerId, int optionIndex) { if (!session.isVotingActive) return; session.votes.put(playerId, optionIndex); session.gameLog = "🗳️ " + session.players.get(playerId).name + " проголосовал (" + session.votes.size() + "/10)"; }

    public void finalizeVoting() {
        if (!session.isVotingActive || session.currentVoting == null) return;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int option : session.votes.values()) counts.put(option, counts.getOrDefault(option, 0) + 1);
        int winningOption = 0, maxVotes = 0;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) { if (entry.getValue() > maxVotes) { maxVotes = entry.getValue(); winningOption = entry.getKey(); } }
        
        VotingSituation v = session.currentVoting;
        String chosenOption = v.options.get(winningOption);
        String consequence = v.consequences.get(winningOption);
        String effect = v.effects.get(winningOption);
        
        applyEffect(effect);
        if (v.chainId != null) {
            session.chainChoices.put(v.chainId, effect);
            if (v.nextSituationId != null && v.turnsUntilNext > 0) {
                session.activeChains.put(v.chainId, v.turnsUntilNext);
                session.pendingChains.put(v.chainId, v.nextSituationId);
            }
        }
        session.votingHistory.add("📜 " + v.title + " → " + chosenOption);
        session.gameLog += " | 🗳️ РЕШЕНИЕ: " + chosenOption + ". Последствия: " + consequence + " (Голосов: " + maxVotes + "/10)";
        session.isVotingActive = false; session.currentVoting = null; session.votes.clear();
        checkEndings();
    }

    public void forceRandomVote() {
        List<SituationEntity> available = new ArrayList<>();
        available.addAll(situationRepo.findByChainIdIsNull());
        available.addAll(situationRepo.findByChainIdIsNotNullAndChainOrder(1));
        if (!available.isEmpty()) { startVoting(VotingSituation.fromEntity(available.get(random.nextInt(available.size())))); session.gameLog = "🐞 [ТЕСТ] Вызвано: " + session.currentVoting.title; }
    }

    private void applyEffect(String effect) {
        if (effect == null || effect.isEmpty()) return;
        Player p = session.players.get(session.currentPlayerIndex);
        
        if (effect.equals("RUSH50")) { if (random.nextBoolean()) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.min(11, pl.x + 2); session.gameLog += " | 🚀 +2 клетки всем!"; } else { for (int i = 0; i < 2; i++) spawnKZ(); session.gameLog += " | 💥 +2 КЗ!"; } }
        else if (effect.equals("BYPASS1") || effect.equals("BYPASS") || effect.equals("BYPASS2")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 1); session.gameLog += " | 🛡️ Все -1 клетка."; }
        else if (effect.equals("BRAKE2")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 2); session.gameLog += " | 🛑 Все -2 клетки."; }
        else if (effect.equals("SHARE1")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.min(11, pl.x + 1); session.gameLog += " | 🤝 Все +1 клетка."; }
        else if (effect.equals("DISTR1")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 1); session.gameLog += " | 💨 Все -1 клетка."; }
        else if (effect.equals("SLING")) { for (Player pl : session.players) { if (pl.x == -1) continue; if (pl.role.equals("Навигатор")) { pl.x = Math.min(11, pl.x + 3); session.gameLog += " 🚀 " + pl.name + " +3!"; } else { pl.x = Math.max(0, pl.x - 1); } } }
        else if (effect.equals("EVADE")) { boolean hasNav = false; for (Player pl : session.players) if (pl.role.equals("Навигатор") && pl.x != -1) hasNav = true; if (hasNav) session.gameLog += " | 🎯 Маневр успешен!"; else { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 1); session.gameLog += " | ❌ Все -1."; } }
        else if (effect.equals("TELEPORT")) { if (p.x != -1) { int[] nearest = findNearestNode(p); if (nearest != null) { session.board[p.x][p.y].playerColors.remove(p.color); p.x = nearest[0]; p.y = nearest[1]; session.board[p.x][p.y].playerColors.add(p.color); session.gameLog += " | 🌀 " + p.name + " телепортирован к [" + p.x + "," + p.y + "]!"; } } }
        else if (effect.equals("EXP2BURN")) { applyBurn(p); }
        else if (effect.equals("SAVE1") || effect.equals("SAVE2")) { for (Player pl : session.players) if (pl.x != -1) { applyBurn(pl); break; } }
        else if (effect.equals("SAVEBOTH50")) { if (random.nextBoolean()) session.gameLog += " | ✅ Оба спасены."; else { int b = 0; for (Player pl : session.players) if (pl.x != -1 && b < 2) { applyBurn(pl); b++; } } }
        else if (effect.equals("IGNORE5BURN") || effect.equals("OTHER30") || effect.equals("IGNORE_BURN")) { for (Player pl : session.players) if (pl.x != -1) applyBurn(pl); if (effect.equals("IGNORE_BURN")) removeRandomKZ(2); }
        else if (effect.equals("RISK50")) { if (!random.nextBoolean()) { for (Player pl : session.players) if (pl.x != -1) applyBurn(pl); } }
        else if (effect.equals("FIX1_BURN2")) { if (session.fixedNodes < 9) session.fixedNodes++; int b = 0; for (Player pl : session.players) if (pl.x != -1 && b < 2) { applyBurn(pl); b++; } }
        else if (effect.equals("RAM1")) { spawnKZ(); }
        else if (effect.equals("SHIELD5")) { spawnKZ(); }
        else if (effect.equals("MAX50")) { if (!random.nextBoolean()) { for (int i = 0; i < 2; i++) spawnKZ(); } }
        else if (effect.equals("KILL2_SPAWN2")) { removeRandomKZ(2); for (int i = 0; i < 2; i++) spawnKZ(); }
        else if (effect.contains("HACK") || effect.contains("ATTACK") || effect.contains("RETREAT")) { removeRandomKZ(999); }
        else if (effect.equals("HACK50")) { if (random.nextBoolean()) removeRandomKZ(999); else { for (int i = 0; i < 3; i++) spawnKZ(); } }
        else if (effect.equals("ANTIVIRUS2")) { if (hasTool(p)) consumeTool(p); removeRandomKZ(999); }
        else if (effect.equals("TAKE")) { for (int i = 0; i < 3; i++) spawnKZ(); giveTools(2); }
        else if (effect.equals("OPEN4")) { giveTools(4); spawnKZ(); }
        else if (effect.equals("ADAPT1")) { giveTools(1); }
        else if (effect.equals("GAIN3") || effect.equals("SELL3")) { giveTools(3); }
        else if (effect.equals("GAIN4")) { giveTools(4); }
        else if (effect.equals("DISASSEMBLE3")) { giveTools(3); }
        else if (effect.equals("DEAL3")) { for (int i = 0; i < 3 && !p.tools.isEmpty(); i++) p.tools.remove(0); }
        else if (effect.equals("BACKUP1")) { if (hasTool(p)) consumeTool(p); }
        else if (effect.equals("AMBUSH50")) { if (random.nextBoolean()) giveTools(3); else { for (int i = 0; i < 2 && !p.tools.isEmpty(); i++) p.tools.remove(0); } }
        else if (effect.equals("OPEN2") || effect.equals("TRADE2") || effect.equals("TRADE2_BREAK1")) { giveTools(2); }
        else if (effect.equals("HELP3_BREAK2") || effect.equals("HELP4_BREAK1")) { giveTools(effect.equals("HELP4_BREAK1") ? 4 : 3); }
        else if (effect.equals("FIX1") || effect.equals("TRUST1FIX")) { if (session.fixedNodes < 9) session.fixedNodes++; }
        else if (effect.equals("ACCEL15")) { int f = 0; for (int x = 0; x < 12 && f < 2; x++) for (int y = 0; y < 12 && f < 2; y++) if (session.board[x][y].type == CellType.NODE) { session.board[x][y].setType(CellType.CORRIDOR); session.fixedNodes++; f++; } checkWinLoss(); }
        else if (effect.equals("ALLIN")) { session.fixedNodes = 9; checkWinLoss(); }
        else if (effect.equals("PROBE1NODES") || effect.equals("PROBE1")) { }
        else if (effect.equals("LOW3")) { removeRandomKZ(999); }
        else if (effect.equals("DELIVER3")) { removeRandomKZ(3); }
        else if (effect.equals("COMPUTER50")) { if (random.nextBoolean()) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.min(11, pl.x + 2); } else { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 2); } }
        else if (effect.equals("EVAC_LOSE") || effect.equals("LAST50")) { if (!random.nextBoolean()) session.gameLog = "💀 ПОРАЖЕНИЕ!"; }
        else if (effect.equals("SACRIFICE") || effect.equals("SACRIFICE_ALL") || effect.equals("ESCAPE_SACRIFICE")) { p.burns = 3; p.x = -1; p.y = -1; session.fixedNodes = 9; session.captainSacrifice = true; checkWinLoss(); }
        
        // === ПРИШЕЛЬЦЫ ===
        else if (effect.equals("ALIEN_HELP")) { session.alienContact = true; session.gameLog += " | 👽 Контакт установлен!"; }
        else if (effect.equals("ALIEN_IGNORE")) { session.gameLog += " | 🤷 Проигнорировали."; }
        else if (effect.equals("ALIEN_ATTACK")) { session.pirateMode = true; giveTools(3); session.gameLog += " | 🏴‍☠️ Напали! +3 предмета."; }
        else if (effect.equals("ALIEN_TRADE")) { session.alienFriendly = true; giveTools(2); session.gameLog += " | 🤝 Обмен технологиями!"; }
        else if (effect.equals("ALIEN_REFUSE") || effect.equals("ALIEN_LEAVE")) { session.gameLog += " | 🚫 Отказались/Улетели."; }
        else if (effect.equals("ALIEN_DEMAND")) { session.alienAngry = true; session.gameLog += " | 😠 Пришельцы обижены!"; }
        else if (effect.equals("ALIEN_LAND")) { if (random.nextInt(10) < 3) { applyBurn(p); session.gameLog += " | ❌ Высадка прошла плохо!"; } else { giveTools(3); session.gameLog += " | ✅ Высадка успешна!"; } session.alienAngry = true; }
        else if (effect.equals("ALIEN_PROBE")) { if (hasTool(p)) { consumeTool(p); session.alienFriendly = true; session.planetDiscovered = true; giveTools(1); session.gameLog += " | 📡 Зонд отправлен. Планета открыта!"; } else { session.alienAngry = true; if (random.nextInt(10) < 3) applyBurn(p); session.gameLog += " | ⚠️ Нет предметов! Пришлось высадиться."; } }
        else if (effect.equals("ALIEN_STUDY")) { session.planetDiscovered = true; giveTools(4); session.gameLog += " | 🔬 Изучили данные. Планета открыта!"; }
        else if (effect.equals("ALIEN_STEAL")) { giveTools(5); session.alienAngry = true; session.gameLog += " | 🕵️ Украли технологии!"; }
        else if (effect.equals("ALIEN_SHARE")) { session.gameLog += " | 📚 Поделились с экипажем."; }
        else if (effect.equals("ALIEN_ALLY")) { session.alienFriendly = true; session.planetDiscovered = true; giveTools(5); session.gameLog += " | 🤝 Союз заключён! Планета открыта!"; }
        else if (effect.equals("ALIEN_BREAK")) { session.gameLog += " | 🚫 Прервали контакт."; }
        else if (effect.equals("ALIEN_MERGE_ALIEN")) { session.aiMerged = true; session.gameLog += " | 🧬 Симбиоз с технологиями пришельцев!"; }
        else if (effect.equals("ALIEN_FIX")) { if (session.fixedNodes < 9) { session.fixedNodes++; session.gameLog += " | 🔧 Пришельцы помогли!"; } }
        
        // === НОВАЯ ВЕТКА: МЕСТЬ ПРИШЕЛЬЦЕВ ===
        else if (effect.equals("ALIEN_SURRENDER")) { for (int i = 0; i < p.tools.size(); i++) p.tools.remove(0); session.gameLog += " | 🏳️ Сдались. Все предметы потеряны."; }
        else if (effect.equals("ALIEN_NEGOTIATE")) { if (random.nextInt(10) < 3) { session.alienFriendly = true; session.alienAngry = false; session.gameLog += " | 🤝 Удалось договориться!"; } else { for (int i = 0; i < 3; i++) spawnKZ(); for (Player pl : session.players) if (pl.x != -1) applyBurn(pl); session.gameLog += " | ❌ Переговоры провалились! Атака!"; } }
        else if (effect.equals("ALIEN_FIGHT")) { for (Player pl : session.players) if (pl.x != -1) applyBurn(pl); removeRandomKZ(3); session.gameLog += " | ⚔️ Сражаемся! Все получают ожоги, но -3 КЗ."; }
        else if (effect.equals("ALIEN_ESCAPE")) { session.escapePods = true; session.gameLog += " | 🚀 Спасаемся на шлюпках!"; }

        // === АНОМАЛИЯ ===
        else if (effect.equals("ANOMALY_EXPLORE")) { session.anomalyExplored = true; int b = 0; for (Player pl : session.players) if (pl.x != -1 && b < 2) { applyBurn(pl); b++; } }
        else if (effect.equals("ANOMALY_DESTROY") || effect.equals("ANOMALY_CLOSE") || effect.equals("ANOMALY_DESTROY_PLANET")) { session.gameLog += " | 💥 Уничтожено."; }
        else if (effect.equals("ANOMALY_IGNORE")) { session.gameLog += " | 🤷 Игнорируем."; }
        else if (effect.equals("ANOMALY_PORTAL")) { session.portalEntered = true; if (random.nextInt(10) < 3) { p.burns = 3; p.x = -1; p.y = -1; session.gameLog += " | 💀 " + p.name + " не вернулся!"; } else { session.planetDiscovered = true; session.gameLog += " | 🌍 Планета обнаружена!"; } }
        else if (effect.equals("ANOMALY_STUDY")) { session.gameLog += " | 📚 Изучили."; }
        else if (effect.equals("ANOMALY_COLONIZE")) { session.planetDiscovered = true; session.gameLog += " | 🏘️ Колония основана! Планета открыта!"; }
        else if (effect.equals("ANOMALY_MINE")) { giveTools(4); for (int i = 0; i < 2; i++) spawnKZ(); session.gameLog += " | ⛏️ Добыли ресурсы. +2 КЗ!"; }
        else if (effect.equals("ANOMALY_STABILIZE")) { for (int i = 0; i < 3 && !p.tools.isEmpty(); i++) p.tools.remove(0); session.gameLog += " | 🔧 Стабилизировали. -3 предмета."; }
        else if (effect.equals("ANOMALY_USE")) { giveTools(4); for (int i = 0; i < 3; i++) spawnKZ(); session.gameLog += " | ⚡ Использовали энергию. +3 КЗ!"; }
        else if (effect.equals("ANOMALY_FLEE")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 2); session.gameLog += " | 🏃 Бежим! Все -2 клетки."; }
        
        // === ИИ ===
        else if (effect.equals("AI_CHECK") || effect.equals("AI_BACKDOOR")) { session.backdoorActive = true; session.gameLog += " | 🔍 Найдена уязвимость!"; }
        else if (effect.equals("AI_IGNORE")) { session.gameLog += " | 🤷 Игнорируем."; }
        else if (effect.equals("AI_DISABLE")) { session.gameLog += " | 🔌 ИИ отключён."; }
        else if (effect.equals("AI_SELL")) { giveTools(3); }
        else if (effect.equals("AI_SHARE")) { session.gameLog += " | 📚 Поделились."; }
        else if (effect.equals("AI_CONTINUE")) { if (random.nextBoolean()) removeRandomKZ(999); else { for (int i = 0; i < 3; i++) spawnKZ(); } }
        else if (effect.equals("AI_RETREAT")) { session.gameLog += " | 🔙 Отступили."; }
        else if (effect.equals("AI_MERGE")) { session.aiMerged = true; }
        else if (effect.equals("AI_ATTACK") || effect.equals("AI_HACK")) { removeRandomKZ(999); }
        else if (effect.equals("AI_DEFEND")) { removeRandomKZ(3); }
        else if (effect.equals("AI_STOP")) { for (int i = 0; i < 4 && !p.tools.isEmpty(); i++) p.tools.remove(0); session.gameLog += " | 🛑 Остановили ИИ. -4 предмета."; }
        else if (effect.equals("AI_EVAC")) { for (Player pl : session.players) if (pl.x != -1) pl.x = Math.max(0, pl.x - 3); session.gameLog += " | 🏃 Эвакуация! Все -3 клетки."; }
        else if (effect.equals("AI_ACCEPT")) { giveTools(5); session.gameLog += " | 🤝 Сделка принята."; }
        else if (effect.equals("AI_REFUSE")) { session.gameLog += " | 🚫 Отказались."; }
        else if (effect.equals("AI_DESTROY")) { for (int i = 0; i < 3 && !p.tools.isEmpty(); i++) p.tools.remove(0); removeRandomKZ(999); session.gameLog += " | 💥 ИИ уничтожен. Все КЗ исчезли!"; }
        
        // === ЗЕМЛЯ ===
        else if (effect.equals("EARTH_WAIT")) { session.earthContact = true; giveTools(5); }
        else if (effect.equals("EARTH_NOWAIT")) { session.gameLog += " | 🚫 Не ждём."; }
        else if (effect.equals("EARTH_REPLY")) { if (hasTool(p)) { consumeTool(p); session.earthContact = true; } }
        else if (effect.equals("EARTH_HELP")) { giveTools(5); if (session.fixedNodes > 0) session.fixedNodes--; }
        else if (effect.equals("EARTH_REFUSE") || effect.equals("EARTH_REFUSE2")) { session.gameLog += " | 🚫 Отказались."; }
        else if (effect.equals("EARTH_ALLY")) { session.earthAlliance = true; giveTools(3); }
        else if (effect.equals("EARTH_SECRET")) { giveTools(4); if (random.nextBoolean()) applyBurn(p); }
        else if (effect.equals("EARTH_NEGOTIATE")) { giveTools(2); }
        else if (effect.equals("EARTH_RETURN")) { session.fixedNodes = 9; checkWinLoss(); }
        else if (effect.equals("EARTH_STAY")) { session.gameLog += " | 🚀 Остаёмся."; }
        else if (effect.equals("EARTH_DATA")) { giveTools(5); }
        
        // === ШЛЮПКИ ===
        else if (effect.equals("ESCAPE_ACTIVATE")) { session.escapePods = true; }
        else if (effect.equals("ESCAPE_CONTINUE")) { session.gameLog += " | 🔧 Продолжаем."; }
        else { session.gameLog += " | ⚙️ Эффект: " + effect; }
    }

    private int removeRandomKZ(int count) {
        int removed = 0;
        for (int i = 0; i < 200 && removed < count; i++) {
            int x = random.nextInt(12), y = random.nextInt(12);
            if (session.board[x][y].hasKZ) { session.board[x][y].hasKZ = false; session.kzCount--; removed++; }
        }
        return removed;
    }

    private void spawnKZ() { int x = random.nextInt(12), y = random.nextInt(12); session.board[x][y].hasKZ = true; session.kzCount++; }

    private void nextTurn() {
        session.currentPlayerIndex = (session.currentPlayerIndex + 1) % 10;
        Player nextPlayer = session.players.get(session.currentPlayerIndex);
        while (nextPlayer.x == -1 && nextPlayer.y == -1) { session.currentPlayerIndex = (session.currentPlayerIndex + 1) % 10; nextPlayer = session.players.get(session.currentPlayerIndex); }
        if (nextPlayer.role.equals("Стажер") && !session.toolDeck.isEmpty()) { String tool = session.toolDeck.poll(); nextPlayer.tools.add(tool); session.gameLog += " | 🎁 " + nextPlayer.name + " (Стажер): " + tool; }
        else { session.gameLog += " | Ход: " + nextPlayer.name; }
    }

    private void checkWinLoss() {
        if (session.fixedNodes >= 9) session.gameLog = "🏆 ПОБЕДА! Все узлы починены!";
        if (session.kzCount >= 7) session.gameLog = "💀 ПОРАЖЕНИЕ! Критическая масса КЗ.";
    }

    // === ПРОВЕРКА КОНЦОВОК (12 штук!) ===
    public void checkEndings() {
        if (session.showEnding) return;
        int alivePlayers = countAlivePlayers();
        
        if (alivePlayers == 0) { triggerEnding("💀 ПОГЛОЩЕНЫ ЧЕРНОЙ ДЫРОЙ", "Весь экипаж погиб. Корабль «ОМ» стал жертвой Логического Вируса.", "BAD"); return; }
        if (session.kzCount >= 7) { triggerEnding("💀 КРИТИЧЕСКАЯ МАССА КЗ", "Маркеры КЗ достигли критической массы. Корабль разорван на части.", "BAD"); return; }
        if (session.alienAngry && session.kzCount >= 5 && alivePlayers == 0) { triggerEnding("👽 ГНЕВ ПРИШЕЛЬЦЕВ", "Разгневанные пришельцы уничтожили корабль «ОМ». Вирус вырвался на свободу.", "BAD"); return; }
        
        if (session.fixedNodes >= 9 && session.alienFriendly && session.planetDiscovered) { triggerEnding("🌟 НОВЫЙ ДОМ", "Экипаж починил все узлы И нашёл планету с дружелюбными пришельцами. Основана первая колония!", "GOOD"); return; }
        if (session.fixedNodes >= 9 && alivePlayers > 0) { triggerEnding("🏠 ВОЗВРАЩЕНИЕ ДОМОЙ", "Все узлы починены! Корабль «ОМ» вернулся на Землю.", "GOOD"); return; }
        
        if (session.alienFriendly && session.planetDiscovered && session.fixedNodes < 9) { triggerEnding("🛸 ПРИГЛАШЕНИЕ НА ПЛАНЕТУ", "Пришельцы приглашают людей на свою планету. Корабль остался дрейфовать, но экипаж спасён.", "GOOD"); return; }
        if (session.pirateMode && alivePlayers > 0 && session.fixedNodes < 9) { triggerEnding("🏴‍☠️ СВОБОДНЫЕ КАПИТАНЫ", "Экипаж стал свободными капитанами, исследующими космос без привязки к дому.", "GOOD"); return; }
        if (session.aiMerged && alivePlayers > 0 && session.fixedNodes < 9) { triggerEnding("🧠 ЦИФРОВОЕ БЕССМЕРТИЕ", "Экипаж слился с ИИ. Теперь они цифровые сущности, живущие в сети корабля.", "GOOD"); return; }
        if (session.captainSacrifice && alivePlayers > 0 && session.fixedNodes < 9) { triggerEnding("🕯️ ПАМЯТЬ О ГЕРОЕ", "Герой пожертвовал собой. Выжившие возвращаются с историей о мужестве.", "GOOD"); return; }
        if (session.escapePods && alivePlayers > 0 && session.fixedNodes < 9) { triggerEnding("🚀 ГЕРОИ-ВЫЖИВШИЕ", "Экипаж спасся на шлюпках. Они не починили все узлы, но выжили.", "GOOD"); return; }
        
        if (session.alienAngry && session.escapePods && alivePlayers > 0) { triggerEnding("🏃 БЕГСТВО ОТ ПРИШЕЛЬЦЕВ", "Экипаж спасся на шлюпках от разгневанных пришельцев. Корабль потерян.", "NEUTRAL"); return; }
        if (session.alienAngry && session.pirateMode && alivePlayers > 0) { triggerEnding("⚔️ ВОЙНА С ПРИШЕЛЬЦАМИ", "Экипаж вступил в войну с пришельцами и победил, став новыми тиранами космоса.", "NEUTRAL"); return; }
        if (session.alienAngry && session.alienFriendly && alivePlayers > 0) { triggerEnding("🕊️ ДИПЛОМАТИЧЕСКАЯ ПОБЕДА", "Несмотря на конфликт, экипаж смог договориться. Пришельцы простили их.", "GOOD"); return; }
    }
    
    private void triggerEnding(String title, String text, String type) { session.showEnding = true; session.endingTitle = title; session.endingText = text; session.endingType = type; session.gameLog = "🎬 КОНЦОВКА: " + title; }
    public void closeEnding() { session.showEnding = false; }

    public void rerollDice(int playerId) { Player p = session.players.get(playerId); if (p.role.equals("Главный Инженер") && !p.abilityUsed) { p.abilityUsed = true; session.gameLog = "🎲 " + p.name + " активировал переброс!"; } }
    public void autoSolveBinary(int playerId) {
        Player p = session.players.get(playerId);
        if (p.role.equals("Программист") && !p.abilityUsed && session.isPuzzleActive) {
            String q = session.currentPuzzle.question.toLowerCase();
            if (q.contains("двоичн") || q.contains("бинарн") || q.contains("1010") || q.contains("173")) {
                session.lastAnswer = session.currentPuzzle.correctAnswer; session.lastAnswerCorrect = true; session.lastExplanation = "🤖 Авто-решение! " + session.currentPuzzle.explanation;
                session.showResult = true; session.isPuzzleActive = false; p.abilityUsed = true;
                session.board[p.x][p.y].setType(CellType.CORRIDOR); session.fixedNodes++; session.gameLog = "✅ " + p.name + " решил автоматически!"; checkWinLoss();
            } else { session.gameLog = "⚠️ Не бинарная задача!"; }
        }
    }
    public void removeAdjacentKZ(int playerId) {
        Player p = session.players.get(playerId);
        if (p.role.equals("Электрик") && !p.abilityUsed) {
            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) {
                int nx = p.x + d[0], ny = p.y + d[1];
                if (nx >= 0 && nx < 12 && ny >= 0 && ny < 12 && session.board[nx][ny].hasKZ) {
                    session.board[nx][ny].hasKZ = false; session.kzCount--; p.abilityUsed = true; session.gameLog = "⚡ " + p.name + " убрал КЗ с [" + nx + "," + ny + "]!"; return;
                }
            }
            session.gameLog = "⚠️ Рядом нет КЗ!";
        }
    }
    public void swapWithPlayer(int playerId, int targetId) {
        Player p1 = session.players.get(playerId), p2 = session.players.get(targetId);
        if (p1.role.equals("Механик") && !p1.abilityUsed && p2.x != -1) {
            session.board[p1.x][p1.y].playerColors.remove(p1.color); session.board[p2.x][p2.y].playerColors.remove(p2.color);
            int tx = p1.x, ty = p1.y; p1.x = p2.x; p1.y = p2.y; p2.x = tx; p2.y = ty;
            session.board[p1.x][p1.y].playerColors.add(p1.color); session.board[p2.x][p2.y].playerColors.add(p2.color);
            p1.abilityUsed = true; session.gameLog = "🔄 " + p1.name + " поменялся с " + p2.name + "!";
        }
    }
    public void drawTwoPuzzles(int playerId) {
        Player p = session.players.get(playerId);
        if (p.role.equals("Аналитик") && !p.abilityUsed && session.puzzleDeck.size() >= 2) {
            session.analystFirstCard = session.puzzleDeck.poll(); session.analystSecondCard = session.puzzleDeck.poll();
            session.analystChoosing = true; p.abilityUsed = true; session.gameLog = "🧠 " + p.name + " выбирает из двух!";
        }
    }
    public void selectAnalystCard(int cardNumber) {
        if (!session.analystChoosing) return;
        if (cardNumber == 1) { session.currentPuzzle = session.analystFirstCard; session.puzzleDeck.addLast(session.analystSecondCard); }
        else { session.currentPuzzle = session.analystSecondCard; session.puzzleDeck.addLast(session.analystFirstCard); }
        session.analystChoosing = false; session.isPuzzleActive = true;
    }
    public void drawTool(int playerId) {
        Player p = session.players.get(playerId);
        if (!session.toolDeck.isEmpty() && p.tools.size() < 3) { p.tools.add(session.toolDeck.poll()); session.gameLog = "🛠️ " + p.name + " получил предмет"; }
        else { session.gameLog = "⚠️ Колода пуста или 3 предмета!"; }
    }
    public void useTool(int playerId, String toolName) {
        Player p = session.players.get(playerId);
        if (!p.tools.contains(toolName)) return;
        if (toolName.equals("Мультиметр") || toolName.equals("Тестер цепей")) { if (session.isPuzzleActive) { session.toolHint = "🔍 Ответ: " + session.currentPuzzle.correctAnswer; session.showToolHint = true; } else session.gameLog = "⚠️ Только во время головоломки!"; }
        else if (toolName.equals("Оловоотсос")) { session.gameLog = "🧹 Убрано " + removeRandomKZ(1) + " КЗ!"; }
        else if (toolName.equals("Флюс")) { p.skipNextTurn = true; session.gameLog = "⏭️ Пропуск хода."; }
        else if (toolName.equals("Паяльная станция") || toolName.equals("Экстренный ремонт")) { Cell cell = session.board[p.x][p.y]; if (cell.type == CellType.NODE) { cell.setType(CellType.CORRIDOR); session.fixedNodes++; session.gameLog = "🔧 Узел починен!"; checkWinLoss(); } else session.gameLog = "⚠️ Только на узле!"; }
        else if (toolName.equals("Термоусадка")) { p.shieldActive = true; session.gameLog = "🛡️ Защита активирована!"; }
        else if (toolName.equals("Осциллограф")) { if (session.isPuzzleActive) { String h = session.currentPuzzle.correctAnswer.length() > 3 ? session.currentPuzzle.correctAnswer.substring(0, 3) : session.currentPuzzle.correctAnswer; session.toolHint = "📊 Ответ начинается с '" + h + "'"; session.showToolHint = true; } else session.gameLog = "⚠️ Только во время головоломки!"; }
        else if (toolName.equals("Аварийный генератор")) { session.skipThreatPhase = true; session.gameLog = "⚡ Фаза пропущена!"; }
        else { session.gameLog = "⚡ " + toolName + " использован!"; }
        p.tools.remove(toolName);
    }
    public void closeToolHint() { session.showToolHint = false; session.toolHint = null; }
}
