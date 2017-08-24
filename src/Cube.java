import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class Cube extends JWindow {
	private static final long serialVersionUID = 1337L;
	private boolean dragging;
	protected CubePane pane;
	private long period = 10;
	private double vy, vx, g = 9.81;
	private Point dragPoint;
	private double friction = 1.3;
	private Point point1;
	private Point point2;
	protected int entered = 0;

	public Cube(Point p1, Point p2) {
		Main.playsound("spawn"+(new Random().nextInt(5)+1)+".wav");
		this.point1 = p1;
		this.point2 = p2;
		this.setBackground(new Color(0, 0, 0, 0));
		this.setAlwaysOnTop(true);
		this.setAutoRequestFocus(true);
		pane = new CubePane((float) ((Math.random() / 2) + 0.25));
		this.setContentPane(pane);

		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				dragging = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				dragPoint = e.getPoint();
				dragging = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Timer t = new Timer(true);
		t.scheduleAtFixedRate(new TimerTask() {

			private Point lastpos;

			@Override
			public void run() {
				if (!dragging) {
					vy = vy + g * period / 1000;
					if (getBounds().contains(point1)) {
						if (entered != 2) {
							entered = 1;
							setLocation((int) (getX() + (point2.getX() - point1.getX())),
									(int) (getY() + (point2.getY() - point1.getY())));
						}
					} else if (getBounds().contains(point2)) {
						if (entered != 1) {
							entered = 2;
							setLocation((int) (getX() + (point1.getX() - point2.getX())),
									(int) (getY() + (point1.getY() - point2.getY())));
						}
					} else {
						entered = 0;
					}

					int y = (int) (getY() + vy);
					int x = (int) (getX() + vx);
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
					if (x < 0) {
						x = 0;
						if (vx > 2.5) {
							vx = 0;
						} else {
							vx = -vx / 2;
						}
					}
					if (y < 0) {
						y = 0;
						if (vy > 2.5) {
							vy = 0;
						} else {
							vy = -vy / 2;
						}
					}
					if (x + getWidth() > d.getWidth()) {
						x = (int) d.getWidth() - getWidth();
						if (vx < 2.5) {
							vx = 0;
						} else {
							vx = -vx / 2;
						}

					}
					if (y + getHeight() > d.getHeight()) {
						y = (int) d.getHeight() - getHeight();
						if (vy < 2.5) {
							vy = 0;
						} else {
							vy = -vy / 2;
						}
						vx = vx / friction;
						if (vx < 0.1 && vx > 0)
							vx = 0;
						if (vx > -0.1 && vx < 0)
							vx = 0;
					}
					setLocation(x, y);
				} else {
					Point mpos = MouseInfo.getPointerInfo().getLocation();
					if (dragPoint != null && mpos != null)
						setLocation((int) (mpos.getX() - dragPoint.getX()), (int) (mpos.getY() - dragPoint.getY()));
					if (lastpos == null)
						lastpos = mpos;
					vy = mpos.getY() - lastpos.getY();
					vx = mpos.getX() - lastpos.getX();
					lastpos = mpos;
				}
			}
		}, period, period);
		this.setVisible(true);
		this.pack();
	}

	@Override
	public void dispose() {
		super.dispose();
		Main.playsound("death" + (new Random().nextInt(2) + 1) + ".wav");
	}
}

class CubePane extends JPanel {
	private static final long serialVersionUID = -9133683753848867517L;
	private BufferedImage img;
	private float scale = 0.5f;

	public CubePane(float d) {
		try {
			img = ImageIO.read(getClass().getResource("cube.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		scale = d;
		setOpaque(false);
		setLayout(new GridBagLayout());

	}

	@Override
	public Dimension getPreferredSize() {
		return img == null ? new Dimension(200, 200)
				: new Dimension((int) (img.getWidth() * scale), (int) (img.getHeight() * scale));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (img != null) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.drawImage(img, 0, 0, (int) (img.getWidth() * scale), (int) (img.getHeight() * scale), this);
			g2d.dispose();
		}
	}
}
