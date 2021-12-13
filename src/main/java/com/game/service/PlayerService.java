package com.game.service;

import java.util.*;

import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.transaction.annotation.Transactional;

//класс обращается к базам данных + здесь пишется бизнес-логика

@Service
public class PlayerService {

	@Autowired
	private final PlayerRepository playerRepository;

	public PlayerService(PlayerRepository playerRepository) {
		this.playerRepository = playerRepository;
	}

	//1. найти всех
	@Transactional
	public List<Player> findAll() {
		return (List<Player>) playerRepository.findAll();
	}

	//2. создать игрока
	@Transactional
	public Player createPlayer(Player player) {
		playerRepository.save(player);
		return player;
	}

	//3. обновить игрока
	@Transactional
	public Player updatePlayer(long id, Player player) {
		Optional<Player> newPlayer = playerRepository.findById(id);
		Player updatePlayer = newPlayer.get();						//сюда сохраняем старого игрока
		if(player.getName() != null){
			updatePlayer.setName(player.getName());
		}
		if(player.getTitle() != null){
			updatePlayer.setTitle(player.getTitle());
		}
		if(player.getRace() != null){
			updatePlayer.setRace(player.getRace());
		}
		if(player.getProfession() != null){
			updatePlayer.setProfession(player.getProfession());
		}
		if(player.getBirthday() != null){
			updatePlayer.setBirthday(player.getBirthday());
		}
		if(player.getBanned() != null) {
			updatePlayer.setBanned(player.getBanned());
		}
		if(player.getExperience() != null){
			updatePlayer.setExperience(player.getExperience());
		}
		updatePlayer.setLevel(countLevel(updatePlayer));
		updatePlayer.setUntilNextLevel(countUntilNextLevel(updatePlayer));
		playerRepository.save(updatePlayer);
		return updatePlayer;

	}

	//4. удалить игрока
	@Transactional
	public void deletePlayer(Long id) {
		playerRepository.deleteById(id);
	}

	//5. найти по ID
	@Transactional
	public Player findById(Long id) {
		Optional<Player> player = playerRepository.findById(id);
		return player.get();
	}

	public boolean existsById(Long id) {
		if (!playerRepository.existsById(id)){
			return false;
		}
		return true;
	}

	//6. найти по параметрам(фильтрам)
	@Transactional
	public List<Player> findAllByParams(String name, String title, Race race, Profession profession,
										Date after, Date before,Boolean banned, Integer minExperience,
										Integer maxExperience, Integer minLevel, Integer maxLevel,
										Integer pageNumber, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		List<Player> all = playerRepository.findAllByParams(name, title, race, profession, after, before,
										banned, minExperience, maxExperience, minLevel,
										maxLevel, pageable);
		return all;
	}

	//7. посчитать кол-во найденных по параметрам
	@Transactional
	public int findAllByParamsAndCount(String name, String title, Race race, Profession profession,
									   Date after, Date before, Boolean banned, Integer minExperience,
									   Integer maxExperience, Integer minLevel, Integer maxLevel) {

		List<Player> all = playerRepository.findAllByParamsAndCount(name, title, race, profession, after, before,
										banned, minExperience, maxExperience, minLevel,maxLevel);
		return all.size();
	}

	//проверка полей перед созданием игрока
	public boolean checkPlayerBeforeSave(Player player){
		Long begin = new GregorianCalendar(2000,1,1).getTimeInMillis();
		Long end = new GregorianCalendar(3000,1,1).getTimeInMillis();
		Long birtDay = player.getBirthday().getTime();
		if(player.getName()== null || player.getName().isEmpty() || player.getName().length() >12
				|| player.getTitle().length() >30 || player.getTitle() == null ||  player.getTitle().isEmpty()
				|| player.getExperience() <= -1 || player.getExperience() > 10000000
				|| player.getRace() == null || player.getProfession() == null || player.getBirthday() == null
				|| birtDay <= -1 || (birtDay < begin || birtDay > end))
		{
			return false;
		}
		return true;
	}

	public boolean checkBirthday(Date date){
		if(date == null) {
			return true;
		}
		Long birtDay = date.getTime();
		Long begin = new GregorianCalendar(2000,1,1).getTimeInMillis();
		Long end = new GregorianCalendar(3000,1,1).getTimeInMillis();

		if(birtDay < 0 || (birtDay < begin || birtDay > end)) {
			return false;
		}
		return true;
	}

	public boolean checkExperience(Integer exp) {
		if(exp == null) {
			return  true;
		}
		if(exp < 0 || exp > 10_000_000 ) {
			return false;
		}
		return true;
	}

	public boolean checkTitle(String title){
		if(title.length() >30)	{
			return false;
		}
		return true;
	}

	//высчитываем текущий  уровень
	public int countLevel(Player player){
		return (int) ((Math.sqrt(2500+200*player.getExperience()) - 50)/100);
	}

	//высчитываем опыт
	public int countUntilNextLevel(Player player){
		return 50* (countLevel(player)+1)*(countLevel(player)+2)-player.getExperience();
	}

	public boolean isValidId(String playerId){
		Long id;
		try {
			id = Long.parseLong(playerId);
		} catch (NumberFormatException e) {
			return false;
		}
		return id > 0;
	}

	public  boolean isEmptyBody (Player player){
		return player.getName() == null
				&& player.getTitle() == null
				&& player.getRace() == null
				&& player.getProfession() == null && player.getBirthday() == null
				&& player.getBanned() == null;
	}
}
