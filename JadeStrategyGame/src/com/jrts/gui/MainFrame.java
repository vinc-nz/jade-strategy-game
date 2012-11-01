package com.jrts.gui;
import jade.core.Runtime;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jrts.O2Ainterfaces.IUnit;
import com.jrts.O2Ainterfaces.Team;
import com.jrts.common.GameConfig;
import com.jrts.environment.Cell;
import com.jrts.environment.CellType;
import com.jrts.environment.Floor;
import com.jrts.environment.Position;
import com.jrts.environment.World;


/**
 * Graphic interface, allow to create the environment configuration 
 * and display the sequence of actions performed by the agent
 *
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

	protected static MainFrame mainFrame;	
	
	protected WorldViewPanel worldViewPanel;
	protected Floor floor;
	
	protected IUnit selectedUnit = null;

	protected JPanel infoPanel;

	private JLabel labelTeam, labelType, labelPosition, labelEnergy, labelAction;
	
	//public static int treeClick = 0;
	
	public static String treeClick = "Add Tree";
	public static String foodClick = "Add Food";
	public static String emptyCellClick = "Add Empty Cell";
	public static String selectionClick = "Get Cell/Unit Info";
	
	public String clickType = selectionClick;
	
	List<Team> teams;
	
	JPanel topPanel;
	
	public static void start(Floor floor, List<Team> teams)
	{
		mainFrame = new MainFrame(floor, teams);
	}
	
	protected MainFrame(Floor floor, List<Team> teams) {
		super();
		
		this.teams = teams;
		
//		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				Runtime.instance().shutDown();
				dispose();
				//TODO killare tutti gli agenti e chiudere la runtime
				System.exit(0);
			}
		});
		
		this.worldViewPanel = new WorldViewPanel(floor);
		this.floor = floor;
		
		topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		for (Team t : teams) {
			topPanel.add(new TeamPanel(t), BorderLayout.WEST);
		}
			    
		JSlider speed = new JSlider(JSlider.HORIZONTAL, GameConfig.MIN_REFRESH_TIME, 
				GameConfig.MAX_REFRESH_TIME, GameConfig.REFRESH_TIME);
		speed.setPaintTicks(true);
		speed.setPaintLabels(true);
		speed.setBorder(BorderFactory.createTitledBorder("Speed"));
		speed.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
			    JSlider source = (JSlider)ce.getSource();
			    if (!source.getValueIsAdjusting())
			        GameConfig.REFRESH_TIME = (int)source.getValue();
			}
		});

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) { MainFrame.this.clickType = e.getActionCommand(); }
		};
		JRadioButton tree = new JRadioButton(treeClick);
		tree.addActionListener(al);
		tree.setActionCommand(treeClick);
		JRadioButton food = new JRadioButton(foodClick);
		food.addActionListener(al);
		food.setActionCommand(foodClick);
		JRadioButton emptyCell = new JRadioButton(emptyCellClick);
		emptyCell.setActionCommand(emptyCellClick);
		emptyCell.addActionListener(al);
		JRadioButton selection = new JRadioButton(selectionClick, true);
		selection.setActionCommand(selectionClick);
		selection.addActionListener(al);
	    ButtonGroup group = new ButtonGroup();
	    group.add(tree);
	    group.add(food);
	    group.add(emptyCell);
	    group.add(selection);
		
		JPanel settingsPanel = new JPanel(new GridLayout(5, 1));
		settingsPanel.add(speed);
		settingsPanel.add(tree);
		settingsPanel.add(food);
		settingsPanel.add(emptyCell);
		settingsPanel.add(selection);
		
		this.infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createTitledBorder("Informations"));
		infoPanel.setLayout(new GridLayout(5, 2));
		infoPanel.add(new JLabel("Team:"));
		labelTeam = new JLabel("None");		
		infoPanel.add(labelTeam);
		infoPanel.add(new JLabel("Type:"));
		labelType = new JLabel("cell");
		infoPanel.add(labelType);
		infoPanel.add(new JLabel("Position:"));
		labelPosition = new JLabel("10, 10");
		infoPanel.add(labelPosition);
		infoPanel.add(new JLabel("Energy:"));
		labelEnergy = new JLabel(String.valueOf(500));
		infoPanel.add(labelEnergy);
		infoPanel.add(new JLabel("Action:"));
		labelAction = new JLabel("nothing");
		infoPanel.add(labelAction);
		Dimension d = new Dimension(200, 50);
		infoPanel.setPreferredSize(d);
		infoPanel.setSize(d);
		infoPanel.setMinimumSize(d);
		infoPanel.setMaximumSize(d);
			
		JPanel leftPanel = new JPanel();
		leftPanel.setPreferredSize(d);
		leftPanel.setSize(d);
		leftPanel.setMinimumSize(d);
		leftPanel.setMaximumSize(d);
		
		leftPanel.add(this.infoPanel);
		leftPanel.add(settingsPanel);
		
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(leftPanel, BorderLayout.WEST);
		
		getContentPane().add(worldViewPanel, BorderLayout.CENTER);
		
		pack();

		//setSize((floor.getCols()+1)*ImageLoader.iconSize, (floor.getRows()+4)*ImageLoader.iconSize*3/5);
		this.setVisible(true);

		class RefreshGUI implements Runnable{
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
					MainFrame.this.repaint();
				}
			}
		}

		new Thread(new RefreshGUI()).start();
	}
	
	@Override
	public void repaint(){
		/** update world panel */
		worldViewPanel.update();
				
		/** update all teampanels */
		for (int i = 0; i < topPanel.getComponentCount(); i++) {
			if (topPanel.getComponent(i) instanceof TeamPanel)
				((TeamPanel) topPanel.getComponent(i)).update();
		}
		
		/** follow the selected unit */
		if(selectedUnit != null) {
			showAgentInfo();
			setSelectedCell(selectedUnit.getPosition().getRow(), selectedUnit.getPosition().getCol());
		}
		
		super.repaint();
	}
	
	protected void showCellInfo(int i, int j, int energy)
	{
		Cell cell = floor.get(new Position(i, j));
		
		try{
			if(cell.getId().isEmpty() || cell.getId().trim().equals(""))
				labelTeam.setText("None");
			else
				labelTeam.setText(cell.getId());
		} catch(NullPointerException e){ labelTeam.setText("None"); }
		
		if(cell.getType().equals(CellType.CITY_CENTER)) 
			labelType.setText("Town Center");
		else if(cell.getType().equals(CellType.FOOD)) 
			labelType.setText("Food");
		else if(cell.getType().equals(CellType.WOOD)) 
			labelType.setText("Wood");
		else if(cell.getType().equals(CellType.FREE)) 
			labelType.setText("Ground");
		else  
			labelType.setText("Cell");
		
		labelPosition.setText(i + ", " + j);
		labelEnergy.setText(String.valueOf(energy));
		labelAction.setText("nothing");
	}
	
	protected void showAgentInfo()
	{
		//IUnit  World.getInstance().getCell(selectedUnit.getPosition()).getUnit();
		labelTeam.setText(selectedUnit.getTeamName());
		if(selectedUnit.getType().equals(CellType.WORKER))
			labelType.setText("Worker");
		else if(selectedUnit.getType().equals(CellType.SOLDIER))
			labelType.setText("Soldier");
		labelPosition.setText(selectedUnit.getPosition().getCol() +", "+ selectedUnit.getPosition().getRow());
		labelEnergy.setText("" + selectedUnit.getLife());
		labelAction.setText(selectedUnit.getStatus());
	}
	
	public static MainFrame getInstance(){
		return mainFrame;
	}

	public void setSelectedCell(int row, int col) {
		for (int i = 0; i < floor.getRows(); i++) {
			for (int j = 0; j < floor.getCols(); j++) {
				if (row == i && col == j) {
					worldViewPanel.labelMatrix[i][j].setSelected(true);
				} else {
					worldViewPanel.labelMatrix[i][j].setSelected(false);
				}
			}
		}
	}
}