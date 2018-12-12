import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
//import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Wavesim extends JFrame implements ActionListener, MouseListener
{
	//params for animation and interface
	final int DELAY_IN_MILLISEC = 20;
	final static int MAX_WIDTH = 1000;
	final static int MAX_HEIGHT = 300;
	final static int NUM_BUTTONS = 1;
	Timer clock = new Timer(DELAY_IN_MILLISEC, this);


	//num surfers and num waves
	final static int NUM_SURFERS = 10;

	//start and stop the simulation
	boolean running = false;

	// buttons
	Button playButton = new Button(200, 50, 100, 22, "play/pause");
	//Button resetButton = new Button(300, 50, 100, 22, "reset");
	Button [] buttonList = new Button [NUM_BUTTONS];


	//graph
	//Graph graph = new Graph(640, 500, 1000, 200);

	//stat lists
	//ArrayList<Integer> numSList = new ArrayList<Integer>();

	Surfer [] surferList = new Surfer [NUM_SURFERS];


	List<Wave> waveList = new ArrayList<Wave>();

	final static int TAKE_OFF_ZONE_Y = 100;
	final static int END_ZONE_Y = MAX_HEIGHT - 50;

	public int time = 0;

	
	public static void main (String [] args)
	{
		Wavesim mg = new Wavesim();

		// Register listeners
		mg.addMouseListener(mg);
	}



	public Wavesim()
	{
		// buttons
		buttonList[0] = playButton;
		//buttonList[1] = resetButton;

		Random rand = new Random();

		//initialize surfers
		for(int i = 0; i < NUM_SURFERS; i ++)
		{

			int x = MAX_WIDTH/3 + i * 50;
			int y = TAKE_OFF_ZONE_Y;
			int surferId = i;

			//TODO change the skill distribution
			int skill = (int)((rand.nextGaussian() * 25) + 50);
			while(skill < 0 || skill > 100)
			{
				skill = (int)((rand.nextGaussian() * 25) + 50);
			}

			//int [] prefList = mapSkillToPref(skill);
//			for(int j = 0; j < prefList.length; j ++)
//			{
//				//TODO add preference distributions for surfers
//				prefList[j] = (int)(Math.random()*10);
//			}





			surferList[i] = new Surfer(x, y, surferId, skill);
		}

		//initialize wave
		//(int yIn, int idIn , int qualityIn)
		//waveList.add(new Wave(0, 1, generateWaveQuality()));




		clock.start();
		setSize(MAX_WIDTH, MAX_HEIGHT);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}


	//TODO fill in code here to make wave quality according to some distribution
	public int generateWaveQuality(){
		return (int)(Math.random()*100);
	}

	public void reset()
	{

		surferList = new Surfer [NUM_SURFERS];
		waveList = new ArrayList<Wave>();
		buttonList = new Button [NUM_BUTTONS];

		buttonList[0] = playButton;
		//buttonList[1] = resetButton;

		clock.restart();
		setSize(MAX_WIDTH, MAX_HEIGHT);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}


	//TODO pick a surfer according to their utility function
	public Surfer pickSurfer(List<Surfer> inZoneList, Wave w)
	{
		//random allocation
		//return inZoneList.get((int)(Math.random()*inZoneList.size()));

		//pick surfer with best skill (current system)
		Surfer maxSkill = inZoneList.get(0);
		for(int i = 1; i < inZoneList.size(); i++)
		{
			if(inZoneList.get(i).skillLevel > maxSkill.skillLevel)
			{
				maxSkill = inZoneList.get(i);
			}
		}
		return maxSkill;

		//pick surfer that gains the most utility
//		Surfer maxUtil = inZoneList.get(0);
//		for(int i = 1; i < inZoneList.size(); i++)
//		{
//			if(inZoneList.get(i).calculateUtilityForSurferGivenWave(w.quality) > maxUtil.calculateUtilityForSurferGivenWave(w.quality))
//			{
//				maxUtil = inZoneList.get(i);
//			}
//		}
//		return maxUtil;

	}

	public Wave makeNewWave()
	{
		Wave newWave = new Wave(0, generateWaveQuality());
		return newWave;
	}

	public void printUtility()
	{
		//aggregate surfer utilities and print
		int utility = 0;
		for(int i = 0; i < NUM_SURFERS; i++)
		{
			utility += surferList[i].utility;
		}
		System.out.println(utility);
	}

	public void printNumWavesRidden()
	{
		System.out.println("Wave frequencies");
		for(int i = 0; i < NUM_SURFERS; i++)
		{
			System.out.println("Skill: " + surferList[i].skillLevel + " NumWaves: "+ surferList[i].numWavesRidden);
		}
		System.out.println(" ");
	}

	/**
	 * updates stuff each clock tick
	 */
	public void actionPerformed(ActionEvent arg0) 
	{

		if(playButton.getClicked())
		{



			//make a list of surfers in the takeoffzone
			List<Surfer> inZoneList = new ArrayList<Surfer>();
			for(int i = 0; i < NUM_SURFERS; i ++)
			{
				if(surferList[i].waiting)
				{
					inZoneList.add(surferList[i]);
				}
			}

			// if there is a wave in the takeoff zone, pick a suffer and assign that wave to them
			for(int i = 0; i < waveList.size(); i++)
			{
				Wave w = waveList.get(i);
				if(w.y == TAKE_OFF_ZONE_Y)
				{
					Surfer s = pickSurfer(inZoneList, w);
					s.catchWave(w);
					printUtility();
				}
				else if(w.y == END_ZONE_Y + 5)
				{
					waveList.remove(w);
				}
			}



			//move the waves
			waveList.forEach(w -> {
				w.move();
			});

			// update surfers (update states and move them)
			for(int i = 0; i < NUM_SURFERS; i++)
			{
				if(surferList[i].y == END_ZONE_Y && surferList[i].surfing)
				{
					surferList[i].getOffWave();
				}
				else if(surferList[i].paddling && surferList[i].y == TAKE_OFF_ZONE_Y)
				{
					surferList[i].startWaiting();
				}
				surferList[i].move();
			}


			//randomly decide to make a new wave at a given interval
			if(time%(80) == 0 && Math.random() > .2)
			{
				Wave w = makeNewWave();
				waveList.add(w);
			}

			time += 1;


		}
		else
		{
			time = 0;
		}

		repaint();
	}

	public void mouseClicked(MouseEvent e) 
	{
		int x = e.getX();
		int y = e.getY();

		if(x > playButton.x && x < playButton.x + playButton.width && y > playButton.y && y < playButton.y + playButton.height)
		{
			if(running)
			{
				printNumWavesRidden();
			}
			running = !running;
			playButton.setClicked(running);
		}
//		else if(x > resetButton.x && x < resetButton.x + resetButton.width && y > resetButton.y && y < resetButton.y + resetButton.height)
//		{
//			running = !running;
//			reset();
//		}


	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{

	}

	public void paint (Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0,0,MAX_WIDTH*3,MAX_HEIGHT*3);
		g.setColor(Color.blue);
		g.drawString("Matching Waves Simulator", (int)(MAX_WIDTH/2.2), 35);

		//draw the buttons
		for(int i = 0; i < NUM_BUTTONS; i++)
		{
			boolean raised = true;
			if(buttonList[i].getClicked() == true)
			{
				g.setColor(buttonList[i].clickedColor);
				raised = false;
			}
			else
			{
				g.setColor(buttonList[i].normalColor);
			}
			g.draw3DRect(buttonList[i].x, buttonList[i].y, buttonList[i].width, buttonList[i].height, raised);
			String s = buttonList[i].text;
			g.drawString(s, buttonList[i].x + 5, buttonList[i].y + 15);
		}

		//draw the surfers

		g.setColor(Color.green);
		for(int i = 0; i < NUM_SURFERS; i++)
		{
			if(surferList[i].skillLevel < 33)
			{
				g.setColor(Color.green);
			}
			else if(surferList[i].skillLevel < 66)
			{
				g.setColor(Color.cyan);
			}
			else
			{
				g.setColor(Color.red);
			}
			g.fillOval(surferList[i].getSurferX(), surferList[i].getSurferY(), 10, 10);
		}

		//draw the waves
		g.setColor(Color.blue);


		for(int i = 0; i < waveList.size(); i++)
		{
			Wave w = waveList.get(i);
			g.drawLine(0, w.getWaveY(), MAX_WIDTH, w.getWaveY());
		}

	}
}

