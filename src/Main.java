import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	private static final double MIN_DISTANCE = 30;
	private Robot robot;
	private Point pos1, pos2;
	private boolean disabled;
	private static boolean sounds;
	private JWindow frame;
	private JWindow frame2;
	private TrayIcon trayIcon;
	private ArrayList<Cube> cubes;

	// Alle Monitore
	// GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()

	// Maus Position
	// MouseInfo.getPointerInfo().getLocation()

	public Main() {
		cubes = new ArrayList<>();
		pos1 = new Point();
		pos2 = new Point();
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		JPopupMenu jpopup = new JPopupMenu();
		JMenuItem ext = new JMenuItem("Beenden");
		JMenuItem ext2 = new JMenuItem("Würfel erstellen");
		JMenuItem ext3 = new JMenuItem("Alle Würfel entfernen");
		jpopup.add(ext);
		jpopup.add(ext2);
		jpopup.add(ext3);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException ex) {
		}

		frame = new JWindow();
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.addMouseMotionListener(new MouseMotionListener() {
			private int mx, my;

			@Override
			public void mouseMoved(MouseEvent e) {
				mx = e.getXOnScreen();
				my = e.getYOnScreen();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e))
					return;
				Point p = frame.getLocation();
				p.x += e.getXOnScreen() - mx;
				p.y += e.getYOnScreen() - my;
				pos1.setLocation(p.x + frame.getWidth() / 2, p.y + frame.getHeight() / 2);
				mx = e.getXOnScreen();
				my = e.getYOnScreen();
				frame.setLocation(p);
			}
		});
		frame.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
			}

			private void doPop(MouseEvent e) {
				jpopup.show(frame, e.getX(), e.getY());
			}
		});
		frame.setContentPane(new PortalPane(false));
		frame.pack();
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame2 = new JWindow();
		frame2.setBackground(new Color(0, 0, 0, 0));
		frame2.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
			}

			private void doPop(MouseEvent e) {
				jpopup.show(frame2, e.getX(), e.getY());
			}
		});
		frame2.addMouseMotionListener(new MouseMotionListener() {
			private int mx, my;

			@Override
			public void mouseMoved(MouseEvent e) {
				mx = e.getXOnScreen();
				my = e.getYOnScreen();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Point p = frame2.getLocation();
				p.x += e.getXOnScreen() - mx;
				p.y += e.getYOnScreen() - my;
				pos2.setLocation(p.x + frame2.getWidth() / 2, p.y + frame2.getHeight() / 2);
				mx = e.getXOnScreen();
				my = e.getYOnScreen();
				frame2.setLocation(p);
			}
		});
		frame2.setContentPane(new PortalPane(true));
		frame2.pack();
		frame2.setVisible(true);
		frame2.setAlwaysOnTop(true);
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		try {
			final PopupMenu popup = new PopupMenu();
			MenuItem exitItem = new MenuItem("Beenden");
			MenuItem cubeItem = new MenuItem("Würfel erzeugen");
			MenuItem delItem = new MenuItem("Alle Würfel entfernen");
			CheckboxMenuItem soundItem = new CheckboxMenuItem("Sounds", false);
			soundItem.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					sounds = soundItem.getState();
				}
			});
			exitItem.addActionListener(event -> {
				exit();
			});
			cubeItem.addActionListener(event -> {
				addCube();
			});
			delItem.addActionListener(event -> {
				delCubes();
			});
			popup.add(soundItem);
			popup.add(exitItem);
			popup.add(cubeItem);
			popup.add(delItem);
			trayIcon = new TrayIcon(ImageIO.read(getClass().getResource("/icon.png")));

			trayIcon.setPopupMenu(popup);
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("Cursor Portal");
			final SystemTray tray = SystemTray.getSystemTray();
			try {
				tray.add(trayIcon);

			} catch (AWTException e) {
				System.out.println("TrayIcon could not be added.");
			}
			ext.addActionListener(event -> {
				exit();
			});
			ext2.addActionListener(event -> {
				addCube();
			});
			ext3.addActionListener(event -> {
				delCubes();
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		frame.setLocation(frame.getWidth(), 0);
		pos1.setLocation(frame.getWidth() + frame.getWidth() / 2, frame.getHeight() / 2);
		pos2.setLocation(frame2.getWidth() / 2, frame2.getHeight() / 2);

	}

	private void delCubes() {
		for (int i = 0; i < cubes.size(); i++) {
			cubes.get(i).dispose();
		}
		cubes.clear();
	}

	private void addCube() {
		if (cubes.isEmpty())
			cubes.add(new Cube(pos1, pos2));
	}

	private void exit() {
		trayIcon.displayMessage("Made by Tim Morgner",
				"Vielen Dank für das Benutzen meiner Software.\nFragen können an @Baspla gestellt werden",
				TrayIcon.MessageType.NONE);
			playsound("goodbye.wav");
		frame.setVisible(false);
		frame2.setVisible(false);
		for (int i = 0; i < cubes.size(); i++) {
			cubes.get(i).setVisible(false);
		}
		disabled = true;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

				System.exit(0);
			}
		}, 5000);
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.moveListen();
	}

	private void moveListen() {
		new Thread(() -> {
			boolean justported = false;
			while (!disabled) {
				Point pos = MouseInfo.getPointerInfo().getLocation();
				if (pos.distance(pos1) < MIN_DISTANCE) {
					if (!justported) {
						justported = true;
						robot.mouseMove(pos2.x, pos2.y);
						playsound("pop.wav");
					}
				} else {
					if (pos.distance(pos2) < MIN_DISTANCE) {
						if (!justported) {
							justported = true;
							robot.mouseMove(pos1.x, pos1.y);
							playsound("pop.wav");
						}
					} else {
						justported = false;
					}
				}
			}
		}).start();
	}

	static void playsound(String string) {
		if(!sounds)return;
		new Thread(new Runnable() {
			public void run() {
				try {
					Clip clip = AudioSystem.getClip();
					AudioInputStream inputStream = AudioSystem.getAudioInputStream(getClass().getResource(string));
					clip.open(inputStream);
					clip.start();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}

	public class PortalPane extends JPanel {

		private static final long serialVersionUID = 4242L;
		private BufferedImage img;

		public PortalPane(boolean b) {
			try {
				if (b) {
					img = ImageIO.read(getClass().getResource("/myportal.png"));
				} else {
					img = ImageIO.read(getClass().getResource("/myportal2.png"));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			setOpaque(false);
			setLayout(new GridBagLayout());

		}

		@Override
		public Dimension getPreferredSize() {
			return img == null ? new Dimension(200, 200) : new Dimension(img.getWidth(), img.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (img != null) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.drawImage(img, 0, 0, this);
				g2d.dispose();
			}
		}
	}
}
