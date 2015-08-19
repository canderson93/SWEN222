package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import model.Board;
import model.Board.Direction;
import model.Characters;
import model.Weapons;
import model.cards.Card;
import model.cards.CharacterCard;
import model.cards.RoomCard;
import model.cards.WeaponCard;
import model.tiles.Room;
import model.tiles.Tile;

/**
 * The main class for controlling game logic
 * 
 * @author Chloe
 * 
 */
public class Game {

	private Board board;
	private boolean gameComplete = false;
	private Player winner = null;

	List<Card> solution = new ArrayList<Card>();
	List<Player> players = new ArrayList<Player>();

	// Complete list of all cards in the deck
	List<WeaponCard> weapons = new ArrayList<WeaponCard>();
	List<CharacterCard> characters = new ArrayList<CharacterCard>();
	List<RoomCard> rooms = new ArrayList<RoomCard>();

	int roll;
	Player current;

	public Game(String filename, int numPlayers) {
		this.board = Board.parseBoard(filename);

		loadCards();
		createPlayers(numPlayers);
		this.current = this.players.get(0);
		dealRemainder();
	}

	/**
	 * Load all of the cards into lists and add three random cards to the
	 * solution. Add the remainder to the rest of the deck list
	 *
	 */
	private void loadCards() {

		//Create weapon cards
		for (Weapons w : Weapons.values()) {
			this.weapons.add(new WeaponCard(w.name()));
		}

		//Create character cards
		for (Characters c : Characters.values()) {
			this.characters.add(new CharacterCard(c.name()));
		}
		
		//Create room cards
		List<Room> roomObj = board.getRooms();
		for (Room r : roomObj) {
			if (r.getName() != "Blank") {
				rooms.add(new RoomCard(r.getName()));
			}
		}
		
		//Extract solution cards
		createSolution(characters);
		createSolution(weapons);
		createSolution(rooms);
	}

	/**
	 * Randomly generate a card from each of the three sets of cards
	 *
	 * @param cards
	 */
	private void createSolution(List<? extends Card> cards) {
		Card randomItem = cards.get(new Random().nextInt(cards.size()));
		this.solution.add(randomItem);
	}

	private void createPlayers(int numPlayers) {
		// create a list of all of the character cards
		List<Card> dealingPlayers = new ArrayList<Card>();
		for (int i = 0; i < 6; i++) {
			dealingPlayers.add(this.characters.get(i));
		}

		Map<Character, Player> tokens = new HashMap<Character, Player>();

		// create players with random characters chosen from the list of
		// characters
		for (int i = 0; i < numPlayers; i++) {
			String temp = dealingPlayers.remove(
					new Random().nextInt(dealingPlayers.size())).getValue();

			// create a list of all of the cards (including the solution)
			List<Card> allCards = new ArrayList<Card>(this.weapons);
			allCards.addAll(this.rooms);
			allCards.addAll(this.characters);

			// loop through the last name until we find an unused token
			int j = temp.indexOf('_') + 1;
			while (tokens.containsKey(temp.charAt(j))) {
				j++;
			}

			// create specified number of players with their random character
			// card and give them a list of all of the possible cards
			Player p = new Player(temp, temp.charAt(j), allCards);

			// add each player to the board and a list of all of the players
			this.board.addPlayer(p);
			players.add(p);
		}
	}

	/**
	 * Randomly assigns a card from the rest of the deck to each player until
	 * there are none left
	 *
	 * */
	private void dealRemainder() {
		// Join all the card collections to make the rest of the deck
		List<Card> restOfDeck = new ArrayList<Card>();
		restOfDeck.addAll(characters);
		restOfDeck.addAll(weapons);
		restOfDeck.addAll(rooms);

		// remove the solution before dealing the cards out		
		restOfDeck.removeAll(this.solution); 

		// Deal out the cards
		while (!restOfDeck.isEmpty()) {
			for (Player p : this.players) {
				if (!restOfDeck.isEmpty()) {
					p.addCard(restOfDeck.remove(new Random().nextInt(restOfDeck
							.size())));
				}
			}
		}
	}

	/**
	 * Generates a random roll and updates the current player
	 *
	 */
	public void nextRound() {

		this.roll = new Random().nextInt(10) + 2;
		this.current = nextPlayer();
	}

	/**
	 * Returns the next viable player
	 *
	 * @return Player
	 */
	private Player nextPlayer() {

		this.current = this.players.get((players.indexOf(current) + 1)
				% players.size());
		
		// keep a count to avoid an infinite loop in the case there are
		// no players left in the game
		int count = this.players.size() - 1; 
		
		// while the next player in the round is out of the game, go to the next player
		while (!current.isPlaying() && count != 0) {
			this.current = this.players.get((players.indexOf(current) + 1)
					% players.size());
			count--;
		}
		if (count == 0) {
			this.gameComplete = true;
		}
		return this.current;
	}

