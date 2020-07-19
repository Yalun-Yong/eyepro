package com.allan.application;

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import com.allan.controller.Controller;
import com.allan.controller.PopupController;
import com.allan.dao.PropertiesDAO;
import com.allan.domain.Property;
import com.allan.utils.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import org.xml.sax.SAXException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
	private TrayIcon trayIcon;
	private Property property;

	public void init() throws ParserConfigurationException, SAXException, IOException{

	}
	@Override
	public void start(Stage primaryStage) throws IOException {
		PropertiesDAO.getInstance().load();
		this.property = Property.getInstance();
		FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/com/allan/fxml/scene.fxml"));
		setUserAgentStylesheet(STYLESHEET_CASPIAN);
		Parent root = fxmlloader.load();
		Scene scene = new Scene(root);
//		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Controller controller = fxmlloader.getController();
		//传递primaryStage参数给Controller
		controller.setStage(primaryStage);
//		primaryStage.getIcons().add(new Image("/application/eye_tray.png"));
		primaryStage.setTitle("EyePro");
		//terminate the all threads when close button clicked.
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent event){
				//System.exit(0);
				primaryStage.hide();
			}
		});
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.hide();
		Platform.setImplicitExit(false);
		//系统托盘
        enableTray(primaryStage);

		Timer timer = new Timer();
        TimerTask  timerTask = new TimerTask (){
            public void run() {
//            	-----------半点提醒--------------------------------------------
				if(TimeUtil.getCurrentTimeString().equals("30:00")){
					Platform.runLater(()->{
						try {
							if(property.isPopUpSwitch()) {
								showTimedDialog(120000);
								setUserAgentStylesheet(STYLESHEET_CASPIAN);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					SoundManager.playSound(SoundManager.HALF);
//					-------------整点提醒-------------------------
				}else if(TimeUtil.getCurrentTimeString().equals("00:00")){
					Platform.runLater(()->{
						try {
							if(property.isPopUpSwitch()) {
								showTimedDialog(300000);
								setUserAgentStylesheet(STYLESHEET_CASPIAN);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					SoundManager.playSound(SoundManager.SHARP);
				}
            }
        };
        timer.schedule (timerTask, 0, 1000);
	}


	public void showTimedDialog(long time) throws IOException {
		FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/com/allan/fxml/popup.fxml"));
		Parent root = fxmlloader.load();
		Scene scene = new Scene(root);
//		scene.setFill(null);
		PopupController controller = fxmlloader.getController();
		Stage stage = new Stage();
		stage.setAlwaysOnTop(true);
		stage.getIcons().add(new Image("/com/allan/pics/tr.png"));
		stage.setScene(scene);
		controller.setStage(stage);

	    Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
//		stage.setX(primaryScreenBounds.getWidth() - 410);
		stage.setX(- 8);
		stage.setY(primaryScreenBounds.getHeight() - 315);
		//按esc最小化
		stage.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent event) -> {
			if (KeyCode.ESCAPE == event.getCode()) {
				stage.setIconified(true);
			}
		});
	    stage.show();

	    Thread thread = new Thread(() -> {
	        try {
	            Thread.sleep(time);
	            if (stage.isShowing()) {
	                Platform.runLater(() ->
	                stage.close());
	            }
				SoundManager.playSoundTimesUp();
	        } catch (Exception exp) {
	            exp.printStackTrace();
	        }
	    });
	    thread.setDaemon(true);
	    thread.start();
	}

	private void enableTray(final Stage stage) {
		PopupMenu popupMenu = new PopupMenu();
		java.awt.MenuItem openItem = new java.awt.MenuItem("Show");
		java.awt.MenuItem hideItem = new java.awt.MenuItem("Properties");
		java.awt.MenuItem quitItem = new java.awt.MenuItem("Exit");
		ActionListener acl = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				java.awt.MenuItem item = (java.awt.MenuItem) e.getSource();
				Platform.setImplicitExit(false); //多次使用显示和隐藏设置false

				if (item.getLabel().equals("Exit")) {
					SystemTray.getSystemTray().remove(trayIcon);
//					Platform.exit();
					System.exit(0);
					return;
				}
				if (item.getLabel().equals("Show")) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							stage.show();
						}
					});
				}
				if (item.getLabel().equals("Properties")) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							try {
								Stage stage = new Stage();
								stage.initModality(Modality.APPLICATION_MODAL);
								FXMLLoader fxmlloader =
									new FXMLLoader(getClass().getResource("/com/allan/fxml/properties.fxml"));
								Parent root;
								root = fxmlloader.load();
								Scene scene = new Scene(root);
								stage.setResizable(false);
								stage.setScene(scene);
								stage.setTitle("Properties");
								stage.show();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		};
		//双击事件方法
		MouseListener sj = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Platform.setImplicitExit(false); //多次使用显示和隐藏设置false
				if (e.getClickCount() == 2) {
					if (stage.isShowing()) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								stage.hide();
							}
						});
					}else{
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								stage.show();
							}
						});
					}
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
			}
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
		};

		openItem.addActionListener(acl);
		quitItem.addActionListener(acl);
		hideItem.addActionListener(acl);

		popupMenu.add(openItem);
		popupMenu.add(hideItem);
		popupMenu.add(quitItem);

		try {
			SystemTray tray = SystemTray.getSystemTray();
			BufferedImage image = ImageIO.read(Main.class
					.getResourceAsStream("/com/allan/pics/hiei.png"));
			trayIcon = new TrayIcon(image, "EyePro", popupMenu);
			trayIcon.setToolTip("EyePro");
			tray.add(trayIcon);
			trayIcon.addMouseListener(sj);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void main(String[] args) {
		launch(args);
	}

}
