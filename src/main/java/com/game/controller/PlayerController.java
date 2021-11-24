package com.game.controller;
import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequestMapping("/rest/players")
public class PlayerController{
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    //1.найти всех
    @GetMapping("/")
    public ResponseEntity<List<Player>> getAll() {
        return new ResponseEntity<>(playerService.findAll(), HttpStatus.OK);
    }

   //2.создать игрока
   @PostMapping("/")
   @ResponseBody
    public  ResponseEntity<Player> createPlayer(@RequestBody Player player){
        if (player == null || !playerService.checkPlayerBeforeSave(player)) {                                       //если игрок полностью null
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
        }
//     else if () {             //если какие-то поля null
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
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

    //3. удалить игрока
    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") Long playerId){
        if (!playerService.isValidId(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
       else if(!playerService.existsById(playerId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
       else {
           playerService.deletePlayer(playerId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        }


    //4. редактровать характеристики существующего
    @PostMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long playerId,@RequestBody Player player ) {
        if (player==null){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else if (!playerService.checkPlayerBeforeUpdate(player) || !playerService.isValidId(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else if(!playerService.existsById(playerId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            Player newPlayer = playerService.updatePlayer(playerId, player);
            return new ResponseEntity<>(newPlayer, HttpStatus.OK);
        }
    }

    //5.найти по ID
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long playerId) {
        if (!playerService.isValidId(playerId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
       else if(!playerService.existsById(playerId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        else {
            Player player = playerService.findById(playerId);
            return new ResponseEntity<>(player, HttpStatus.OK);
        }
    }



}
