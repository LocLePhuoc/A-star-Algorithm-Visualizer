package Presentation;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.Environment;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractEnvironment;
import aima.core.environment.eightpuzzle.BidirectionalEightPuzzleProblem;
import aima.core.environment.eightpuzzle.EightPuzzleBoard;
import aima.core.environment.eightpuzzle.ManhattanHeuristicFunction;
import aima.core.environment.eightpuzzle.MisplacedTilleHeuristicFunction;
import aima.core.search.framework.Node;
import aima.core.search.framework.SearchAgent;
import aima.core.search.framework.SearchForActions;
import aima.core.search.framework.problem.Problem;
import aima.core.search.framework.qsearch.BidirectionalSearch;
import aima.core.search.framework.qsearch.GraphSearch;
import aima.core.search.informed.AStarSearch;
import aima.core.search.informed.GreedyBestFirstSearch;
import aima.core.search.local.SimulatedAnnealingSearch;
import aima.core.search.uninformed.BreadthFirstSearch;
import aima.core.search.uninformed.DepthLimitedSearch;
import aima.core.search.uninformed.IterativeDeepeningSearch;
import aima.gui.swing.framework.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Graphical 8-puzzle game application. It demonstrates the performance of
 * different search algorithms. Additionally, users can make experiences with
 * human problem solving.
 * 
 * @author Ruediger Lunde
 */
public class EightPuzzleApp extends SimpleAgentApp {

	/** List of supported search algorithm names. */
	protected static List<String> SEARCH_NAMES = new ArrayList<String>();
	/** List of supported search algorithms. */
	protected static List<SearchForActions> SEARCH_ALGOS = new ArrayList<SearchForActions>();

	/** Adds a new item to the list of supported search algorithms. */
	public static void addSearchAlgorithm(String name, SearchForActions algo) {
		SEARCH_NAMES.add(name);
		SEARCH_ALGOS.add(algo);
	}

	public static EmptyEightPuzzleView mainView = new EmptyEightPuzzleView();

	public static EightPuzzleStateInfoView stateInfoView = new EightPuzzleStateInfoView();

	public static boolean isUsingManhattanHeuristic;
	public static boolean isUsingMisplacedHeuristic;
	public static boolean isGreedyBestFirst;

	static {
		addSearchAlgorithm("Greedy Best First Search (MisplacedTileHeursitic)",
				new GreedyBestFirstSearch(new GraphSearch(), new MisplacedTilleHeuristicFunction()));
		addSearchAlgorithm("Greedy Best First Search (ManhattanHeursitic)",
				new GreedyBestFirstSearch(new GraphSearch(), new ManhattanHeuristicFunction()));
		addSearchAlgorithm("AStar Search (MisplacedTileHeursitic)",
				new AStarSearch(new GraphSearch(), new MisplacedTilleHeuristicFunction()));
		addSearchAlgorithm("AStar Search (ManhattanHeursitic)",
				new AStarSearch(new GraphSearch(), new ManhattanHeuristicFunction()));
	}

	/** Returns an <code>EightPuzzleView</code> instance. */
	public AgentAppEnvironmentView createEnvironmentView() {
		return mainView;
	}

	/** Returns a <code>EightPuzzleFrame</code> instance. */
	@Override
	public AgentAppFrame createFrame() {
		return new EightPuzzleFrame();
	}

	/** Returns a <code>EightPuzzleController</code> instance. */
	@Override
	public AgentAppController createController() {
		return new EightPuzzleController();
	}

	// ///////////////////////////////////////////////////////////////
	// main method

	/**
	 * Starts the application.
	 */
	public static void main(String args[]) {
		new EightPuzzleApp().startApplication();
	}

	// ///////////////////////////////////////////////////////////////
	// some inner classes

	/**
	 * Adds some selectors to the base class and adjusts its size.
	 */
	protected static class EightPuzzleFrame extends AgentAppFrame {
		private static final long serialVersionUID = 1L;
		public static String ENV_SEL = "EnvSelection";
		public static String SEARCH_SEL = "SearchSelection";

