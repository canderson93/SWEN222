package model.tiles;

/**
 * The tile object used to represent the hallway of the cluedo game
 * @author Carl
 *
 */
public class Hall extends Tile{
	
	public Hall(int x, int y){
		super(' ', x, y);
	}

	@Override
	public boolean canMoveTo(Tile tile) {
		//Check the room is one the hall is allowed to move to
		if (tile instanceof Room || tile instanceof Warp){return false;}
		if (tile instanceof Door){return tile.canMoveTo(this);}
		
		return Math.abs(this.x-tile.x) + Math.abs(this.y-tile.y) == 1;
	}
}
