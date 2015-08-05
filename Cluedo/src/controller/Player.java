package controller;
import java.util.ArrayList;
import java.util.List;

import model.cards.*;
import model.tiles.Room;
import model.tiles.Tile;

/**
 * A player class for the Cluedo Game
 * @author Carl and Chloe
 *
 */
public class Player {
	private final String character;
	private final char key;
	private List<Card> hand;
	private List<Card> unseenCards; //cards the player hasn't seen, this is updated throughout the game
	private boolean playing;
	private Room lastSuggestion;
	private Tile tile; //The tile the player is currently standing on
	
	public Player(String name, char key, List<Card> allCards){
		this.character = name;
		this.key = key;
		this.playing = true;
		hand = new ArrayList<Card>();
		this.unseenCards = allCards;
		this.lastSuggestion = null;
	}
	
	public void addCard(Card c){ 
		this.hand.add(c); 
		this.unseenCards.remove(c);
	}
	
	public void showCard(Card c){ 
		this.unseenCards.remove(c);
	}
	
	/**
	 * Getters
	 */
	public String getName(){return this.character; }
	public char getKey(){ return this.key; }
	public Tile getTile(){ return this.tile; }
	public boolean isPlaying(){return this.playing; }
	public List<Card> getUnseenCards(){ return this.unseenCards; }
	public List<Card> getHand(){ return this.hand; }
	public Room getLastSuggestion(){ return this.lastSuggestion; }
	
	/**
	 * Setters
	 */
	public void setTile(Tile t){ this.tile = t; }
	public void setPlaying(boolean b) { this.playing = b; }
	public void setLastSuggestion(Room r){this.lastSuggestion = r;}
}