		public JPanel treePanel;

		public EightPuzzleFrame() {
			treePanel = new JPanel();
			treePanel.setLayout(null);
			setTitle("Eight Puzzle Application");
			setSelectors(new String[] { ENV_SEL, SEARCH_SEL },
					new String[] { "Select Environment", "Select Search" });
			setSelectorItems(ENV_SEL, new String[] { "Three Moves", "Medium", "Extreme", "Random" }, 0);
			setSelectorItems(SEARCH_SEL, (String[]) SEARCH_NAMES.toArray(new String[] {}), 0);
			setEnvView(new EmptyEightPuzzleView());
			this.setSplitPaneResizeWeight(0.5);
			setSize(1000, 750);}
	}

	/**
	 * Displays the informations provided by a
	 * <code>EightPuzzleEnvironment</code> on a panel using an grid of buttons.
	 * By pressing a button, the user can move the corresponding tile to the
	 * adjacent gap.
	 */
	public static class EmptyEightPuzzleView extends AgentAppEnvironmentView {

		private Vector<Vector<Integer>> childCoord = new Vector<Vector<Integer>>();
		private Vector<Vector<Integer>> parentCoord = new Vector<Vector<Integer>>();

		//used for clearing the view
		private boolean isClear;

		EmptyEightPuzzleView () {
			setLayout(null);
			setPreferredSize(new Dimension(5000,5000));
			isClear = false;
		}

		@Override
		public void agentActed(Agent agent, Action action, Environment source) {

		}

		@Override
		public void agentAdded(Agent agent, Environment source) {
		}

		@Override
		public void paintComponent(Graphics g) {
			if (isClear) {
				childCoord.clear();
				parentCoord.clear();
				isClear = false;
			} else {
				if (childCoord.size() == 0) {

				} else {
					for (int i = 0; i < childCoord.size(); i++) {
						int xChild = childCoord.get(i).get(0);
						int yChild = childCoord.get(i).get(1);

						int xParent = parentCoord.get(i).get(0);
						int yParent = parentCoord.get(i).get(1);
						g.drawLine(xChild, yChild, xParent, yParent);
					}
				}
			}
		}

