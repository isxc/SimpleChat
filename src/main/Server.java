package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @Description: 服务端
 * @author xiaocui
 *
 */
public class Server extends JFrame {

	private SSLServerSocket serverSocket;
	private final static int PORT = 9999;
	private UserDatabase userDatabase = new UserDatabase();
	private final UserManager userManager = new UserManager();
	final DefaultTableModel onlineUsersDtm = new DefaultTableModel();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private final JPanel contentPane;
	private final JTable tableOnlineUsers;
	private final JTextPane textPaneMsgRecord;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Server frame = new Server();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Server() {

		setTitle("\u670D\u52A1\u5668");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 561, 403);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPaneNorth = new JSplitPane();
		splitPaneNorth.setResizeWeight(0.5);
		contentPane.add(splitPaneNorth, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setPreferredSize(new Dimension(100, 300));
		scrollPaneMsgRecord.setViewportBorder(
				new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneNorth.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setPreferredSize(new Dimension(100, 100));
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setPreferredSize(new Dimension(100, 300));
		splitPaneNorth.setRightComponent(scrollPaneOnlineUsers);

		onlineUsersDtm.addColumn("用户名");
		onlineUsersDtm.addColumn("IP");
		onlineUsersDtm.addColumn("端口");
		onlineUsersDtm.addColumn("登录时间");
		tableOnlineUsers = new JTable(onlineUsersDtm);
		tableOnlineUsers.setPreferredSize(new Dimension(100, 270));
		tableOnlineUsers.setFillsViewportHeight(true);
		scrollPaneOnlineUsers.setViewportView(tableOnlineUsers);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton btnStart = new JButton("\u542F\u52A8");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					serverSocket = createSSLServerSocket();
					String msgRecord = dateFormat.format(new Date()) + " 服务器启动成功" + "\r\n";
					addMsgRecord(msgRecord, Color.red, 12, false, false);
					new Thread() {
						@Override
						public void run() {
							while (true) {
								try {
									Socket socket = serverSocket.accept();
									UserHandler userHandler = new UserHandler(socket);
									new Thread(userHandler).start();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
					}.start();
					btnStart.setEnabled(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		});
		panelSouth.add(btnStart);

		JButton btnOffline = new JButton("\u5F3A\u5236\u4E0B\u7EBF");
		btnOffline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int number = tableOnlineUsers.getSelectedRow();
				if (number == -1) {
					JOptionPane.showMessageDialog(null, "请选择一个用户");
				} else {
					String srcUser = (String) onlineUsersDtm.getValueAt(number, 0);
					ForcedMessage forcedMessage = new ForcedMessage(srcUser, "", true);
					try {
						ObjectOutputStream oos = userManager.getUserOos(srcUser);
						synchronized (oos) {
							oos.writeObject(forcedMessage);
							oos.flush();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					UserStateMessage msg = new UserStateMessage(srcUser, "", false);
					String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")"
							+ "被强制下线!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					String[] users = userManager.getAllUsers();
					for (String user : users) {
						try {
							ObjectOutputStream objectOutputStream = userManager.getUserOos(user);
							synchronized (objectOutputStream) {
								objectOutputStream.writeObject(msg);
								objectOutputStream.flush();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					userManager.removeUser(srcUser);
					for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
						if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
							onlineUsersDtm.removeRow(i);
						}
					}
				}

			}
		});
		panelSouth.add(btnOffline);
	}

	private void addMsgRecord(final String msgRecord, Color msgColor, int fontSize, boolean isItalic,
			boolean isUnderline) {
		final SimpleAttributeSet attrset = new SimpleAttributeSet();
		StyleConstants.setForeground(attrset, msgColor);
		StyleConstants.setFontSize(attrset, fontSize);
		StyleConstants.setUnderline(attrset, isUnderline);
		StyleConstants.setItalic(attrset, isItalic);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Document docs = textPaneMsgRecord.getDocument();
				try {
					docs.insertString(docs.getLength(), msgRecord, attrset);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}

	class UserHandler implements Runnable {
		private final Socket currentUserSocket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		public UserHandler(Socket currentUserSocket) {
			this.currentUserSocket = currentUserSocket;
			try {
				ois = new ObjectInputStream(currentUserSocket.getInputStream());
				oos = new ObjectOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					Message msg = (Message) ois.readObject();
					if (msg instanceof UserStateMessage) {
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof RegisterMessage) {
						processRegisterMessage((RegisterMessage) msg);
					} else if (msg instanceof LoginMessage) {
						processSignInMessage((LoginMessage) msg);
					} else if (msg instanceof FileMessage) {
						processFileMessage((FileMessage) msg);
					} else if (msg instanceof SendFileMessage) {
						processSendFileMessage((SendFileMessage) msg);
					} else {
						System.err.println("用户发来的消息格式错误!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("客户端退出");
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} finally {
				if (currentUserSocket != null) {
					try {
						currentUserSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void transferMsgToOtherUsers(Message msg) {
			String[] users = userManager.getAllUsers();
			for (String user : users) {
				if (userManager.getUserSocket(user) != currentUserSocket) {
					try {
						ObjectOutputStream o = userManager.getUserOos(user);
						synchronized (o) {
							o.writeObject(msg);
							o.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void processSignInMessage(LoginMessage msg) {
			String srcUser = msg.getSrcUser();
			String passWord = msg.getPasswd();
			if (userDatabase.checkUserPassword(srcUser, passWord) == true) {

				try {
					LoginJudgment signInJudgment = new LoginJudgment("local", srcUser, true);
					synchronized (oos) {
						oos.writeObject(signInJudgment);
						oos.flush();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				try {
					LoginJudgment signInJudgment = new LoginJudgment("local", srcUser, false);
					synchronized (oos) {
						oos.writeObject(signInJudgment);
						oos.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			if (msg.isUserOnline()) {
				try {
					if (userManager.hasUser(srcUser)) {
						LoginJudgment sInJudgment = new LoginJudgment("", srcUser, false);
						synchronized (oos) {
							oos.writeObject(sInJudgment);
							oos.flush();
						}
						return;
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String[] users = userManager.getAllUsers();
				try {
					for (String user : users) {
						UserStateMessage userStateMessage = new UserStateMessage(user, srcUser, true);
						synchronized (oos) {
							oos.writeObject(userStateMessage);
							oos.flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				transferMsgToOtherUsers(msg);
				onlineUsersDtm.addRow(new Object[] { srcUser, currentUserSocket.getInetAddress().getHostAddress(),
						currentUserSocket.getPort(), dateFormat.format(new Date()) });
				userManager.addUser(srcUser, currentUserSocket, oos, ois);
				String ip = currentUserSocket.getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "上线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
			} else {
				if (!userManager.hasUser(srcUser)) {
					System.err.println("用户未发送登录消息就发送了下线消息");
					return;
				}
				String ip = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")" + "下线了!\r\n";
				addMsgRecord(msgRecord, Color.green, 12, false, false);
				userManager.removeUser(srcUser);
				for (int i = 0; i < onlineUsersDtm.getRowCount(); i++) {
					if (onlineUsersDtm.getValueAt(i, 0).equals(srcUser)) {
						onlineUsersDtm.removeRow(i);
					}
				}
				transferMsgToOtherUsers(msg);
			}
		}

		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (userManager.hasUser(srcUser)) {
				final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "说: " + msgContent + "\r\n";
				addMsgRecord(msgRecord, Color.black, 12, false, false);
				if (msg.isPubChatMessage()) {
					transferMsgToOtherUsers(msg);
				} else {
					ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
					synchronized (objectOutputStream) {
						try {
							objectOutputStream.writeObject(msg);
							objectOutputStream.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}

					}
				}
			} else {
				System.err.println("用启未发送上线消息就直接发送了聊天消息");
				return;
			}
		}

		private void processRegisterMessage(RegisterMessage msg) {
			String srcUser = msg.getSrcUser();
			String passwdString = msg.getPasswd();
			String phoneString = msg.getPhone();
			try {
				if (userDatabase.insertUser(srcUser, passwdString, phoneString)) {
					userDatabase.showAllUsers();
					String ip = currentUserSocket.getInetAddress().getHostAddress();
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "(" + ip + ")"
							+ "注册成功了!\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
					RegisterJudgment reiJudgment = new RegisterJudgment("", srcUser, true);
					synchronized (oos) {
						oos.writeObject(reiJudgment);
						oos.flush();
					}
				} else {
					RegisterJudgment reiJudgment = new RegisterJudgment("", srcUser, false);
					synchronized (oos) {
						oos.writeObject(reiJudgment);
						oos.flush();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 处理文件发送请求消息
	private void processFileMessage(FileMessage msg) {
		// 接收文件的用户
		String dstUser = msg.getDstUser();
		try {
			ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
			synchronized (objectOutputStream) {
				objectOutputStream.writeObject(msg);
				objectOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processSendFileMessage(SendFileMessage msg) {
		String dstUser = msg.getDstUser();
		try {
			ObjectOutputStream objectOutputStream = userManager.getUserOos(dstUser);
			synchronized (objectOutputStream) {
				objectOutputStream.writeObject(msg);
				objectOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SSLServerSocket createSSLServerSocket() throws Exception {
		String keyStoreFile = "mykeys.keystore";
		String passphrase = "123456";
		KeyStore ks = KeyStore.getInstance("PKCS12");
		char[] password = passphrase.toCharArray();
		ks.load(new FileInputStream(keyStoreFile), password);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, password);// 区别：需要口令

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(kmf.getKeyManagers(), null, null);// !!!!!!!!!!!!!!!!!!!!!提问

		SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
		return (SSLServerSocket) serverSocket;
	}

}

// 管理在线用户信息
class UserManager {
	private final Hashtable<String, User> onLineUsers;

	public UserManager() {
		onLineUsers = new Hashtable<String, User>();
	}

	// 判断某用户是否在线
	public boolean hasUser(String userName) {
		return onLineUsers.containsKey(userName);
	}

	// 判断在线用户列表是否空
	public boolean isEmpty() {
		return onLineUsers.isEmpty();
	}

	// 获取在线用户的Socket的的输出流封装成的对象输出流
	public ObjectOutputStream getUserOos(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOos();
		}
		return null;
	}

	// 获取在线用户的Socket的的输入流封装成的对象输入流
	public ObjectInputStream getUserOis(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getOis();
		}
		return null;
	}

	// 获取在线用户的Socket
	public Socket getUserSocket(String userName) {
		if (hasUser(userName)) {
			return onLineUsers.get(userName).getSocket();
		}
		return null;
	}

	// 添加在线用户
	public boolean addUser(String userName, Socket userSocket) {
		if ((userName != null) && (userSocket != null)) {
			onLineUsers.put(userName, new User(userSocket));
			return true;
		}
		return false;
	}

	// 添加在线用户
	public boolean addUser(String userName, Socket userSocket, ObjectOutputStream oos, ObjectInputStream ios) {
		if ((userName != null) && (userSocket != null) && (oos != null) && (ios != null)) {
			onLineUsers.put(userName, new User(userSocket, oos, ios));
			return true;
		}
		return false;
	}

	// 删除在线用户
	public boolean removeUser(String userName) {
		if (hasUser(userName)) {
			onLineUsers.remove(userName);
			return true;
		}
		return false;
	}

	// 获取所有在线用户名
	public String[] getAllUsers() {
		String[] users = new String[onLineUsers.size()];
		int i = 0;
		for (String userName : onLineUsers.keySet()) {
			users[i++] = userName;
		}
		return users;
	}

	// 获取在线用户个数
	public int getOnlineUserCount() {
		return onLineUsers.size();
	}
}

class User {
	private final Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private final Date logonTime;

	public User(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		logonTime = new Date();
	}

	public User(Socket socket, ObjectOutputStream oos, ObjectInputStream ois, Date logonTime) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public Date getLogonTime() {
		return logonTime;
	}

}