	/**
	 * Keeps track of how many rolls the player has left, and then moves the
	 * player in the direction they desire if valid, returning appropriate
	 * status
	 *
	 * @param direction
	 * @return String
	 */
	public boolean move(Tile tile) {
		
		List<Tile> path = board.findPath();
		if(this.roll < path.size()){ return false; } //not a high enough roll for user's choice
		for(int i = 0; i < path.size(); i++){
			if(i < path.size() - 1){ board.move(this.current, path.get(i), false); }
			else { board.move(this.current, path.get(i), true); } //warn move method that this is the destination tile
		}
	}

	/**
	 * This method allows a player to guess the solution (three cards) and
	 * return true if their correct, otherwise return false and remove them from
	 * the game
	 *
	 * @param String
	 *            room, String character, String weapon
	 */
	public boolean accusation(String r, String c, String w) {

		RoomCard room = new RoomCard(r);
		CharacterCard character = new CharacterCard(c);
		WeaponCard weapon = new WeaponCard(w);
		if (this.solution.contains(room) && this.solution.contains(character)
				&& this.solution.contains(weapon)) {
			this.gameComplete = true;
			this.winner = this.current;
			return true;
		} else {
			this.current.setPlaying(false);

			// Check we still have active players in the game
			boolean stillPlaying = false;
			for (Player p : players) {
				if (p.isPlaying()) {
					stillPlaying = true;
				}
			}

			if (!stillPlaying) {
				this.gameComplete = true;
			}

			return false;
		}
	}

	/**
	 * Handles the player suggestion, and returns a card that disproves it from
	 * the hand of the the clockwise player.
	 * 
	 * @param c character enum string
	 * @param w weapon enum string
	 * @param r room object
	 * @return
	 */
	public Card suggestion(String c, String w, Room r) {
		//check the player is actually standing in the room
		if (this.current.getTile() != r){return null;}

		CharacterCard character = new CharacterCard(c);
		WeaponCard weapon = new WeaponCard(w);
		RoomCard room = new RoomCard(r.getName());
		List<Card> suggestion = new ArrayList<Card>();

		Weapons suggestedWeapon = Weapons.valueOf(w);

		// Move the weapon to the room
		r.addWeapon(suggestedWeapon);
		for (Room rooms : board.getRooms()) {
			if (rooms.containsWeapon(suggestedWeapon)) {
				rooms.removeWeapon(suggestedWeapon);
				break;
			}
		}

		// Look for a player matching the character, and then move them to the
		// room as well
		for (Player p : players) {
			if (p.getName().equals(c)) {
				Tile t = p.getTile();
				r.setPlayer(p);
				t.removePlayer(p);
				break;
			}
		}
		
		this.current.setLastSuggestion(r);

		suggestion.add(character);
		suggestion.add(weapon);
		suggestion.add(room);

		List<Card> found = new ArrayList<Card>();
		int loop = this.players.indexOf(this.current);
		int i = loop + 1;

		// Loop through the players until we find one who has one of the suggestion
		// cards in their hand, or we end back up at the original player
		while (i % players.size() != loop && found.isEmpty()) {
			for (Card card : suggestion) {
				if (this.players.get(i % players.size()).getHand()
						.contains(card)) {
					found.add(card);
				}
			}

			i++;
		}

		// randomly select one of the found cards (in case of multiple) and return it
		if (!found.isEmpty()) {
			Card rtn = found.get(new Random().nextInt(found.size()));
			this.current.getUnseenCards().remove(rtn);
			return rtn;
		}

		return null;
	}

	/*
	 * Getters for game logic
	 */

	public Board getBoard() {return this.board;}
	public List<Player> getPlayers() {return this.players;}
	public int getRoll() {return this.roll;}
	public Player getCurrent() {return this.current;}
	public Player getWinner() {return this.winner;}
	public boolean isFinished() {return this.gameComplete;}
	public List<WeaponCard> getWeaponCards() {return this.weapons;}
	public List<CharacterCard> getCharacterCards() {return this.characters;}
	public List<RoomCard> getRoomCards() {return this.rooms;}
	public List<Card> getSolution() {return solution;}

	/*
	 * Setters
	 */
	public void setRoll(int i) { this.roll = i; }
	public void setSolution(List<Card> cards) {this.solution = cards;}
	public void setCurrent(Player p) {this.current = p;}
}