		public void drawTree2() {
			Border border = BorderFactory.createLineBorder(Color.YELLOW,10);
			Border goalStateBorder = BorderFactory.createLineBorder(Color.GREEN,10);

			int orderChosenFromFrontier = 0;
			int currentDepth = 0;
			while (orderChosenFromFrontier <= EightPuzzleTree.numberOfExpandedNodes) {
				int[] nodeToExpand = EightPuzzleTree.getCurrentExpandedNode(currentDepth,orderChosenFromFrontier);
				int x = nodeToExpand[0];  //depth
				int y = nodeToExpand[1];  //position in depth
				EightPuzzleView newView = EightPuzzleTree.tree.get(x).get(y).view;
				//only need to add root node
				if (orderChosenFromFrontier == 0){
					add(newView);
					newView.updateView(EightPuzzleTree.tree.get(x).get(y).node.getPathCost());
					newView.setSize(200, 200);
					newView.setLocation(400 * y + 100, 400 * x + 100);
				}
				if (orderChosenFromFrontier == EightPuzzleTree.numberOfExpandedNodes) {
					newView.setBorder(goalStateBorder);
				} else {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					newView.setBorder(border);
				}

				//draw child views
				if (x < EightPuzzleTree.tree.size() - 1) {
					for (int i = 0; i < EightPuzzleTree.tree.get(x + 1).size(); i++) {
						EightPuzzleTree.EightPuzzleTreeNode childNode = EightPuzzleTree.tree.get(x + 1).get(i);
						if (childNode.hasParent(y,x)) {
							EightPuzzleView childView = EightPuzzleTree.tree.get(x + 1).get(i).view;
							add(childView);
							childView.updateView(EightPuzzleTree.tree.get(x).get(y).node.getPathCost());
							childView.setSize(200, 200);
							childView.setLocation(400 * i + 100, 400 * (x + 1) + 100);

							int x1 = 400 * i + 200;
							int y1 = 400 * (x + 1) + 100;
							int x2 = 400 * y+ 200;
							int y2 = 400 * x + 300;
							Vector<Integer> newChildVector = new Vector<Integer>();
							newChildVector.add(x1);
							newChildVector.add(y1);
							Vector<Integer> newParentVector = new Vector<Integer>();
							newParentVector.add(x2);
							newParentVector.add(y2);
							childCoord.add(newChildVector);
							parentCoord.add(newParentVector);
							paintComponent(getGraphics());

							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				orderChosenFromFrontier++;
				if (x == currentDepth)
					currentDepth++;
			}
		}

		public void setClearView(boolean set) {
			isClear = set;
		}

		public Graphics getViewGraphics() {
			return getGraphics();
		}
	}

	protected static class EightPuzzleView extends AgentAppEnvironmentView implements ActionListener {
		private static final long serialVersionUID = 1L;
		public JButton[] squareButtons;
		JLabel upperLabel;
		JPanel lowerPanel;

		protected EightPuzzleView() {
			setLayout(new GridBagLayout());
			GridBagConstraints gbc  = new GridBagConstraints();
			Border defaultBorder = BorderFactory.createLineBorder(Color.BLACK);
			Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 30);

			setBorder(defaultBorder);
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			upperLabel = new JLabel();
			upperLabel.setHorizontalAlignment(SwingConstants.CENTER);
			upperLabel.setFont(f);
			add(upperLabel,gbc);

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			lowerPanel = new JPanel();
			lowerPanel.setLayout(new GridLayout(3, 3));
			add(lowerPanel,gbc);


			squareButtons = new JButton[9];
			for (int i = 0; i < 9; i++) {
				JButton square = new JButton(Integer.toString(i));
				square.setFont(f);
				square.addActionListener(this);
				squareButtons[i] = square;
				lowerPanel.add(square);
			}
		}

		public void updateView(double pathCost) {
			if (!isUsingManhattanHeuristic && !isUsingMisplacedHeuristic) {
				remove(0);
			} else {
				int h = 0;
				if (isUsingManhattanHeuristic) {
					h = calculateManhattanHeuristic();
				} else {
					h = calculateMisplacedTileHeuristic();
				}

				upperLabel.setText(Double.toString(h + ((isGreedyBestFirst) ? 0 : pathCost)));
			}
		}

		@Override
		public void setEnvironment(Environment env) {
			super.setEnvironment(env);
			showState();
		}

		/** Agent value null indicates a user initiated action. */
		@Override
		public void agentActed(Agent agent, Action action, Environment source) {
			showState();
			notify((agent == null ? "User: " : "") + action.toString());
		}

		@Override
		public void agentAdded(Agent agent, Environment source) {
			showState();
		}

		/**
		 * Displays the board state by labeling and coloring the square buttons.
		 */
		protected void showState() {
			int[] vals = ((EightPuzzleEnvironment) env).getBoard().getState();
			for (int i = 0; i < 9; i++) {
				squareButtons[i].setBackground(vals[i] == 0 ? Color.LIGHT_GRAY : Color.WHITE);
				squareButtons[i].setText(vals[i] == 0 ? "" : Integer.toString(vals[i]));
			}
		}

		public void copyStateFromNode(Node state) {
			EightPuzzleBoard boardState = (EightPuzzleBoard) state.getState();
			for (int i = 0 ;i < 9; i++) {
				int currentVal = boardState.getState()[i];
				squareButtons[i].setText(currentVal == 0 ? "" : Integer.toString(currentVal));
			}
		}

		/**
		 * When the user presses square buttons the board state is modified
		 * accordingly.
		 */
		@Override
		public void actionPerformed(ActionEvent ae) {
			stateInfoView.copyState(this);
			AgentAppFrame.rightPane.add(JSplitPane.TOP,stateInfoView);
		}

		public int calculateManhattanHeuristic() {
			int retVal = 0;
			int currentVal = 0;
			for (int i = 0; i < 9 ; i++) {
				if (!squareButtons[i].getText().equals("")) {
					switch (Integer.parseInt(squareButtons[i].getText())) {
						case 1:
							currentVal = Math.abs(i / 3 - 0) + Math.abs(i % 3 - 1);
							break;
						case 2:
							currentVal = Math.abs(i / 3 - 0) + Math.abs(i % 3 - 2);
							break;
						case 3:
							currentVal = Math.abs(i / 3 - 1) + Math.abs(i % 3 - 0);
							break;
						case 4:
							currentVal = Math.abs(i / 3 - 1) + Math.abs(i % 3 - 1);
							break;
						case 5:
							currentVal = Math.abs(i / 3 - 1) + Math.abs(i % 3 - 2);
							break;
						case 6:
							currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 0);
							break;
						case 7:
							currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 1);
							break;
						case 8:
							currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 2);
							break;
					}
				}
				retVal += currentVal;
			}
			return retVal;
		}

