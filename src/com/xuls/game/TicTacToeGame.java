package com.xuls.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * 井字棋
 *
 * @author xuls
 * @date 2022/4/2 21:52
 */
public class TicTacToeGame {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TicTacToeGame().startGame());
	}

	private void startGame() {
		JFrame jFrame = new JFrame("井字棋");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.getContentPane().add(new GamePanel(new GameModel()));
		jFrame.setSize(new Dimension(600, 600));
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}

	//处理井字棋游戏逻辑
	private class GameModel {
		//棋盘
		private Cell[][] checkerboard = new Cell[3][3];
		//当前是红方还是蓝方
		private boolean isRedMark = true;
		//记录下棋的次数
		private int count = 0;
		//赢的情况
		private int[] wins = {7, 56, 448, 73, 146, 292, 273, 84};
		//是否结束
		private boolean over = false;
		//结束语
		private String gameOverString = "";
		//红方结果
		private int red = 0;
		//蓝方结果
		private int blue = 0;

		GameModel() {
			//初始化棋盘
			initCheckerboardModel();
		}

		//重新开始
		void reStart() {
			count = 0;
			over = false;
			gameOverString = "";
			red = 0;
			blue = 0;
			initCheckerboardModel();
		}

		void initCheckerboardModel() {
			for (int i = 0; i < checkerboard.length; i++) {
				for (int j = 0; j < checkerboard[i].length; j++) {
					checkerboard[i][j] = new Cell();
				}
			}
		}

		/**
		 * 检查游戏是否结束
		 * 这里的解法是 Leecode 上 amanehayashi 用的位运算的解法
		 * 详情参考这里 ：https://leetcode-cn.com/problems/find-winner-on-a-tic-tac-toe-game/solution/java-wei-yun-suan-xiang-jie-shi-yong-wei-yun-suan-/
		 * @param i 所在行
		 * @param j 所在列
		 */
		void checkOver(int i, int j) {
			if (isRedMark) {
				red ^= 1 << (i * 3 + j);
			} else {
				blue ^= 1 << (i * 3 + j);
			}
			for (int win : wins) {
				if ((win & red) == win) {
					gameOverString = "红方获胜!";
					over = true;
					return;
				} else if ((win & blue) == win) {
					gameOverString = "蓝方获胜!";
					over = true;
					return;
				}
			}
			if (count == 9) {
				gameOverString = "旗鼓相当的对手!";
				over = true;
			}
		}

		//处理游戏点击事件
		boolean click(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1 || isOver()) {
				return false;
			}
			Point point = e.getPoint();
			for (int i = 0; i < checkerboard.length; i++) {
				for (int j = 0; j < checkerboard[i].length; j++) {
					Cell cell = checkerboard[i][j];
					if (cell.getClass().isAssignableFrom(Cell.class) && cell.contains(point)) {
						Rectangle rectangle = checkerboard[i][j].getRectangle();
						checkerboard[i][j] = getCell(rectangle);
						count++;
						checkOver(i, j);
						changeMark();
						return true;
					}
				}
			}
			return false;
		}

		//红蓝交换
		private void changeMark() {
			isRedMark = !isRedMark;
		}

		//获得对应的格子 红色 or 蓝色
		private Cell getCell(Rectangle rectangle) {
			return isRedMark ? new RedMarkCell(rectangle) : new BlueMarkCell(rectangle);
		}

		boolean isOver() {
			return over;
		}

		String getGameOverString() {
			return gameOverString;
		}

		//更新棋盘每个格子的范围，方便去绘制和判断点击的点是否在格子中
		private void updateCheckerboardRectangle(int panelWidth, int panelHeight) {
			int width = (int) (panelWidth * 1.0 / checkerboard.length);
			int height = (int) (panelHeight * 1.0 / checkerboard[0].length);
			for (int i = 0; i < checkerboard.length; i++) {
				for (int j = 0; j < checkerboard[i].length; j++) {
					checkerboard[i][j].setRectangle(new Rectangle(width * i, height * j, width, height));
				}
			}
		}

		Cell[][] getCheckerboard() {
			return checkerboard;
		}
	}

	//井字棋绘制
	private class GamePanel extends JPanel {
		//移动点
		private Point movingPoint;
		private GameModel gameModel;
		private MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//鼠标点击事件
				if (gameModel.click(e)) {
					repaint();//重新绘制
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				//鼠标移动事件
				movingPoint = e.getPoint();
				repaint();
			}
		};
		private KeyAdapter keyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				//按键事件
				if (e.getKeyCode() == KeyEvent.VK_SPACE && gameModel.isOver()){
					gameModel.reStart();
					repaint();
				}
			}
		};

		GamePanel(GameModel gameModel) {
			this.gameModel = gameModel;
			addMouseListener(mouseAdapter);
			addMouseMotionListener(mouseAdapter);
			this.setFocusable(true);
			addKeyListener(keyAdapter);
		}

		@Override
		public void paint(Graphics g) {
			//绘制时，先更新格子范围
			gameModel.updateCheckerboardRectangle(getWidth(), getHeight());
			//绘制棋盘
			paintCheckerboard(g);
			//绘制焦点
			paintFocus(g);
			//绘制游戏结束语
			paintGameOver(g);
		}

		private void paintFocus(Graphics g) {
			if (gameModel.isOver()){
				return;
			}
			Graphics2D g2d = (Graphics2D) g.create();
			Color color = gameModel.isRedMark ? Color.red:Color.blue;
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(5));
			Rectangle movingRectangle = getMovingRectangle();
			if (movingRectangle == null){
				return;
			}
			int length = Math.min(movingRectangle.width / 4 ,movingRectangle.height /4);
			g2d.drawLine(movingRectangle.x,movingRectangle.y,movingRectangle.x+length,movingRectangle.y);
			g2d.drawLine(movingRectangle.x,movingRectangle.y,movingRectangle.x,movingRectangle.y+length);

			g2d.drawLine(movingRectangle.x+movingRectangle.width-length,movingRectangle.y,movingRectangle.x+movingRectangle.width,movingRectangle.y);
			g2d.drawLine(movingRectangle.x+movingRectangle.width,movingRectangle.y,movingRectangle.x+movingRectangle.width,movingRectangle.y+length);

			g2d.drawLine(movingRectangle.x,movingRectangle.y+movingRectangle.height-length,movingRectangle.x,movingRectangle.y+movingRectangle.height);
			g2d.drawLine(movingRectangle.x,movingRectangle.y+movingRectangle.height,movingRectangle.x+length,movingRectangle.y+movingRectangle.height);

			g2d.drawLine(movingRectangle.x+movingRectangle.width-length,movingRectangle.y+movingRectangle.height,movingRectangle.x+movingRectangle.width,movingRectangle.y+movingRectangle.height);
			g2d.drawLine(movingRectangle.x+movingRectangle.width,movingRectangle.y+movingRectangle.height-length,movingRectangle.x+movingRectangle.width,movingRectangle.y+movingRectangle.height);
		}

		private Rectangle getMovingRectangle(){
			Cell[][] checkerboard = gameModel.getCheckerboard();
			for (Cell[] cells : checkerboard) {
				for (Cell cell : cells) {
					if (movingPoint != null && cell.contains(movingPoint)){
						return cell.getRectangle();
					}
				}
			}
			return null;
		}

		private void paintCheckerboard(Graphics g) {
			Graphics graphics = g.create();
			graphics.setColor(new Color(238, 238, 238));
			graphics.fillRect(0, 0, getWidth(), getHeight());
			Cell[][] checkerboard = gameModel.getCheckerboard();
			for (Cell[] cells : checkerboard) {
				for (Cell cell : cells) {
					Graphics2D g2d = (Graphics2D) g.create();
					cell.paint(g2d);
				}
			}
		}

		private void paintGameOver(Graphics g) {
			if (!gameModel.isOver()) {
				return;
			}
			Graphics2D g2d = (Graphics2D) g.create();
			Font font = new Font("宋体", Font.BOLD, 30);
			g2d.setFont(font);
			g2d.setColor(Color.DARK_GRAY);
			FontMetrics fontMetrics = g2d.getFontMetrics(font);
			Rectangle2D rectangle2D = fontMetrics.getStringBounds(gameModel.getGameOverString(), g2d);
			int x = (int) ((getWidth() - rectangle2D.getWidth()) / 2);
			int y = (int) ((getHeight() + rectangle2D.getHeight()) / 2);
			g2d.drawString(gameModel.getGameOverString(), x, y);
			String str = "点击空格键重新开始....";
			font = new Font("宋体", Font.BOLD, 15);
			g2d.setFont(font);
			fontMetrics = g2d.getFontMetrics(font);
			rectangle2D = fontMetrics.getStringBounds(str, g2d);
			x = (int) (getWidth() - rectangle2D.getWidth());
			y = (int) (getHeight() - rectangle2D.getHeight());
			g2d.drawString(str, x, y);
		}
	}


	//格子，记录范围和绘制
	private class Cell {
		private Rectangle rectangle;

		Cell() {
		}

		Cell(Rectangle rectangle) {
			this.rectangle = rectangle;
		}

		Rectangle getRectangle() {
			return rectangle;
		}

		void setRectangle(Rectangle rectangle) {
			this.rectangle = rectangle;
		}

		void paint(Graphics2D g2d) {
			g2d.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
		}

		boolean contains(Point point) {
			return rectangle.contains(point);
		}
	}

	//红色格子，记录范围和绘制叉 X
	private class RedMarkCell extends Cell {

		RedMarkCell(Rectangle rectangle) {
			super(rectangle);
		}

		@Override
		void paint(Graphics2D g2d) {
			super.paint(g2d);
			g2d.setColor(Color.red);
			g2d.setStroke(new BasicStroke(5));
			Rectangle rectangle = getRectangle();
			int w = (int) (rectangle.getWidth() / 3);
			int h = (int) (rectangle.getHeight() / 3);
			g2d.drawOval(rectangle.x + w, rectangle.y + h, w, h);
		}
	}

	//蓝色格子 记录范围 和 绘制圈 O
	private class BlueMarkCell extends Cell {

		BlueMarkCell(Rectangle rectangle) {
			super(rectangle);
		}

		@Override
		void paint(Graphics2D g2d) {
			super.paint(g2d);
			g2d.setColor(Color.BLUE);
			g2d.setStroke(new BasicStroke(5));
			Rectangle rectangle = getRectangle();
			int w = (int) (rectangle.getWidth() / 3);
			int h = (int) (rectangle.getHeight() / 3);
			g2d.drawLine(rectangle.x + w, rectangle.y + h, rectangle.x + rectangle.width - w, rectangle.y + rectangle.height - h);
			g2d.drawLine(rectangle.x + rectangle.width - w, rectangle.y + h, rectangle.x + w, rectangle.y + rectangle.height - h);
		}
	}

}
