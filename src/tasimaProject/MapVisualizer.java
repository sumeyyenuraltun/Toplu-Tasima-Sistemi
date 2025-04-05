package tasimaProject;

import transport.*;

import org.graphstream.graph.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.io.File;
import java.text.DecimalFormat;

import javax.swing.event.MouseInputListener;
import javax.swing.text.*;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import transport.calculateRouteAttributes;

import javax.swing.border.TitledBorder;

public class MapVisualizer {
	private static final int DEFAULT_WIDTH = 1540;
	private static final int DEFAULT_HEIGHT = 1000;

	private final Graph graph;
	private final JXMapViewer mapViewer;
	private final JFrame frame;
	private final Map<String, GeoPosition> nodePositions;
	private boolean highlightEnabled = false;
	private JPopupMenu popupMenu;
	private Node hoveredNode = null;
	private JTextField startLocationField1, startLocationField2;
	private JTextField endLocationField1, endLocationField2;
	private JComboBox<String> passengerTypeComboBox;
	private JComboBox<String> paymentTypeComboBox;
	private JSpinner hourSpinner, minuteSpinner;
	private JButton findRouteButton;
	private RouteFinder routeFinder;
	private List<Node> currentRoute = null;
	private boolean isStartSelected = false;
	private boolean ozelDayCounter = false;
	private JCheckBox specialDayCheckBox;
	private JButton bakiyeSorgulaButton;
	private String bilgi;
	private JPanel ekpanel ;


	public MapVisualizer(Graph graph) {
		this.graph = graph;
		this.nodePositions = new HashMap<>();
		this.mapViewer = setupMapViewer();
		this.frame = new JFrame("İZMİT ROTA PLANLAMA SİSTEMİ");
		this.popupMenu = new JPopupMenu();
		this.routeFinder = new RouteFinder(graph);


		extractNodePositions();
		setupFrame();
		updateMap();
	}


	public static Graph visualize(Graph graph) {
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(() -> {
					MapVisualizer visualizer = new MapVisualizer(graph);
					visualizer.display();
				});
			} else {
				MapVisualizer visualizer = new MapVisualizer(graph);
				visualizer.display();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return graph;
	}