		public int calculateMisplacedTileHeuristic() {
			int retVal = 0;
			for (int i = 0; i < 9; i++) {
				if (!squareButtons[i].getText().equals("")) {
					switch (Integer.parseInt(squareButtons[i].getText())) {
						case 1:
							if (!((i / 3 == 0) && (i % 3 == 1)))
								retVal += 1;
							break;
						case 2:
							if (!((i / 3 == 0) && (i % 3 == 2)))
								retVal += 1;
							break;
						case 3:
							if (!((i / 3 == 1) && (i % 3 == 0)))
								retVal += 1;
							break;
						case 4:
							if (!((i / 3 == 1) && (i % 3 == 1)))
								retVal += 1;
							break;
						case 5:
							if (!((i / 3 == 1) && (i % 3 == 2)))
								retVal += 1;
							break;
						case 6:
							if (!((i / 3 == 2) && (i % 3 == 0)))
								retVal += 1;
							break;
						case 7:
							if (!((i / 3 == 2) && (i % 3 == 1)))
								retVal += 1;
							break;
						case 8:
							if (!((i / 3 == 2) && (i % 3 == 2)))
								retVal += 1;
							break;
					}
				}
			}
			return retVal;
		}
	}

	/**
	 * Defines how to react on standard simulation button events.
	 */
	protected static class EightPuzzleController extends AgentAppController {

		protected EightPuzzleEnvironment env = null;
		protected SearchAgent agent = null;
		protected boolean dirty;

		/** Prepares next simulation. */
		@Override
		public void clear() {
			prepare(null);
		}

		/**
		 * Creates an eight puzzle environment and clears the current search
		 * agent.
		 */
		@Override
		public void prepare(String changedSelector) {
			AgentAppFrame.SelectionState selState = frame.getSelection();
			List<Object> selItems = selState.getItems();
			if (selItems.get(1).toString().contains("Misplaced")) {
				isUsingMisplacedHeuristic = true;
				isUsingManhattanHeuristic = false;
			} else if (selItems.get(1).toString().contains("Manhattan")) {
				isUsingManhattanHeuristic = true;
				isUsingMisplacedHeuristic = false;
			} else {
				isUsingManhattanHeuristic = false;
				isUsingMisplacedHeuristic = false;
			}

			if (selItems.get(1).toString().contains("Greedy")) {
				isGreedyBestFirst = true;
			} else {
				isGreedyBestFirst = false;
			}

			EightPuzzleBoard board = null;
			switch (selState.getIndex(EightPuzzleFrame.ENV_SEL)) {
			case 0: // three moves
				board = new EightPuzzleBoard(new int[] { 1, 2, 5, 3, 4, 0, 6, 7, 8 });
				break;
			case 1: // medium
				board = new EightPuzzleBoard(new int[] { 1, 4, 2, 7, 5, 8, 3, 0, 6 });
				break;
			case 2: // extreme
				board = new EightPuzzleBoard(new int[] { 0, 8, 7, 6, 5, 4, 3, 2, 1 });
				break;
			case 3: // random
				board = new EightPuzzleBoard(new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 });
				Random r = new Random(System.currentTimeMillis());
				for (int i = 0; i < 200; i++) {
					switch (r.nextInt(4)) {
					case 0:
						board.moveGapUp();
						break;
					case 1:
						board.moveGapDown();
						break;
					case 2:
						board.moveGapLeft();
						break;
					case 3:
						board.moveGapRight();
						break;
					}
				}
			}
			env = new EightPuzzleEnvironment(board);
			agent = null;
			dirty = false;
			frame.getEnvView().setEnvironment(env);
		}

		/**
		 * Creates a new search agent and adds it to the current environment if
		 * necessary.
		 */
		protected void addAgent() throws Exception {
			if (agent == null) {
				int pSel = frame.getSelection().getIndex(EightPuzzleFrame.SEARCH_SEL);
				Problem problem = new BidirectionalEightPuzzleProblem(env.getBoard());
				SearchForActions search = SEARCH_ALGOS.get(pSel);
				agent = new SearchAgent(problem, search);
				env.addAgent(agent);
			}
		}

		/** Checks whether simulation can be started. */
		@Override
		public boolean isPrepared() {
			return !dirty && (agent == null || !agent.isDone());
		}

		/** Starts simulation. */
		@Override
		public void run(MessageLogger logger) {
			logger.log("<simulation-log>");
			try {
				addAgent();
				while (!agent.isDone() && !frame.simulationPaused()) {
					Thread.sleep(500);
					env.step();
				}
			} catch (InterruptedException e) {
				// nothing to do...
			} catch (Exception e) {
				e.printStackTrace(); // probably search has failed...
			}
			logger.log(getStatistics());
			logger.log("</simulation-log>\n");
		}

		/** Executes one simulation step. */
		@Override
		public void step(MessageLogger logger) {
			try {
				addAgent();
				env.step();
			} catch (Exception e) {
				e.printStackTrace(); // probably search has failed...
			}
		}

		/** Updates the status of the frame after simulation has finished. */
		public void update(SimulationThread simulationThread) {
			if (simulationThread.isCanceled()) {
				frame.setStatus("Task canceled.");
			} else if (frame.simulationPaused()) {
				frame.setStatus("Task paused.");
			} else {
				frame.setStatus("Task completed.");
			}
		}

		/** Provides a text with statistical information about the last run. */
		private String getStatistics() {
			StringBuffer result = new StringBuffer();
			Properties properties = agent.getInstrumentation();
			Iterator<Object> keys = properties.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				String property = properties.getProperty(key);
				result.append("\n" + key + " : " + property);
			}
			return result.toString();
		}

		public void executeUserAction(Action action) {
			env.executeAction(null, action);
			agent = null;
			dirty = true;
			frame.updateEnabledState();
		}

		@Override
		public void calculateManhattanHeuristic() {
			int retVal = 0;
			int currentVal = 0;
			stateInfoView.resetViewBorderToInitial();
			Border borderCurrent = BorderFactory.createLineBorder(Color.RED,10);
			Border borderTarget = BorderFactory.createLineBorder(Color.GREEN,10);
			Border originalBorder = stateInfoView.squareButtons[0].getBorder();
			for (int i = 0; i < 9 ; i++) {
				stateInfoView.squareButtons[i].setBorder(borderCurrent);
				stateInfoView.squareButtons[stateInfoView.vals[i]].setBorder(borderTarget);
				switch (stateInfoView.vals[i]) {
					case 1:
						currentVal = Math.abs(i / 3- 0) + Math.abs(i % 3 - 1);
						break;
					case 2:
						currentVal = Math.abs(i / 3- 0) + Math.abs(i % 3 - 2);
						break;
					case 3:
						currentVal = Math.abs(i / 3- 1) + Math.abs(i % 3 - 0);
						break;
					case 4:
						currentVal = Math.abs(i / 3 - 1) + Math.abs(i % 3- 1);
						break;
					case 5:
						currentVal = Math.abs(i / 3 - 1) + Math.abs(i % 3 - 2);
						break;
					case 6:
						currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 0);
						break;
					case 7:
						currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 1);
						break;
					case 8:
						currentVal = Math.abs(i / 3 - 2) + Math.abs(i % 3 - 2);
						break;
				}
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stateInfoView.squareButtons[stateInfoView.vals[i]].setBorder(originalBorder);
				stateInfoView.squareButtons[i].setBorder(originalBorder);
				if (stateInfoView.vals[i] != 0) System.out.println("h(" + stateInfoView.vals[i] + "): " + currentVal);
				retVal += currentVal;
			}
			System.out.println("\n" + "h = "+ Integer.toString(retVal));
		}

		@Override
		public void calculateMistiledHeuristic() {
			int retVal = 0;
			stateInfoView.resetViewBorderToInitial();
			Border borderMistiled = BorderFactory.createLineBorder(Color.RED,10);
			Border borderRightTiled = BorderFactory.createLineBorder(Color.GREEN,10);
			Border originalBorder = stateInfoView.squareButtons[0].getBorder();
			for (int i = 0; i < 9; i++) {
				switch (stateInfoView.vals[i]) {
					case 1:
						if ((i / 3 == 0) && (i % 3 == 1)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 2:
						if ((i / 3 == 0) && (i % 3 == 2)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 3:
						if ((i / 3 == 1) && (i % 3 == 0)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 4:
						if ((i / 3 == 1) && (i % 3 == 1)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 5:
						if ((i / 3 == 1) && (i % 3 == 2)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 6:
						if ((i / 3 == 2) && (i % 3 == 0)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 7:
						if ((i / 3 == 2) && (i % 3 == 1)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
					case 8:
						if ((i / 3 == 2) && (i % 3 == 2)) {
							stateInfoView.squareButtons[i].setBorder(borderRightTiled);
						} else {
							stateInfoView.squareButtons[i].setBorder(borderMistiled);
							retVal += 1;
						}
						break;
				}
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("\n" + "h = "+ Integer.toString(retVal));
		}
	}

	/** Simple environment maintaining just the current board state. */
	protected static class EightPuzzleEnvironment extends AbstractEnvironment {
		EightPuzzleBoard board;

		protected EightPuzzleEnvironment(EightPuzzleBoard board) {
			this.board = board;
		}

		protected EightPuzzleBoard getBoard() {
			return board;
		}

		/** Executes the provided action and returns null. */
		@Override
		public void executeAction(Agent agent, Action action) {
			if (action == EightPuzzleBoard.UP)
				board.moveGapUp();
			else if (action == EightPuzzleBoard.DOWN)
				board.moveGapDown();
			else if (action == EightPuzzleBoard.LEFT)
				board.moveGapLeft();
			else if (action == EightPuzzleBoard.RIGHT)
				board.moveGapRight();
			if (agent == null)
				notifyEnvironmentViews(agent, action);
		}

		/** Returns null. */
		@Override
		public Percept getPerceptSeenBy(Agent anAgent) {
			return null;
		}
	}

	public static class EightPuzzleStateInfoView extends EightPuzzleView {
		int[] vals;

		EightPuzzleStateInfoView() {
			removeAll();
			Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 32);
			setLayout(new GridLayout(3,3));
			for (int i = 0; i < 9; i++) {
				JButton square = new JButton(Integer.toString(i));
				square.setFont(f);
				square.addActionListener(this);
				squareButtons[i] = square;
				add(square);
			}
			vals = new int[9];
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
		}

		public void copyState(EightPuzzleView view) {
			for (int i = 0; i < 9; i++) {
				String text = view.squareButtons[i].getText();
				squareButtons[i].setText(view.squareButtons[i].getText());
				if (text == "") vals[i] = 0;
				else vals[i] = Integer.parseInt(text);
			}
			resetViewBorderToInitial();
		}


		public void resetViewBorderToInitial() {
			Border defaultBorder = UIManager.getBorder("Button.border");
			for (int i = 0; i < 9; i++) {
				squareButtons[i].setBorder(defaultBorder);
			}
		}
	}
}
