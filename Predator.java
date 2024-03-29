package sim.app.pvp;

import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class Predator extends Animal implements Steppable{

private int oldAge = 1000;
private double deathRate = .0001;
private double repoRate = .00001;
private double agingDeathMod = 1.0001;
private double hungerDeathMod = 1.01;
private int repAge = 56;
private int deathRandNum = 10000;
private int repRandNum = 10000;
private Bag seen;
private int daysLM = 28;


	Predator(SimState state){
		int directionNum= state.random.nextInt(3);
		if(directionNum == 0)
			direction = 0;
		else if(directionNum == 1)
			direction = 1;
		else if (directionNum == 2)
			direction = 2;
		else
			direction = 3;
		vP = new VisualProcessor(state);
	}
	@Override
	public void step(SimState state) {
		// TODO Auto-generated method stub
		super.step(state);
		//System.out.println("Last Meal: " + lastMeal + " timesteps");

		//Visual Processor
		this.vision(state, grid);
		
		//Death Calculations
		if(this.iDie(state))
			return;
		
		//Reproduction Calculations
		if(this.iReproduce(state))
			return;
		
		//Will I eat?
		/*if(this.willEat(grid, state))
			return;
		*/

		
		
	}

	//Method that allows Predator to kill its Prey
	public void eat(Object p){
		
		if(p.getClass().equals(Prey.class)){
			Prey prey = (Prey) p;
			if(prey.isDiseased())
				this.setDisease(true);
			lastMeal = 0;
			//System.out.println(this + " ate " + p);
			grid.remove(p);
		}
			
	}
	
	
	//Method that "kills" the Predator by removing it from the grid
	public boolean iDie(SimState state){
		
		 //older = more likely to die
		 if(age>oldAge)
		 	deathRate = deathRate * agingDeathMod;
		 	
		 //Last meal, more likely to die
		 if(lastMeal > daysLM)
			deathRate = deathRate * hungerDeathMod;
		//System.out.println("deathRate: " + deathRate);
			
		 // Death Rate
		double d = state.random.nextInt(deathRandNum);
		double death = d/deathRandNum;
		
		assert(d >= 0 && death >=0);
		
		//System.out.println("d: " + d + " death: " + death);
		if(death < deathRate){
			grid.remove(this);
			return true;
		}
		return false;
	}
	
	public boolean iReproduce(SimState state){
		// Reproduction Rate
		double r = state.random.nextInt(repRandNum);
		double repo = r/repRandNum;
				
		assert (r >= 0 && repo >= 0);
				
		if(repo <= repoRate && age >= repAge){
			this.reproduce(state);
			return true;
		}
		return false;
	}
	
	public boolean willEat(SparseGrid2D grid, SimState state){
		
		//Eating Prey on the same location
		assert(grid.getObjectsAtLocationOfObject(this) !=null);
		
		
		System.out.println(grid.getObjectsAtLocationOfObject(this).size());
			int gridNum = grid.getObjectsAtLocationOfObject(this).size();
			
			assert(gridNum != 0);
			
			for(int i = 0; i < gridNum; i++){
				Object obj = (grid.getObjectsAtLocationOfObject(this)).get(i);
				if(obj.getClass().equals(Prey.class) && obj != null){
					this.eat(obj);
					return true;
				}// end of if
			}// end of for loop
		
		
		return false;
	}
	//Method that allows Predator to duplicate
	public void reproduce(SimState state){
		
		
		Predator p = new Predator(state);
		
		grid.setObjectLocation(p, grid.getObjectLocation(this));
		state.schedule.scheduleRepeating(p);
	}
	public void vision(SimState state, SparseGrid2D grid){
		//System.out.println("Direction: " + direction);
		//Visual Processor				
		
		Int2D cord = grid.getObjectLocation(this);
		assert(vP != null && cord != null);
		assert(direction!=0);
		//System.out.println(this + "was at location: " + cord);
		if(cord != null){
		seen = vP.sight(cord.x, cord.y, state, direction);
		Bag locations = new Bag();
		
		//Testing Print Statements
		for(int s = 0; s < seen.size(); s++){
			Object j = seen.get(s);
			//System.out.print(s + "saw " + seen.get(s));
			Int2D obLoc = grid.getObjectLocation(j);
			locations.add(obLoc);
			//System.out.println(" at location:" + obLoc);
			//if(j.equals(Prey.class))
				//System.out.println("****" + seen.get(s));
			}// end of for loop
		
		this.behaviorProb(locations, seen);
		
		//Move every timestep
		super.move(grid, state);}
	}// end of vision
	
	public void behaviorProb(Bag locs, Bag seen){
	
		int[] newProb = behavior.updateProbPred(locs, seen, defaultProb);
		
		actualProb = newProb;
	}
}
