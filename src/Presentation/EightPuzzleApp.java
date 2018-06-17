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

	static {
		addSearchAlgorithm("Breadth First Search (Graph Search)",
				new BreadthFirstSearch(new GraphSearch()));
		addSearchAlgorithm("Breadth First Search (Bidirectional Search)",
				new BreadthFirstSearch(new BidirectionalSearch()));
		addSearchAlgorithm("Depth Limited Search (9)", new DepthLimitedSearch(9));
		addSearchAlgorithm("Iterative Deepening Search", new IterativeDeepeningSearch());
		addSearchAlgorithm("Greedy Best First Search (MisplacedTileHeursitic)",
				new GreedyBestFirstSearch(new GraphSearch(), new MisplacedTilleHeuristicFunction()));
		addSearchAlgorithm("Greedy Best First Search (ManhattanHeursitic)",
				new GreedyBestFirstSearch(new GraphSearch(), new ManhattanHeuristicFunction()));
		addSearchAlgorithm("AStar Search (MisplacedTileHeursitic)",
				new AStarSearch(new GraphSearch(), new MisplacedTilleHeuristicFunction()));
		addSearchAlgorithm("AStar Search (ManhattanHeursitic)",
				new AStarSearch(new GraphSearch(), new ManhattanHeuristicFunction()));
		addSearchAlgorithm("Simulated Annealing Search",
				new SimulatedAnnealingSearch(new ManhattanHeuristicFunction()));
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
			setSize(1000, 750);
		}
	}

	/**
	 * Displays the informations provided by a
	 * <code>EightPuzzleEnvironment</code> on a panel using an grid of buttons.
	 * By pressing a button, the user can move the corresponding tile to the
	 * adjacent gap.
	 */
	public static class EmptyEightPuzzleView extends AgentAppEnvironmentView {

		EmptyEightPuzzleView () {
			setLayout(null);
			setPreferredSize(new Dimension(5000,5000));
		}

		@Override
		public void agentActed(Agent agent, Action action, Environment source) {

		}

		@Override
		public void agentAdded(Agent agent, Environment source) {
		}


		public void drawTree() {
			for (int i = 0; i < EightPuzzleTree.tree.size(); i++) {  //depth
				for (int j = 0; j < EightPuzzleTree.tree.get(i).size(); j++) { //nodes in a level of depth
					System.out.println("i: " + i + ", j: " + j);
					EightPuzzleView newView = EightPuzzleTree.tree.get(i).get(j).view;
					add(newView);
					newView.setSize(200,200);
					newView.setLocation(300 * j + 100,300 * i + 100);
				}
			}
		}
	}

	protected static class EightPuzzleView extends AgentAppEnvironmentView implements ActionListener {
		private static final long serialVersionUID = 1L;
		public JButton[] squareButtons;

		protected EightPuzzleView() {
			setLayout(new GridLayout(3, 3));
			Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 32);
			squareButtons = new JButton[9];
			for (int i = 0; i < 9; i++) {
				JButton square = new JButton(Integer.toString(i));
				square.setFont(f);
				square.addActionListener(this);
				squareButtons[i] = square;
				add(square);
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
//			for (int i = 0; i < 9; i++) {
//				if (ae.getSource() == squareButtons[i]) {
//					EightPuzzleController contr = (EightPuzzleController) getController();
//					XYLocation locGap = ((EightPuzzleEnvironment) env).getBoard().getLocationOf(0);
//					if (locGap.getXCoOrdinate() == i / 3) {
//						if (locGap.getYCoOrdinate() == i % 3 - 1)
//							contr.executeUserAction(EightPuzzleBoard.RIGHT);
//						else if (locGap.getYCoOrdinate() == i % 3 + 1)
//							contr.executeUserAction(EightPuzzleBoard.LEFT);
//					} else if (locGap.getYCoOrdinate() == i % 3) {
//						if (locGap.getXCoOrdinate() == i / 3 - 1)
//							contr.executeUserAction(EightPuzzleBoard.DOWN);
//						else if (locGap.getXCoOrdinate() == i / 3 + 1)
//							contr.executeUserAction(EightPuzzleBoard.UP);
//					}
//				}
//			}
			stateInfoView.copyState(this);
			AgentAppFrame.rightPane.add(JSplitPane.TOP,stateInfoView);
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
			Border borderMistiled = BorderFactory.createLineBorder(Color.RED,10);
			Border borderRightTiled = BorderFactory.createLineBorder(Color.GREEN,10);
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
		}
	}
}