	public void display() {
		SwingUtilities.invokeLater(() -> {
			frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			frame.setLocationRelativeTo(null);
			Component layeredPane = frame.getContentPane().getComponent(0);
			layeredPane.setBounds(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

			frame.setVisible(true);
			mapViewer.repaint();
			refreshDisplay();

			System.out.println("Map visualization displayed");
		});
	}

	private JXMapViewer setupMapViewer() {
		try {
			TileFactoryInfo info = new OSMTileFactoryInfo();
			DefaultTileFactory tileFactory = new DefaultTileFactory(info);

			File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

			JXMapViewer mapViewer = new JXMapViewer();
			mapViewer.setTileFactory(tileFactory);

			mapViewer.setZoom(7);

			mapViewer.setAddressLocation(new GeoPosition(39.9334, 32.8597));

			MouseInputListener mia = new PanMouseInputListener(mapViewer);
			mapViewer.addMouseListener(mia);
			mapViewer.addMouseMotionListener(mia);
			mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

			return mapViewer;
		} catch (Exception e) {
			e.printStackTrace();
			JXMapViewer fallbackMapViewer = new JXMapViewer();
			fallbackMapViewer.setTileFactory(new DefaultTileFactory(new OSMTileFactoryInfo()));
			return fallbackMapViewer;
		}
	}

	private void extractNodePositions() {
		for (Node node : graph) {
			if (node.hasAttribute("lat") && node.hasAttribute("lon")) {
				double lat = node.getAttribute("lat", Double.class);
				double lon = node.getAttribute("lon", Double.class);
				nodePositions.put(node.getId(), new GeoPosition(lat, lon));
			}
		}

		if (!nodePositions.isEmpty()) {
			double totalLat = 0, totalLon = 0;
			for (GeoPosition pos : nodePositions.values()) {
				totalLat += pos.getLatitude();
				totalLon += pos.getLongitude();
			}

			GeoPosition center = new GeoPosition(totalLat / nodePositions.size(), totalLon / nodePositions.size());

			mapViewer.setAddressLocation(center);
			mapViewer.setZoom(7);

			System.out.println("Map centered at: " + center.getLatitude() + ", " + center.getLongitude());
		} else {
			System.out.println("No node positions found with lat/lon attributes!");
		}
	}

	private void setupFrame() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		JPanel leftPanel = createLeftPanel();
		frame.getContentPane().add(leftPanel, BorderLayout.WEST);

		JPanel rightPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(rightPanel, BorderLayout.CENTER);

		rightPanel.add(mapViewer, BorderLayout.CENTER);

		JPanel debugPanel = new JPanel();
		JLabel statusLabel = new JLabel("Map Status: Loading...");
		debugPanel.add(statusLabel);
		rightPanel.add(debugPanel, BorderLayout.NORTH);

		JPanel controlPanel = createControlPanel();
		rightPanel.add(controlPanel, BorderLayout.SOUTH);

		mapViewer.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point mousePoint = e.getPoint();
				hoveredNode = null;

				for (Node node : graph) {
					if (nodePositions.containsKey(node.getId())) {
						GeoPosition pos = nodePositions.get(node.getId());
						Point2D nodePoint = mapViewer.getTileFactory().geoToPixel(pos, mapViewer.getZoom());

						int nodeX = (int) (nodePoint.getX() - mapViewer.getViewportBounds().getX());
						int nodeY = (int) (nodePoint.getY() - mapViewer.getViewportBounds().getY());

						int size = 40;
						if (Math.abs(mousePoint.x - nodeX) <= size && Math.abs(mousePoint.y - nodeY) <= size) {
							hoveredNode = node;

							StringBuilder tooltipText = new StringBuilder("<html>");
							tooltipText.append(
									"<div style='background-color:#FFFFCC; padding:5px; border:1px solid black;'>");
							tooltipText.append("<h3 style='margin:0;'>").append(node.getId()).append("</h3>");
							tooltipText.append("<hr style='margin:3px 0;'>");

							if (node.hasAttribute("type")) {
								tooltipText.append("<b>Type:</b> ").append(node.getAttribute("type")).append("<br>");
							}
							if (node.hasAttribute("lat")) {
								tooltipText.append("<b>Lat:</b> ").append(node.getAttribute("lat")).append("<br>");
							}
							if (node.hasAttribute("lon")) {
								tooltipText.append("<b>Lon:</b> ").append(node.getAttribute("lon")).append("<br>");
							}
							if (node.hasAttribute("ui.label")) {
								tooltipText.append("<b>Name:</b> ").append(node.getAttribute("ui.label"))
										.append("<br>");
							}

							tooltipText.append("</div></html>");

							JLabel label = new JLabel(tooltipText.toString());
							popupMenu.removeAll();
							popupMenu.add(label);
							popupMenu.show(mapViewer, e.getX() + 15, e.getY());
							mapViewer.repaint();
							refreshDisplay();
							return;
						}
					}
				}

				if (popupMenu.isVisible()) {
					popupMenu.setVisible(false);
					mapViewer.repaint();
				}
				refreshDisplay();
			}
		});

		mapViewer.addPropertyChangeListener("zoom", evt -> {
			statusLabel.setText("Map Status: Loaded (Zoom: " + mapViewer.getZoom() + ")");
		});

		JPanel glassPane = new JPanel() {
			{
				setOpaque(false);
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				for (Node node : graph) {
					if (nodePositions.containsKey(node.getId())) {
						try {
							GeoPosition pos = nodePositions.get(node.getId());
							Point2D point = mapViewer.getTileFactory().geoToPixel(pos, mapViewer.getZoom());

							Point mapViewerLocation = SwingUtilities.convertPoint(mapViewer, 0, 0, this);

							int x = (int) (point.getX() - mapViewer.getViewportBounds().getX());
							int y = (int) (point.getY() - mapViewer.getViewportBounds().getY());

							x += mapViewerLocation.x;
							y += mapViewerLocation.y;

							boolean isHovered = hoveredNode != null && hoveredNode.getId().equals(node.getId());
							boolean isTransfer = "transfer".equals(node.getAttribute("type"));
							int size = (isTransfer && highlightEnabled ? 12 : 8) * 5;

							if (isHovered) {
								size += 10;
								g2d.setColor(Color.YELLOW);
							} else {
								g2d.setColor(isTransfer && highlightEnabled ? Color.RED : new Color(255, 0, 0, 200));
							}

							g2d.fillOval(x - size / 2, y - size / 2, size, size);
							g2d.setColor(Color.BLACK);
							g2d.setStroke(new BasicStroke(3.0f));
							g2d.drawOval(x - size / 2, y - size / 2, size, size);

							String name = node.hasAttribute("ui.label") ? node.getAttribute("ui.label", String.class)
									: node.getId();
							FontMetrics fm = g2d.getFontMetrics();
							int textWidth = fm.stringWidth(name);

							g2d.setColor(new Color(0, 0, 0, 200));
							g2d.fillRect(x - textWidth / 2 - 4, y - size / 2 - 20, textWidth + 8, 20);
							g2d.setColor(Color.WHITE);
							g2d.drawString(name, x - textWidth / 2, y - size / 2 - 5);

						} catch (Exception e) {
							System.err.println("Error drawing node on glass pane: " + e.getMessage());
						}
					}
				}
			}
		};

		frame.setGlassPane(glassPane);
		frame.getGlassPane().setVisible(true);
	}

	private JPanel createLeftPanel() {
		JPanel leftPanel = new JPanel() {
			private Image backgroundImage;

			{
				try {

					backgroundImage = new ImageIcon("C://Users//Sümeyye//Downloads//izmit_sehir.png").getImage();
					if (backgroundImage.getWidth(null) <= 0) {
						BufferedImage placeholder = new BufferedImage(300, 1000, BufferedImage.TYPE_INT_RGB);
						Graphics2D g2d = placeholder.createGraphics();
						g2d.setPaint(
								new GradientPaint(0, 0, new Color(200, 200, 255), 0, 1000, new Color(100, 100, 200)));
						g2d.fillRect(0, 0, 300, 1000);
						g2d.dispose();
						backgroundImage = placeholder;
					}
				} catch (Exception e) {
					System.err.println("Error loading background image: " + e.getMessage());

					BufferedImage placeholder = new BufferedImage(300, 1000, BufferedImage.TYPE_INT_RGB);
					Graphics2D g2d = placeholder.createGraphics();
					g2d.setPaint(new GradientPaint(0, 0, new Color(200, 200, 255), 0, 1000, new Color(100, 100, 200)));
					g2d.fillRect(0, 0, 300, 1000);
					g2d.dispose();
					backgroundImage = placeholder;
				}
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();

				float transparency = 0.3f;
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
				g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
				g2d.dispose();
			}
		};

		leftPanel.setLayout(null);
		leftPanel.setPreferredSize(new Dimension(300, DEFAULT_HEIGHT));

		Color yesilColor = new Color(10, 92, 10);

		JLabel title1 = new JLabel("İZMİT ROTA");
		JLabel title2 = new JLabel("SİSTEMİ");
		title1.setBounds(65, 30, 160, 35);
		title1.setForeground(yesilColor);
		title1.setFont(new Font("HelveticaNeue-BoldItalic", Font.BOLD, 28));
		leftPanel.add(title1);

		title2.setBounds(90, 65, 155, 35);
		title2.setFont(new Font("HelveticaNeue-BoldItalic", Font.BOLD, 28));
		title2.setForeground(yesilColor);
		leftPanel.add(title2);

		JLabel label1 = new JLabel("Başlangıç konumu giriniz !");
		label1.setBounds(60, 130, 190, 30);
		label1.setFont(new Font("HelveticaNeue-BoldItalic", Font.LAYOUT_LEFT_TO_RIGHT, 16));
		leftPanel.add(label1);

		startLocationField1 = new JTextField();
		startLocationField1.setBounds(40, 170, 100, 30);
		leftPanel.add(startLocationField1);

		startLocationField2 = new JTextField();
		startLocationField2.setBounds(155, 170, 100, 30);
		leftPanel.add(startLocationField2);

		JLabel label2 = new JLabel("Hedef konumu giriniz !");
		label2.setFont(new Font("HelveticaNeue-BoldItalic", Font.LAYOUT_LEFT_TO_RIGHT, 16));
		label2.setBounds(75, 210, 190, 30);
		leftPanel.add(label2);

		endLocationField1 = new JTextField();
		endLocationField1.setBounds(40, 250, 100, 30);
		leftPanel.add(endLocationField1);

		endLocationField2 = new JTextField();
		endLocationField2.setBounds(155, 250, 100, 30);
		leftPanel.add(endLocationField2);

		JLabel label3 = new JLabel("Yolcu türünü seçiniz !");
		label3.setFont(new Font("HelveticaNeue-BoldItalic", Font.LAYOUT_LEFT_TO_RIGHT, 16));
		label3.setBounds(80, 290, 190, 30);
		leftPanel.add(label3);

        String[] passengerTypes = PassengerCreator.getPassengerTypesArray();

        for (String type : passengerTypes) {
            System.out.println(type);
        }


		passengerTypeComboBox = new JComboBox<>(passengerTypes);
		passengerTypeComboBox.setBounds(40, 330, 215, 30);
		leftPanel.add(passengerTypeComboBox);

		JLabel label4 = new JLabel("Ödeme türünü seçiniz !");
		label4.setFont(new Font("HelveticaNeue-BoldItalic", Font.LAYOUT_LEFT_TO_RIGHT, 16));
		label4.setBounds(70, 370, 215, 30);
		leftPanel.add(label4);


		Map<String, BiFunction<Double, Object[], OdemeYontemi>> methods = OdemeYontemiFactory.getPaymentMethods();

		//String[] paymentTypes = { "Kent Kart", "Nakit", "Kredi Kartı" };

		String[] paymentTypes = methods.keySet().stream() // Adds space between camelCase
				.toArray(String[]::new);
		paymentTypeComboBox = new JComboBox<>(paymentTypes);
		paymentTypeComboBox.setBounds(40, 410, 218, 30);
		leftPanel.add(paymentTypeComboBox);

		JLabel label5 = new JLabel("Saati giriniz !");
		label5.setFont(new Font("HelveticaNeue-BoldItalic", Font.LAYOUT_LEFT_TO_RIGHT, 16));
		label5.setBounds(105, 450, 190, 30);
		leftPanel.add(label5);

		SpinnerNumberModel hourModel = new SpinnerNumberModel(12, 0, 23, 1);
		SpinnerNumberModel minuteModel = new SpinnerNumberModel(30, 0, 59, 1);

		hourSpinner = new JSpinner(hourModel);
		minuteSpinner = new JSpinner(minuteModel);

		hourSpinner.setBounds(40, 490, 102, 30);
		minuteSpinner.setBounds(154, 490, 102, 30);
		leftPanel.add(hourSpinner);
		leftPanel.add(minuteSpinner);

		specialDayCheckBox = new JCheckBox("Özel gün mü?");
		specialDayCheckBox.setFocusable(false);
		specialDayCheckBox.setBounds(40, 550, 218, 30);

		specialDayCheckBox.addActionListener(e -> {
			this.ozelDayCounter = specialDayCheckBox.isSelected();
			System.out.println("Özel gün seçildi mi? " + this.ozelDayCounter);
		});

		leftPanel.add(specialDayCheckBox);


		mapViewer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point mousePoint = e.getPoint();

				System.out.println("Mouse Clicked at (Pixels): " + mousePoint);
				System.out.println("Map Viewer Size: " + mapViewer.getSize());
				System.out.println("Map Zoom Level: " + mapViewer.getZoom());

				GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(mousePoint);

				System.out.println("Converted GeoPosition: " + geoPosition);

				if (geoPosition != null) {
					String latitude = String.format("%.6f", geoPosition.getLatitude());
					String longitude = String.format("%.6f", geoPosition.getLongitude());

					// Noktaları virgülden noktaya çevir
					latitude = latitude.replace(',', '.');
					longitude = longitude.replace(',', '.');

					if (!isStartSelected) {
						startLocationField1.setText(latitude);
						startLocationField2.setText(longitude);
						isStartSelected = true;
						System.out.println("Başlangıç noktası seçildi: " + geoPosition);
					} else {
						endLocationField1.setText(latitude);
						endLocationField2.setText(longitude);
						isStartSelected = false;
						System.out.println("Hedef noktası seçildi: " + geoPosition);
					}
				} else {
					System.err.println("GeoPosition null döndü. Tıklanan nokta geçersiz olabilir.");
				}
			}
		});

		findRouteButton = new RoundedButton("ROTA BUL");
		findRouteButton.setFocusable(false);
		findRouteButton.setForeground(Color.white);
		findRouteButton.setBounds(40, 650, 220, 50);
		findRouteButton.addActionListener(e -> findRoute());
		findRouteButton.setBackground(yesilColor);

		leftPanel.add(findRouteButton);

		return leftPanel;
	}

	private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		final int R = 6371;
		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}

	private Node findNearestNode(double lat, double lon) {
		Node nearestNode = null;
		double minDistance = Double.MAX_VALUE;

		for (Node node : graph) {
			if (node.hasAttribute("lat") && node.hasAttribute("lon")) {
				double nodeLat = node.getAttribute("lat", Double.class);
				double nodeLon = node.getAttribute("lon", Double.class);
				double distance = calculateDistance(lat, lon, nodeLat, nodeLon);

				if (distance < minDistance) {
					minDistance = distance;
					nearestNode = node;
				}
			}
		}

		return nearestNode;
	}

	private void findRoute() {
		try {

			String startLoc1 = startLocationField1.getText();
			String startLoc2 = startLocationField2.getText();
			String endLoc1 = endLocationField1.getText();
			String endLoc2 = endLocationField2.getText();
			String paymentType = ((String) paymentTypeComboBox.getSelectedItem()).trim();
			int hour = (Integer) hourSpinner.getValue();
			int minute = (Integer) minuteSpinner.getValue();

			BiletHesapla biletHesapla = new BiletHesapla();
			String passengerType = (String) passengerTypeComboBox.getSelectedItem();
			Passenger yolcu = PassengerCreator.createPassenger(passengerType);

			double startLat = Double.parseDouble(startLoc1);
			double startLon = Double.parseDouble(startLoc2);
			double endLat = Double.parseDouble(endLoc1);
			double endLon = Double.parseDouble(endLoc2);

			Node startNode = findNearestNode(startLat, startLon);
			Node endNode = findNearestNode(endLat, endLon);

			if (startNode == null || endNode == null) {
				JOptionPane.showMessageDialog(frame, "Başlangıç veya hedef konumunun yakınında düğüm bulunamadı.",
						"Hata", JOptionPane.ERROR_MESSAGE);
				return;
			}

			double startToNodeDistance = calculateDistance(startLat, startLon,
					startNode.getAttribute("lat", Double.class), startNode.getAttribute("lon", Double.class));

			double endToNodeDistance = calculateDistance(endLat, endLon, endNode.getAttribute("lat", Double.class),
					endNode.getAttribute("lon", Double.class));

			DecimalFormat df = new DecimalFormat("#.##");

			List<List<Node>> allRoutes = routeFinder.findAllRoutes(startNode, endNode);
			List<Node> shortestRoute = routeFinder.findShortestRoute(startNode, endNode);

			Color yesil = new Color(10, 92, 10);

			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

			JPanel headerPanel = new JPanel();
			headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JTextPane headerTextPane = new JTextPane();
			headerTextPane.setEditable(false);
			//headerTextPane.setPreferredSize(new Dimension(600, headerTextPane.getPreferredSize().height));
			headerTextPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));

			StyledDocument headerDoc = headerTextPane.getStyledDocument();
			Style headerStyle = headerTextPane.addStyle("HeaderStyle", null);
			Style greenStyle = headerTextPane.addStyle("GreenStyle", null);

			StyleConstants.setFontSize(headerStyle, 15);
			StyleConstants.setFontSize(greenStyle, 15);
			StyleConstants.setForeground(greenStyle, yesil);
			StyleConstants.setBold(greenStyle, true);

			JButton taksiButton = new JButton("Taksi");
			JButton toplutasimaButton = new JButton("Toplu Taşıma");
			JButton yürümeButton = new JButton("Yürüme");

			IconFontSwing.register(FontAwesome.getIconFont());

			Icon icon = IconFontSwing.buildIcon(FontAwesome.MALE, 30);
			yürümeButton.setIcon(icon);
			Icon icon2 = IconFontSwing.buildIcon(FontAwesome.TAXI, 30);
			taksiButton.setIcon(icon2);
			Icon icon3 = IconFontSwing.buildIcon(FontAwesome.BUS, 30);
			toplutasimaButton.setIcon(icon3);

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(taksiButton);
			buttonPanel.add(toplutasimaButton);
			buttonPanel.add(yürümeButton);

			taksiButton.setPreferredSize(new Dimension(200, 35));
			toplutasimaButton.setPreferredSize(new Dimension(200, 35));
			yürümeButton.setPreferredSize(new Dimension(200, 35));

			contentPanel.add(buttonPanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

			contentPanel.add(headerTextPane);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

			JPanel routesPanel = new JPanel();
			routesPanel.setLayout(new BoxLayout(routesPanel, BoxLayout.Y_AXIS));
			routesPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

			contentPanel.add(routesPanel);

			JScrollPane mainScrollPane = new JScrollPane(contentPanel);
			mainScrollPane.setPreferredSize(new Dimension(640, 500));
			mainScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
			
			
			Color sari = new Color(255, 223, 0);
			
			toplutasimaButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					double finalPriceWithDiscount, priceWithoutTaxi;
					double totalDistance, totalTime;

					try {
						headerDoc.remove(0, headerDoc.getLength());
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}

					routesPanel.removeAll();
					
					double BaslangicSure = 0.0;
					double BaslangicMesafe = startToNodeDistance;
					double BaslangicUcret = 0.0;

					double VarisSure = 0.0;
					double VarisMesafe = endToNodeDistance;
					double VarisUcret = 0.0;

					String passenger = PassengerCreator.getPassengerType(yolcu);
					double ucret3=0;
					try {
						headerDoc.insertString(headerDoc.getLength(), "Rota Bilgileri:\n\n", greenStyle);
						headerDoc.insertString(headerDoc.getLength(), "En yakın başlangıç durağı: " + startNode.getId() + " ("
								+ startNode.getAttribute("ui.label", String.class) + ")\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(), "En yakın hedef durağı: " + endNode.getId() + " ("
								+ endNode.getAttribute("ui.label", String.class) + ")\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(),
								"Saat: " + hour + ":" + String.format("%02d", minute) + "\n\n", headerStyle);

						Vehicle arac = UlasimStratejisi.uygunArac(startToNodeDistance);
						String aracTuru = arac.getClass().getSimpleName();
						//ucret3 =arac.ucretHesapla(startToNodeDistance);
						headerDoc.insertString(headerDoc.getLength(), "Başlangıç noktasına olan mesafe: "
								+ df.format(startToNodeDistance) + " km. "+ aracTuru +" ile gidilecek.\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(),
								"Ucret: " + df.format(arac.ucretHesapla(startToNodeDistance)) + " TL\n",
								headerStyle);
						int sure = (int) Math.round(arac.sureHesapla(startToNodeDistance));
						int saat = sure / 60;
						int dakika = sure % 60;

						BaslangicSure = sure;
						BaslangicUcret = arac.ucretHesapla(startToNodeDistance);

						headerDoc.insertString(headerDoc.getLength(),
								"Süre: " + saat + " saat " + dakika + " dakika\n\n", headerStyle);


						String odemeSonucu = arac.processPayment(paymentType, arac.ucretHesapla(startToNodeDistance));
						headerDoc.insertString(headerDoc.getLength(), odemeSonucu + "\n\n", headerStyle);


/// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						Vehicle arac2 = UlasimStratejisi.uygunArac(endToNodeDistance);
						String aracTuru2 = arac2.getClass().getSimpleName();

						headerDoc.insertString(headerDoc.getLength(), "Bitiş noktasına olan mesafe: "
								+ df.format(endToNodeDistance) + " km. " + aracTuru2 + " ile gidilecek.\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(),
								"Ucret: " + df.format(arac2.ucretHesapla(endToNodeDistance)) + " TL\n",
								headerStyle);
						int sure2 = (int) Math.round(arac2.sureHesapla(endToNodeDistance));
						int saat2= sure2 / 60;
						int dakika2 = sure2 % 60;

						VarisSure = sure2;
						VarisUcret = arac2.ucretHesapla(endToNodeDistance);

						headerDoc.insertString(headerDoc.getLength(),
								"Süre: " + saat2 + " saat " + dakika2 + " dakika\n\n", headerStyle);


						odemeSonucu = arac2.processPayment(paymentType, arac2.ucretHesapla(endToNodeDistance));
						headerDoc.insertString(headerDoc.getLength(), odemeSonucu + "\n\n", headerStyle);


						headerDoc.insertString(headerDoc.getLength(), "Tüm Olası Rotalar (" + allRoutes.size() + "):", greenStyle);
						
						for (int i = 0; i < allRoutes.size(); i++) {
							boolean ozelGunMu = ozelDayCounter;
							List<Node> route = allRoutes.get(i);

							JPanel routePanel = new JPanel();
							routePanel.setLayout(new BorderLayout());

							String routeTitle = "Rota " + (i + 1) + ": ";

							routePanel.setBorder(BorderFactory.createTitledBorder(
								BorderFactory.createLineBorder(yesil, 2),
								routeTitle,
								TitledBorder.LEFT,
								TitledBorder.TOP,
								new Font("Dialog", Font.BOLD, 14),
								yesil
							));

							JTextPane routeTextPane = new JTextPane();
							routeTextPane.setBackground(Color.WHITE);
							routeTextPane.setEditable(false);

							StyledDocument routeDoc = routeTextPane.getStyledDocument();
							Style routeStyle = routeTextPane.addStyle("RouteStyle", null);
							Style routeHighlightStyle = routeTextPane.addStyle("RouteHighlightStyle", null);
							
							StyleConstants.setFontSize(routeStyle, 15);
							StyleConstants.setFontSize(routeHighlightStyle, 15);
							StyleConstants.setForeground(routeHighlightStyle, sari);


							formatRouteInfo RouteInfo = new formatRouteInfo();
							String routeInfo = RouteInfo.formatRouteInfo(route, yolcu, ozelGunMu);
							routeDoc.insertString(routeDoc.getLength(), routeInfo, routeStyle);

							returnIdealRoutes findBestRoute = new returnIdealRoutes();
							Map<String, List<Node>> bestRoutes = findBestRoute.returnIdealRoutes(allRoutes);
							List<Node> cheapestRoute = bestRoutes.get("cost");
							List<Node> shortestTimeRoute = bestRoutes.get("duration");
							List<Node> leastTransfersRoute = bestRoutes.get("transfer");
							
							List<String> routeAttributes = new ArrayList<>();
							
							if (route.equals(cheapestRoute)) {
								routeAttributes.add("En ucuz");
							}
							if (route.equals(leastTransfersRoute)) {
								routeAttributes.add("En az aktarmalı");
							}
							if (route.equals(shortestTimeRoute)) {
								routeAttributes.add("En kısa süreli");
							}
							if (route.equals(shortestRoute)) {
								routeAttributes.add("En kısa");
							}

							calculateRouteAttributes getRouteAttributes = new calculateRouteAttributes();

							priceWithoutTaxi = getRouteAttributes.calculateTotalPrice(route);
							totalDistance = getRouteAttributes.calculateTotalDistance(route) + BaslangicMesafe + VarisMesafe;
							totalTime = getRouteAttributes.calculateTOAwithoutTaxiAndWalk(route) + BaslangicSure + VarisSure;
							

							String ucretText;
							
							if(ozelGunMu){
								ucretText = ("Toplam Ücret: Özel günlerde seyahat ücretsizdir.");
								priceWithoutTaxi = 0;
							}
							else {
								if(paymentType=="KentKart") {
									switch ((int) (yolcu.getIndirimOrani() * 100)) {
										case 0:
											ucretText = ("Toplam Ücret (indirim " + passenger + " için uygulanmadı): " + priceWithoutTaxi + " TL");
											break;

										case 100:
											ucretText = ("Toplam Ücret: " + passenger + " yolcular ücretsiz seyahat edebilirler");
											break;

										default:
											ucretText = ("Toplam Ücret (" + passenger + " için indirim uygulandı): " + df.format(biletHesapla.calculatePrice(yolcu, priceWithoutTaxi)) + " TL");
									}
								}
								else{
									ucretText = ("Toplam Ücret: "+ priceWithoutTaxi +" (Kentkart kullanılmadığı için indirim yapılamadı)");
								}
							}
							

							if(paymentType=="KentKart" && !ozelGunMu) finalPriceWithDiscount = biletHesapla.calculatePrice(yolcu, priceWithoutTaxi) + BaslangicUcret +VarisUcret;
							else if (!ozelGunMu)finalPriceWithDiscount = priceWithoutTaxi + BaslangicUcret + VarisUcret;
							else finalPriceWithDiscount = BaslangicUcret + VarisUcret;
							
							routeDoc.insertString(routeDoc.getLength(), "\n"+ ucretText, routeStyle);
							routeDoc.insertString(routeDoc.getLength(), "\nToplam Mesafe: " + df.format(getRouteAttributes.calculateTotalDistance(route)) + " Km", routeStyle);
							routeDoc.insertString(routeDoc.getLength(), "\nToplam Süre: " + df.format(getRouteAttributes.calculateTOAwithoutTaxiAndWalk(route)) + " Dk", routeStyle);
							
							routeDoc.insertString(routeDoc.getLength(), "\n\nToplam Ücret (taksi dahil): " + df.format(finalPriceWithDiscount) + " TL", routeStyle);
							routeDoc.insertString(routeDoc.getLength(), "\nToplam Mesafe (taksi veya yürüme dahil): " + df.format(totalDistance) + " Km", routeStyle);
							routeDoc.insertString(routeDoc.getLength(), "\nToplam Süre (taksi veya yürüme dahil): " + df.format(totalTime) + " Dk\n", routeStyle);

							int totalMinutes = (hour * 60) + minute + (int)totalTime;
							int arrivalHour = totalMinutes / 60;
							int arrivalMinute = totalMinutes % 60;

							arrivalHour = arrivalHour % 24;

							String arrivalTime = String.format("%02d:%02d", arrivalHour, arrivalMinute);
							routeDoc.insertString(routeDoc.getLength(),"Tahmini varış saati: "+arrivalTime+"\n",routeStyle);

							JPanel ekpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
							ekpanel.setBackground(Color.WHITE);

							JButton bakiyeSorgulaButton = null;

							OdemeYontemi odemeYontemi = OdemeYontemiFactory.factoryMethod(paymentType, finalPriceWithDiscount);
							routeDoc.insertString(routeDoc.getLength(),odemeYontemi.odemeYap()+"\n",routeStyle);

							if (odemeYontemi instanceof KentKart) {
								KentKart kentKart = (KentKart) odemeYontemi;
								String bakiyeBilgisi = kentKart.bakiyeSorgula();

								// Create the button dynamically for Kent Kart balance inquiry
								bakiyeSorgulaButton = new JButton("Bakiye Sorgula");
								bakiyeSorgulaButton.setBackground(new Color(45, 128, 188));
								bakiyeSorgulaButton.setForeground(Color.WHITE);
								bakiyeSorgulaButton.setFocusPainted(false);
								bakiyeSorgulaButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

								final String finalBakiyeBilgisi = bakiyeBilgisi;
								bakiyeSorgulaButton.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										JOptionPane.showMessageDialog(
												null,
												finalBakiyeBilgisi,
												"Bakiye Bilgisi",
												JOptionPane.INFORMATION_MESSAGE
										);
									}
								});

							}




							returnRouteVehicleTypesText routeVhc = new returnRouteVehicleTypesText();
							String routeVehiclesText = routeVhc.returnRouteVehicleTypesText(route);
							
							JLabel label = new JLabel(routeVehiclesText, JLabel.CENTER);
							Color kirmizi = new Color(45, 128, 188);

							label.setBorder(BorderFactory.createRaisedBevelBorder());
							label.setOpaque(true);
							label.setForeground(Color.WHITE);
							label.setBackground(kirmizi);
							label.setPreferredSize(new Dimension(120, 26));
							ekpanel.add(label);
							
							Color attributeColor = new Color(36, 149, 122);
							for (String attribute : routeAttributes) {
								JLabel attributeLabel = new JLabel(attribute, JLabel.CENTER);
								attributeLabel.setBorder(BorderFactory.createRaisedBevelBorder());
								attributeLabel.setOpaque(true);
								attributeLabel.setForeground(Color.WHITE);
								attributeLabel.setBackground(attributeColor);
								attributeLabel.setPreferredSize(new Dimension(100, 26));
								ekpanel.add(attributeLabel);
							}

							JButton selectRouteButton = new JButton("Bu Rotayı Seç");
							selectRouteButton.setBackground(new Color(36, 149, 122));
							selectRouteButton.setForeground(Color.WHITE);
							selectRouteButton.setFocusPainted(false);
							selectRouteButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
							
							final int routeIndex = i;
							selectRouteButton.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									currentRoute = allRoutes.get(routeIndex);
									highlightEnabled = true;
									updateMap();
									JOptionPane.showMessageDialog(
											frame,
											"Rota " + (routeIndex + 1) + " seçildi ve haritada işaretlendi.",
											"Rota Seçildi",
											JOptionPane.INFORMATION_MESSAGE
									);
								}
							});
							
							
							JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							buttonPanel.setBackground(Color.WHITE);
							if (bakiyeSorgulaButton != null) {
								buttonPanel.add(bakiyeSorgulaButton);
							}
							buttonPanel.add(selectRouteButton);
							routePanel.add(buttonPanel, BorderLayout.NORTH);

							routePanel.add(routeTextPane, BorderLayout.CENTER);
							routePanel.add(ekpanel, BorderLayout.SOUTH);

							routesPanel.add(routePanel);

							if (i < allRoutes.size() - 1) {
								routesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
							}
						}

						routesPanel.revalidate();
						routesPanel.repaint();
						contentPanel.revalidate();
						contentPanel.repaint();
						
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}
				}
			});

			yürümeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					try {
						headerDoc.remove(0, headerDoc.getLength());
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}

					routesPanel.removeAll();
					
					double mesafe = calculateDistance(startLat, startLon, endLat, endLon);
					Vehicle yurume =  VehicleFactory.createVehicle("walk", 5.0);
					int sure = (int) Math.round(yurume.sureHesapla(mesafe));
					int saat = sure / 60;
					int dakika = sure % 60;

					int totalMinutes = (hour * 60) + minute + sure;
					int arrivalHour = totalMinutes / 60;
					int arrivalMinute = totalMinutes % 60;
					arrivalHour = arrivalHour % 24;
					String varisSaati = String.format("%02d:%02d", arrivalHour, arrivalMinute);


					try {
						headerDoc.insertString(headerDoc.getLength(), "Rota Bilgileri:\n\n", greenStyle);
						headerDoc.insertString(headerDoc.getLength(),
								"Toplam mesafe: " + df.format(mesafe) + " km. Yürüyerek gidilecek.\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(), "Yürüme süresi: " + saat + " saat " + dakika + " dakika\n",
								headerStyle);

						JPanel yurumePaneli = new JPanel();
						yurumePaneli.setLayout(new BorderLayout());

						yurumePaneli.setBorder(BorderFactory.createTitledBorder(
							BorderFactory.createLineBorder(yesil, 2),
							"Yürüme Rotası",
							TitledBorder.LEFT,
							TitledBorder.TOP,
							new Font("Dialog", Font.BOLD, 14),
							yesil
						));

						JTextPane yurumeBilgiPane = new JTextPane();
						yurumeBilgiPane.setBackground(Color.WHITE);
						yurumeBilgiPane.setEditable(false);
						
						StyledDocument yurumeBilgiDoc = yurumeBilgiPane.getStyledDocument();
						Style yurumeBilgiStyle = yurumeBilgiPane.addStyle("YurumeBilgiStyle", null);
						StyleConstants.setFontSize(yurumeBilgiStyle, 15);
						String a = yurume.processPayment(paymentType,0);
						
						yurumeBilgiDoc.insertString(0, 
							"Başlangıç: (" + df.format(startLat) + ", " + df.format(startLon) + ")\n" +
							"Hedef: (" + df.format(endLat) + ", " + df.format(endLon) + ")\n" +
							"Toplam mesafe: " + df.format(mesafe) + " km\n" +
							"Tahmini yürüme süresi: " + saat + " saat " + dakika + " dakika\n"+
							"Tahmini varış saati: " + varisSaati + "\n"+
									""+ a,
							yurumeBilgiStyle);
							
						yurumePaneli.add(yurumeBilgiPane, BorderLayout.CENTER);
						routesPanel.add(yurumePaneli);
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}

					routesPanel.revalidate();
					routesPanel.repaint();
					contentPanel.revalidate();
					contentPanel.repaint();
				}
			});

			taksiButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					try {
						headerDoc.remove(0, headerDoc.getLength());
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}

					routesPanel.removeAll();
					
					double mesafe = calculateDistance(startLat, startLon, endLat, endLon);
					Vehicle taksi =  VehicleFactory.createVehicle("taksi", 10.0, 4.0, 60.0);
					double ucret1 = taksi.ucretHesapla(mesafe);
					int taksiSuresi = (int) Math.round(taksi.sureHesapla(mesafe));
					int saat = taksiSuresi / 60;
					int dakika = taksiSuresi % 60;

					int totalMinutes = (hour * 60) + minute + taksiSuresi;
					int arrivalHour = totalMinutes / 60;
					int arrivalMinute = totalMinutes % 60;
					arrivalHour = arrivalHour % 24;
					String varisSaati = String.format("%02d:%02d", arrivalHour, arrivalMinute);



					try {
						headerDoc.insertString(headerDoc.getLength(), "Rota Bilgileri:\n\n", greenStyle);
						headerDoc.insertString(headerDoc.getLength(),
							"Toplam mesafe: " + df.format(mesafe) + " km. Taksi ile gidilecek.\n", headerStyle);
						headerDoc.insertString(headerDoc.getLength(), "Taksi süresi: " + saat + " saat " + dakika + " dakika\n",
							headerStyle);
						headerDoc.insertString(headerDoc.getLength(), "Taksi ücreti: " + df.format(ucret1) + " TL\n", headerStyle);

						JPanel taksiPaneli = new JPanel();
						taksiPaneli.setLayout(new BorderLayout());

						taksiPaneli.setBorder(BorderFactory.createTitledBorder(
							BorderFactory.createLineBorder(yesil, 2),
							"Taksi Rotası",
							TitledBorder.LEFT,
							TitledBorder.TOP,
							new Font("Dialog", Font.BOLD, 14),
							yesil
						));

						JTextPane taksiBilgiPane = new JTextPane();
						taksiBilgiPane.setBackground(Color.WHITE);
						taksiBilgiPane.setEditable(false);
						
						StyledDocument taksiBilgiDoc = taksiBilgiPane.getStyledDocument();
						Style taksiBilgiStyle = taksiBilgiPane.addStyle("TaksiBilgiStyle", null);
						StyleConstants.setFontSize(taksiBilgiStyle, 15);

						String odemeMetni = "";

						odemeMetni = taksi.processPayment(paymentType, taksi.ucretHesapla(mesafe));




						taksiBilgiDoc.insertString(0, 
							"Başlangıç: (" + df.format(startLat) + ", " + df.format(startLon) + ")\n" +
							"Hedef: (" + df.format(endLat) + ", " + df.format(endLon) + ")\n" +
							"Toplam mesafe: " + df.format(mesafe) + " km\n" +
							"Tahmini taksi süresi: " + saat + " saat " + dakika + " dakika\n" +
							"Tahmini varış saati: " + varisSaati + "\n"+
							"Taksi ücreti: " + df.format(ucret1) + " TL\n" +
							"Ödeme: " + odemeMetni + "\n", 
							taksiBilgiStyle);
							
						taksiPaneli.add(taksiBilgiPane, BorderLayout.CENTER);
						routesPanel.add(taksiPaneli);
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}

					routesPanel.revalidate();
					routesPanel.repaint();
					contentPanel.revalidate();
					contentPanel.repaint();
				}
			});





			JOptionPane.showMessageDialog(frame, mainScrollPane, "Rota Bilgileri", JOptionPane.INFORMATION_MESSAGE);

			currentRoute = shortestRoute;
			highlightEnabled = true;
			updateMap();

			calculateRouteAttributes routeAtt = new calculateRouteAttributes();

			System.out.println("Route finding complete:");
			System.out.println("Found " + allRoutes.size() + " possible routes");
			System.out.println("Shortest route distance: " + routeAtt.calculateTotalDistance(shortestRoute) + " km");
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frame, "Lütfen geçerli koordinat değerleri giriniz.", "Hata",
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Rota hesaplanırken bir hata oluştu: " + e.getMessage(), "Hata",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		controlPanel.setBackground(new Color(240, 240, 240));
		controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
		controlPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH - 300, 50)); // Fixed height

		JButton zoomInButton = new JButton("Zoom In");
		zoomInButton.addActionListener(e -> {
			mapViewer.setZoom(Math.max(0, mapViewer.getZoom() - 1));
			System.out.println("Zoomed in to level: " + mapViewer.getZoom());
			mapViewer.repaint();
			refreshDisplay();
		});

		JButton zoomOutButton = new JButton("Zoom Out");
		zoomOutButton.addActionListener(e -> {
			mapViewer.setZoom(Math.min(15, mapViewer.getZoom() + 1));
			System.out.println("Zoomed out to level: " + mapViewer.getZoom());
			mapViewer.repaint();
			refreshDisplay();
		});

		JButton resetButton = new JButton("Reset View");
		resetButton.addActionListener(e -> {
			if (!nodePositions.isEmpty()) {
				double totalLat = 0, totalLon = 0;
				for (GeoPosition pos : nodePositions.values()) {
					totalLat += pos.getLatitude();
					totalLon += pos.getLongitude();
				}

				GeoPosition center = new GeoPosition(totalLat / nodePositions.size(), totalLon / nodePositions.size());

				mapViewer.setAddressLocation(center);
				mapViewer.setZoom(7);
				System.out.println("View reset to center: " + center.getLatitude() + ", " + center.getLongitude());
				mapViewer.repaint();
				refreshDisplay();
			}
		});

		JButton highlightButton = new JButton("Highlight Transfers");
		highlightButton.addActionListener(e -> {
			highlightEnabled = !highlightEnabled;
			if (highlightEnabled) {
				highlightButton.setText("Remove Highlight");
			} else {
				highlightButton.setText("Highlight Transfers");
			}
			updateMap();
			refreshDisplay();
		});

		JButton debugButton = new JButton("Force Repaint");
		debugButton.addActionListener(e -> {
			System.out.println("Forcing repaint...");
			mapViewer.repaint();
			refreshDisplay();
		});

		controlPanel.add(zoomInButton);
		controlPanel.add(zoomOutButton);
		controlPanel.add(resetButton);
		controlPanel.add(highlightButton);
		controlPanel.add(debugButton);

		return controlPanel;
	}

	private void updateMap() {
		try {
			Set<MyWaypoint> waypoints = new HashSet<>();

			for (Node node : graph) {
				if (nodePositions.containsKey(node.getId())) {
					boolean isTransfer = "transfer".equals(node.getAttribute("type"));
					Color color = isTransfer && highlightEnabled ? Color.RED : Color.GRAY;
					int size = isTransfer && highlightEnabled ? 12 : 8;
					String name = node.hasAttribute("ui.label") ? node.getAttribute("ui.label", String.class)
							: node.getId();

					GeoPosition pos = nodePositions.get(node.getId());
					System.out.println("Adding waypoint for node " + node.getId() + " at position: " + pos.getLatitude()
							+ ", " + pos.getLongitude());

					waypoints.add(new MyWaypoint(node.getId(), name, color, size, pos));
				} else {
					System.out.println("No position found for node: " + node.getId());
				}
			}

			if (waypoints.isEmpty()) {
				System.err.println("WARNING: No waypoints created! Check if nodes have lat/lon attributes.");
			}

			WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<>();
			waypointPainter.setWaypoints(waypoints);
			waypointPainter.setRenderer(new MyWaypointRenderer(hoveredNode));

			EdgePainter edgePainter = new EdgePainter(graph, nodePositions, highlightEnabled);

			List<Painter<JXMapViewer>> painters = new ArrayList<>();
			painters.add(edgePainter);
			painters.add(waypointPainter);

			CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
			mapViewer.setOverlayPainter(painter);

			mapViewer.repaint();
			System.out.println("Map updated with " + waypoints.size() + " waypoints");

		} catch (Exception e) {
			System.err.println("Error updating map: " + e.getMessage());
			e.printStackTrace();
		}
		refreshDisplay();
	}

	private static class MyWaypoint extends DefaultWaypoint {
		private final String id;
		private final String name;
		private final Color color;
		private final int size;

		public MyWaypoint(String id, String name, Color color, int size, GeoPosition pos) {
			super(pos);
			this.id = id;
			this.name = name;
			this.color = color;
			this.size = size;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public Color getColor() {
			return color;
		}

		public int getSize() {
			return size;
		}
	}


	private class MyWaypointRenderer implements WaypointRenderer<MyWaypoint> {
		private Node hoveredNode;

		public MyWaypointRenderer(Node hoveredNode) {
			this.hoveredNode = hoveredNode;
		}

		@Override
		public void paintWaypoint(Graphics2D g, JXMapViewer map, MyWaypoint waypoint) {
			try {
				Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());

				int x = (int) (point.getX() - map.getViewportBounds().getX());
				int y = (int) (point.getY() - map.getViewportBounds().getY());

				boolean isHovered = hoveredNode != null && hoveredNode.getId().equals(waypoint.getId());

				boolean isInRoute = false;
				if (currentRoute != null) {
					for (Node node : currentRoute) {
						if (node.getId().equals(waypoint.getId())) {
							isInRoute = true;
							break;
						}
					}
				}


				int size = waypoint.getSize() * 5;

				if (isHovered) {
					size += 10;
					g.setColor(Color.YELLOW);
				} else if (isInRoute) {
					size += 5;
					g.setColor(Color.GREEN.darker());
				} else {
					g.setColor(new Color(255, 100, 100));
				}

				g.fillOval(x - size / 2, y - size / 2, size, size);

				g.setColor(Color.BLACK);
				g.setStroke(new BasicStroke(2.5f));
				g.drawOval(x - size / 2, y - size / 2, size, size);

				String name = waypoint.getName();
				FontMetrics fm = g.getFontMetrics();
				int textWidth = fm.stringWidth(name);
				int textHeight = fm.getHeight();

				g.setColor(new Color(0, 0, 0, 200));
				g.fillRect(x - textWidth / 2 - 4, y - size / 2 - textHeight - 4, textWidth + 8, textHeight + 4);

				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(name, x - textWidth / 2, y - size / 2 - 6);

				if (isHovered || isInRoute) {
					System.out.println("Drawing node " + waypoint.getId() + " at position: " + x + ", " + y);
				}
			} catch (Exception e) {
				System.err.println("Error drawing waypoint: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}


	private class EdgePainter implements Painter<JXMapViewer> {
		private final Graph graph;
		private final Map<String, GeoPosition> nodePositions;
		private final boolean highlightEnabled;

		public EdgePainter(Graph graph, Map<String, GeoPosition> nodePositions, boolean highlightEnabled) {
			this.graph = graph;
			this.nodePositions = nodePositions;
			this.highlightEnabled = highlightEnabled;
		}

		@Override
		public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			graph.edges().forEach(edge -> {
				Node sourceNode = edge.getSourceNode();
				Node targetNode = edge.getTargetNode();

				if (currentRoute != null && isEdgeInRoute(sourceNode, targetNode, currentRoute)) {
					return;
				}

				if (nodePositions.containsKey(sourceNode.getId()) && nodePositions.containsKey(targetNode.getId())) {

					GeoPosition sourcePos = nodePositions.get(sourceNode.getId());
					GeoPosition targetPos = nodePositions.get(targetNode.getId());

					Point2D sourcePoint = map.getTileFactory().geoToPixel(sourcePos, map.getZoom());
					Point2D targetPoint = map.getTileFactory().geoToPixel(targetPos, map.getZoom());

					int x1 = (int) (sourcePoint.getX() - map.getViewportBounds().getX());
					int y1 = (int) (sourcePoint.getY() - map.getViewportBounds().getY());
					int x2 = (int) (targetPoint.getX() - map.getViewportBounds().getX());
					int y2 = (int) (targetPoint.getY() - map.getViewportBounds().getY());

					boolean isTransferEdge = "transfer".equals(edge.getAttribute("type"));
					if (isTransferEdge && highlightEnabled) {
						g.setColor(Color.RED);
						g.setStroke(new BasicStroke(2.0f));
					} else {
						g.setColor(Color.DARK_GRAY);
						g.setStroke(new BasicStroke(1.0f));
					}

					g.drawLine(x1, y1, x2, y2);

					if (!isTransferEdge && edge.hasAttribute("distance")) {
						String distance = edge.getAttribute("distance").toString();
						g.setColor(Color.BLUE);
						g.drawString(distance, (x1 + x2) / 2, (y1 + y2) / 2);
					}

					drawArrowTip(g, x1, y1, x2, y2);
				}
			});

			if (currentRoute != null && highlightEnabled) {
				for (int i = 0; i < currentRoute.size() - 1; i++) {
					Node sourceNode = currentRoute.get(i);
					Node targetNode = currentRoute.get(i + 1);

					if (nodePositions.containsKey(sourceNode.getId())
							&& nodePositions.containsKey(targetNode.getId())) {

						GeoPosition sourcePos = nodePositions.get(sourceNode.getId());
						GeoPosition targetPos = nodePositions.get(targetNode.getId());

						Point2D sourcePoint = map.getTileFactory().geoToPixel(sourcePos, map.getZoom());
						Point2D targetPoint = map.getTileFactory().geoToPixel(targetPos, map.getZoom());

						int x1 = (int) (sourcePoint.getX() - map.getViewportBounds().getX());
						int y1 = (int) (sourcePoint.getY() - map.getViewportBounds().getY());
						int x2 = (int) (targetPoint.getX() - map.getViewportBounds().getX());
						int y2 = (int) (targetPoint.getY() - map.getViewportBounds().getY());

						g.setColor(Color.GREEN.darker());
						g.setStroke(new BasicStroke(4.0f));
						g.drawLine(x1, y1, x2, y2);

						drawArrowTip(g, x1, y1, x2, y2);
					}
				}
			}
		}

		private boolean isEdgeInRoute(Node source, Node target, List<Node> route) {
			for (int i = 0; i < route.size() - 1; i++) {
				if (route.get(i).equals(source) && route.get(i + 1).equals(target)) {
					return true;
				}
			}
			return false;
		}

		private void drawArrowTip(Graphics2D g, int x1, int y1, int x2, int y2) {

			double dx = x2 - x1;
			double dy = y2 - y1;
			double length = Math.sqrt(dx * dx + dy * dy);


			if (length > 0) {
				dx = dx / length;
				dy = dy / length;
			}


			int tipX = (int) (x2 - 6 * dx);
			int tipY = (int) (y2 - 6 * dy);

			double perpX = -dy;
			double perpY = dx;

			int wing1X = (int) (tipX + 3 * perpX - 3 * dx);
			int wing1Y = (int) (tipY + 3 * perpY - 3 * dy);
			int wing2X = (int) (tipX - 3 * perpX - 3 * dx);
			int wing2Y = (int) (tipY - 3 * perpY - 3 * dy);

			int[] xPoints = { x2, wing1X, wing2X };
			int[] yPoints = { y2, wing1Y, wing2Y };
			g.fillPolygon(xPoints, yPoints, 3);
		}
	}

	private void refreshDisplay() {
		mapViewer.repaint();
		frame.getGlassPane().repaint();
	}
	public class UlasimStratejisi {
		public static Vehicle uygunArac(double mesafe) {
			if (mesafe > 3.0) {
				return VehicleFactory.createVehicle("taksi", 10.0, 4.0, 60.0);
			} else {
				return VehicleFactory.createVehicle("walk", 5.0);
			}
		}
	}

}