class Surfer
{
	public int x;
	public int y;
	public int dx = 0;
	public int dy = 0;
	public int surferId;
	public int skillLevel;
	//public int [] preferenceDistribution;
	public int utility = 0;
	boolean waiting = true;
	boolean surfing = false;
	boolean paddling = false;
	public Wave surferWave = null;

	public int numWavesRidden = 0;


	public Surfer(int xIn, int yIn, int idIn, int skillIn)
	{
		x = xIn;
		y = yIn;
		surferId = idIn;
		//preferenceDistribution = prefDistIn;
		skillLevel = skillIn;
	}

	public void updateUtility(Wave w)
	{
//		//decide how much utility to add by seeing how many times the wave w quality appears in pref dist list
//		int x = 0;
//		for(int i = 0; i < preferenceDistribution.length; i ++)
//		{
//			if(preferenceDistribution[i] == w.quality)
//			{
//				x++;
//			}
//		}

		int waveQuality = w.quality;
		int x = calculateUtilityForSurferGivenWave(waveQuality);
		utility = utility + x;
	}

	//TODO this function can be update
	public int calculateUtilityForSurferGivenWave(int waveQuality)
	{
		int scalar = 10;
		if(waveQuality > skillLevel)
		{
			return 0;
		}
		return (int)(scalar*waveQuality/skillLevel);
	}

	public void catchWave(Wave w)
	{
		surferWave = w;
		dy = w.dy;
		updateUtility(w);
		waiting = false;
		surfing = true;
		paddling = false;
		numWavesRidden++;
	}

	public void getOffWave()
	{
		surferWave = null;
		dy = -dy;
		waiting = false;
		surfing = false;
		paddling = true;
	}

	public void startWaiting()
	{
		dy = 0;
		waiting = true;
		surfing = false;
		paddling = false;
	}

	public int getSurferX()
	{
		return x;
	}

	public int getSurferY()
	{
		return y;
	}

	public void move()
	{
		x = x + dx;
		y = y + dy;
	}



}

class Wave
{
	public int y;
	public int dy = 1;
	public int quality;



	public Wave(int yIn, int qualityIn)
	{
		y = yIn;
		quality = qualityIn;

	}

	public int getWaveY()
	{
		return y;
	}

	public void move()
	{
		y = y + dy;

	}

}




class Button
{
	public int x;
	public int y;
	public int width;
	public int height;
	boolean clicked = false;
	String text;
	public Color normalColor = Color.gray;
	public Color clickedColor = Color.blue;

	public Button(int xIn, int yIn, int widthIn, int heightIn, String textIn)
	{
		x = xIn;
		y = yIn;
		width = widthIn;
		height = heightIn;
		text = textIn;
	}

	public void setClicked(boolean cIn)
	{
		clicked = cIn;
	}

	public boolean getClicked()
	{
		return clicked;
	}
}

