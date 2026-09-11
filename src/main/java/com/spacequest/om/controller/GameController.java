package com.spacequest.om.controller;
import com.spacequest.om.service.GameEngine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class GameController {
    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameEngine gameEngine;
    
    public GameController(GameEngine gameEngine) { this.gameEngine = gameEngine; }

    @GetMapping("/")
    public String gamePage(Model model) {
        try {
            if (gameEngine.getSession().players.isEmpty()) gameEngine.startGame();
            model.addAttribute("game", gameEngine.getSession());
            return "game";
        } catch (Exception e) {
            log.error("❌ ОШИБКА: " + e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/move")
    public String move(@RequestParam int playerId, @RequestParam int dx, @RequestParam int dy) {
        gameEngine.movePlayer(playerId, dx, dy); return "redirect:/";
    }
    @PostMapping("/puzzle/start")
    public String startPuzzle() { gameEngine.startPuzzle(); return "redirect:/"; }
    @PostMapping("/puzzle/submit")
    public String submitPuzzle(@RequestParam String answer) { gameEngine.submitPuzzleAnswer(answer); return "redirect:/"; }
    @PostMapping("/result/close")
    public String closeResult() { gameEngine.closeResult(); return "redirect:/"; }
    @PostMapping("/threat")
    public String threatPhase() { gameEngine.threatPhase(); return "redirect:/"; }
    @PostMapping("/reset")
    public String reset() { gameEngine.startGame(); return "redirect:/"; }
    
    @PostMapping("/ability/reroll")
    public String reroll(@RequestParam int playerId) { gameEngine.rerollDice(playerId); return "redirect:/"; }
    @PostMapping("/ability/autoSolve")
    public String autoSolve(@RequestParam int playerId) { gameEngine.autoSolveBinary(playerId); return "redirect:/"; }
    @PostMapping("/ability/removeKZ")
    public String removeKZ(@RequestParam int playerId) { gameEngine.removeAdjacentKZ(playerId); return "redirect:/"; }
    @PostMapping("/ability/swap")
    public String swap(@RequestParam int playerId, @RequestParam int targetId) { gameEngine.swapWithPlayer(playerId, targetId); return "redirect:/"; }
    @PostMapping("/ability/drawTwo")
    public String drawTwo(@RequestParam int playerId) { gameEngine.drawTwoPuzzles(playerId); return "redirect:/"; }
    @PostMapping("/analyst/select")
    public String selectAnalystCard(@RequestParam int cardNumber) { gameEngine.selectAnalystCard(cardNumber); return "redirect:/"; }
    @PostMapping("/tool/draw")
    public String drawTool(@RequestParam int playerId) { gameEngine.drawTool(playerId); return "redirect:/"; }
    @PostMapping("/tool/use")
    public String useTool(@RequestParam int playerId, @RequestParam String toolName) { gameEngine.useTool(playerId, toolName); return "redirect:/"; }
    @PostMapping("/tool/hint/close")
    public String closeToolHint() { gameEngine.closeToolHint(); return "redirect:/"; }
    
    @PostMapping("/vote")
    public String castVote(@RequestParam int playerId, @RequestParam int option) { gameEngine.castVote(playerId, option); return "redirect:/"; }
    @PostMapping("/vote/finalize")
    public String finalizeVoting() { gameEngine.finalizeVoting(); return "redirect:/"; }
    
    @PostMapping("/debug/forceVote")
    public String forceVote() { gameEngine.forceRandomVote(); return "redirect:/"; }
    
    @PostMapping("/ending/close")
    public String closeEnding() { gameEngine.closeEnding(); return "redirect:/"; }
}
