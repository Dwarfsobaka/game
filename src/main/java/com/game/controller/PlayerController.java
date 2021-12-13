package com.game.controller;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController{
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    //1.создать игрока
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player){
        if (playerService.isEmptyBody(player)){
            return new ResponseEntity<>(player, HttpStatus.BAD_REQUEST);
        }
        else if (!playerService.checkPlayerBeforeSave(player)) {
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
        }
        else {
            player.setLevel(playerService.countLevel(player));	//высчитываем текущий  уровень
            player.setUntilNextLevel(playerService.countUntilNextLevel(player));	//высчитываем до след уровня
            if(player.getBanned() == null){
                player.setBanned(false);
            }
            Player savedPlayer = playerService.createPlayer(player);
            return new ResponseEntity<>(savedPlayer, HttpStatus.OK);
        }
    }

    //2. удалить игрока
    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable("id")  String playerId){
        Long id;
        if (playerService.isValidId(playerId)){
            id = Long.parseLong(playerId);
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(!playerService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            playerService.deletePlayer(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    //3. редактровать характеристики существующего
    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") String playerId, @RequestBody Player player ) {
        Long id = 0L;
        if (playerService.isValidId(playerId)){
            id = Long.parseLong(playerId);
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (playerService.isEmptyBody(player)){
            return new ResponseEntity<>(playerService.findById(id),HttpStatus.OK);
        }
        else if (!playerService.checkBirthday(player.getBirthday())
                || !playerService.checkExperience(player.getExperience())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else if(!playerService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            Player newPlayer = playerService.updatePlayer(id, player);
            return new ResponseEntity<>(newPlayer, HttpStatus.OK);
        }
    }

    //4.найти по ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") String playerId) {
        Long id;
        if (playerService.isValidId(playerId)){
            id = Long.parseLong(playerId);
        }
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
       if(!playerService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            Player player = playerService.findById(id);
            return new ResponseEntity<>(player, HttpStatus.OK);
        }
    }

    //5.найти по параметрам
    @GetMapping("")
    public ResponseEntity<List<Player>> getAll(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize   ) {

        List<Player> playerList;

        if (after!= null && before != null) {
          playerList = playerService.findAllByParams(name, title, race, profession,
                    new Date(after), new Date(before), banned, minExperience, maxExperience,
                    minLevel, maxLevel, pageNumber, pageSize);
        }
        else {
            playerList = playerService.findAllByParams(name, title, race, profession, null, null,
                    banned, minExperience, maxExperience,
                    minLevel, maxLevel,pageNumber, pageSize);
        }

        return new ResponseEntity<>(playerList, HttpStatus.OK);
    }

    //6. посчитать найденных по параметрам
    @GetMapping("/count")
    public ResponseEntity<Integer> getAllCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false) Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {

        int countByParams = 0;
        if (after!= null && before != null) {
            countByParams =  playerService.findAllByParamsAndCount(name, title, race, profession,
                    new Date(after), new Date(before), banned, minExperience, maxExperience,
                    minLevel, maxLevel);
        }
        else if (after == null && before!= null) {
            countByParams =  playerService.findAllByParamsAndCount(name, title, race, profession,
                    null,  new Date(before), banned, minExperience, maxExperience,
                    minLevel, maxLevel);
        }

        else if (before == null && after != null) {
            countByParams =  playerService.findAllByParamsAndCount(name, title, race, profession,
                    new Date(after),  null, banned, minExperience, maxExperience,
                    minLevel, maxLevel);
        }
        else {
            countByParams = playerService.findAllByParamsAndCount(name, title, race, profession, null, null,
                    banned, minExperience, maxExperience,
                    minLevel, maxLevel);
        }
        return new ResponseEntity<>(countByParams, HttpStatus.OK);
    }
    }


