//April 1, 2014

public class Unit{
	private int x, y;
	private int attackRng, sightRng, alarmRng, chasingRng;
	private int moveSpeed, attackSpeed;
	private int hp,maxHp,mana,maxMana;
	private int manaRegen;
	private Attack[] attacks;
	private SpriteList sprites;
	private int attackLvl, armourLvl;
	private int preAtkDelay, postAtkDelay;

	
	public Unit(String type, int x, int y){
		this.x = x;
		this.y = y;
		if (type.equals("Marine")){
			attackRng = 6;
			sightRng = 10;
			alarmRng = 8;
			chasingRng = 5;
			moveSpeed = 8;
			attackSpeed = 6;
			
			
			//alarmRng = 10;
			//moveSpeed
			//attackSpeed
			//hp
			//ap
			//attackLvl
			//armourLvl
		}
	}
	
	public void update(){
		
	}
	
	public void draw(Graphics g){
		
	}
}