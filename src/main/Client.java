package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @Description: 客户端
 * @author xiaocui
 *
 */
public class Client extends JFrame {

	private final int port = 9999;
	private SSLSocket socket;
	private SSLServerSocket serverSocket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private String localUserName;
	private String localPasswd;
	private String fileSelectPath;
	private String fileSavePath;
	private String fileName;
	private long fileLength;
	private final DefaultListModel<String> onlinUserDlm = new DefaultListModel<String>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private final JPanel contentPane;
	private final JTextField textFieldUserName;
	private final JPasswordField passwordFieldPwd;
	private final JTextField textFieldMsgToSend;
	private final JTextPane textPaneMsgRecord;
	private final JList<String> listOnlineUsers;
	private final JButton btnLogon;
	private final JButton btnSendMsg;
	private final JButton btnSendFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Client frame = new Client();
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
	public Client() {
		setTitle("\u5BA2\u6237\u7AEF");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 612, 397);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panelNorth = new JPanel();
		panelNorth.setBorder(new EmptyBorder(0, 0, 5, 0));
		contentPane.add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));

		JLabel lblUserName = new JLabel("\u7528\u6237\u540D\uFF1A");
		panelNorth.add(lblUserName);

		textFieldUserName = new JTextField();
		panelNorth.add(textFieldUserName);
		textFieldUserName.setColumns(10);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut);

		JLabel lblPwd = new JLabel("\u53E3\u4EE4\uFF1A");
		panelNorth.add(lblPwd);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setColumns(10);
		panelNorth.add(passwordFieldPwd);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut_1);

		btnLogon = new JButton("\u767B\u5F55");
		btnLogon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnLogon.getText().equals("登录")) {
					localUserName = textFieldUserName.getText();
					localPasswd = passwordFieldPwd.getText();
					if (localUserName.length() > 0) {
						try {
							socket = createSSLSocket();
							oos = new ObjectOutputStream(socket.getOutputStream());
							ois = new ObjectInputStream(socket.getInputStream());
						} catch (UnknownHostException e1) {
							JOptionPane.showMessageDialog(null, "找不到服务器主机");
							e1.printStackTrace();
							System.exit(0);
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "服务器I/O错误，服务器未启动？");
							e1.printStackTrace();
							System.exit(0);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						LoginMessage LoginMessage = new LoginMessage(localUserName, "", localPasswd);
						try {
							oos.writeObject(LoginMessage);
							oos.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						try {
							LoginJudgment loginJudgment = (LoginJudgment) ois.readObject();
							if (loginJudgment.getJudgment() == true) {
								UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", true);
								oos.writeObject(userStateMessage);
								oos.flush();
								String msgRecord = dateFormat.format(new Date()) + " 登录成功\r\n";
								addMsgRecord(msgRecord, Color.red, 12, false, false);
								new Thread(new ListeningHandler()).start();
								btnLogon.setText("退出");
								btnSendFile.setEnabled(true);
								btnSendMsg.setEnabled(true);
							} else {
								JOptionPane.showMessageDialog(null, "密码错误！请重新输入");
							}
						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						JOptionPane.showMessageDialog(null, "请输入用户名和密码");
					}
				} else if (btnLogon.getText().equals("退出")) {
					if (JOptionPane.showConfirmDialog(null, "是否退出?", "退出确认",
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						UserStateMessage userStateMessage = new UserStateMessage(localUserName, "", false);
						try {
							synchronized (oos) {
								oos.writeObject(userStateMessage);
								oos.flush();
							}
							System.exit(0);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}

			}
		});
		panelNorth.add(btnLogon);

		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		panelNorth.add(horizontalStrut_4);

		JButton btnRegister = new JButton("\u6CE8\u518C");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnLogon.getText().equals("登录")) {
					Register register = new Register();
					register.setLocationRelativeTo(null);
					register.setVisible(true);
					register.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				} else {
					JOptionPane.showMessageDialog(null, "您已登录已登录用户");
				}
			}
		});
		panelNorth.add(btnRegister);

		JSplitPane splitPaneCenter = new JSplitPane();
		splitPaneCenter.setResizeWeight(1.0);
		contentPane.add(splitPaneCenter, BorderLayout.CENTER);

		JScrollPane scrollPaneMsgRecord = new JScrollPane();
		scrollPaneMsgRecord.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneCenter.setLeftComponent(scrollPaneMsgRecord);

		textPaneMsgRecord = new JTextPane();
		scrollPaneMsgRecord.setViewportView(textPaneMsgRecord);

		JScrollPane scrollPaneOnlineUsers = new JScrollPane();
		scrollPaneOnlineUsers.setViewportBorder(
				new TitledBorder(null, "\u5728\u7EBF\u7528\u6237", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPaneCenter.setRightComponent(scrollPaneOnlineUsers);

		listOnlineUsers = new JList<String>(onlinUserDlm);
		scrollPaneOnlineUsers.setViewportView(listOnlineUsers);

		JPanel panelSouth = new JPanel();
		panelSouth.setBorder(new EmptyBorder(5, 0, 0, 0));
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));

		textFieldMsgToSend = new JTextField();
		panelSouth.add(textFieldMsgToSend);
		textFieldMsgToSend.setColumns(10);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_2);

		btnSendMsg = new JButton("\u53D1\u9001\u6D88\u606F");
		btnSendMsg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msgContent = textFieldMsgToSend.getText();
				if (msgContent.length() > 0) {
					ChatMessage chatMessage = null;
					if (listOnlineUsers.getSelectedValue() != null) {
						chatMessage = new ChatMessage(localUserName, listOnlineUsers.getSelectedValue(), msgContent);
						String msgRecord = dateFormat.format(new Date()) + "向" + listOnlineUsers.getSelectedValue()
								+ "说:" + msgContent + "\r\n";
						addMsgRecord(msgRecord, Color.blue, 12, false, false);
					} else {
						chatMessage = new ChatMessage(localUserName, "", msgContent);
						String msgRecord = dateFormat.format(new Date()) + "向大家说:" + msgContent + "\r\n";
						addMsgRecord(msgRecord, Color.blue, 12, false, false);
					}
					try {
						synchronized (oos) {
							oos.writeObject(chatMessage);
							oos.flush();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		panelSouth.add(btnSendMsg);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panelSouth.add(horizontalStrut_3);

		btnSendFile = new JButton("\u53D1\u9001\u6587\u4EF6");
		btnSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("D:");
				fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
				int number = listOnlineUsers.getSelectedIndex();
				if (number == -1) {
					JOptionPane.showMessageDialog(null, "请选择一个用户发送文件");
				} else {
					if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						fileSelectPath = fileChooser.getSelectedFile().getPath();
						fileName = fileChooser.getSelectedFile().getName();
						fileLength = new File(fileSelectPath).length();
					}
					String drcUser = onlinUserDlm.getElementAt(number);
					FileMessage fileMessage = new FileMessage(localUserName, drcUser, fileName, fileLength);
					if (fileName != null) {
						try {
							synchronized (oos) {
								oos.writeObject(fileMessage);
								oos.flush();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						btnSendFile.setEnabled(false);
					}
				}

			}
		});
		panelSouth.add(btnSendFile);
		btnSendFile.setEnabled(false);
		btnSendMsg.setEnabled(false);
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

	class ListeningHandler implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					Message msg = null;
					synchronized (ois) {
						msg = (Message) ois.readObject();
					}
					if (msg instanceof UserStateMessage) {
						processUserStateMessage((UserStateMessage) msg);
					} else if (msg instanceof ChatMessage) {
						processChatMessage((ChatMessage) msg);
					} else if (msg instanceof LoginJudgment) {
					} else if (msg instanceof ForcedMessage) {
						processForceOfflineMessage((ForcedMessage) msg);
					} else if (msg instanceof FileMessage) {
						processFileMessage((FileMessage) msg);
					} else if (msg instanceof SendFileMessage) {
						processFileSend((SendFileMessage) msg);
					} else {
						System.err.println("用户发来的消息格式错误!");
					}
				}
			} catch (IOException e) {
				if (e.toString().endsWith("Connection reset")) {
					System.out.println("服务器端退出");
				} else {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void processUserStateMessage(UserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			if (msg.isUserOnline()) {
				if (msg.isPubUserStateMessage()) {
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "上线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					onlinUserDlm.addElement(srcUser);
				}
				if (dstUser.equals(localUserName)) {
					onlinUserDlm.addElement(srcUser);
				}
			} else if (msg.isUserOffline()) {
				if (onlinUserDlm.contains(srcUser)) {
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "下线了!\r\n";
					addMsgRecord(msgRecord, Color.green, 12, false, false);
					onlinUserDlm.removeElement(srcUser);
				}
			}
		}

		private void processChatMessage(ChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (onlinUserDlm.contains(srcUser)) {
				if (msg.isPubChatMessage() || dstUser.equals(localUserName)) {
					final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "说: " + msgContent
							+ "\r\n";
					addMsgRecord(msgRecord, Color.black, 12, false, false);
				}
			}
		}

	}

	private void processForceOfflineMessage(ForcedMessage msg) {
		if (msg.isState()) {
			JOptionPane.showMessageDialog(null, "你已被服务器强制下线!", "Log on", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}
	}

	private void processFileMessage(FileMessage msg) {
		String fileName = msg.getFilename();
		long fileLength = msg.getFilelength();
		String srcUser = msg.getSrcUser();
		String dstUser = msg.getDstUser();
		int filePort;
		final String msgRecord = dateFormat.format(new Date()) + " " + srcUser + "请求发送文件:" + fileName + "\r\n"
				+ "  文件大小:" + fileLength + "bit" + "\r\n";
		addMsgRecord(msgRecord, Color.black, 12, false, false);
		int m = JOptionPane.showConfirmDialog(null, "是否接收" + fileName + "文件", "提示", JOptionPane.YES_NO_OPTION);
		if (m == JOptionPane.YES_OPTION) {
			System.out.println("接收方同意接收文件");
			JFileChooser chooser = new JFileChooser("D:");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			try {
				System.out.println("开始接收");
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String selectPath = chooser.getSelectedFile().getPath();
					File file = new File(selectPath + "/" + fileName);
					file.createNewFile();
					fileSavePath = file.getPath();
					FileOutputStream fos = new FileOutputStream(file.getPath());
					serverSocket = createSSLServerSocket();
					filePort = serverSocket.getLocalPort();
					SendFileMessage fileDecisionMessage = new SendFileMessage(dstUser, srcUser, true, filePort);
					synchronized (oos) {
						oos.writeObject(fileDecisionMessage);
						oos.flush();
					}
					SSLSocket sslSocket = (SSLSocket) serverSocket.accept();
					RecieveFileHandler receiveHandler = new RecieveFileHandler(sslSocket, fos, fileLength);
					new Thread(receiveHandler).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("拒绝接收文件");
			SendFileMessage fileDecisionMessage = new SendFileMessage(dstUser, srcUser, false, 0);
			try {
				synchronized (oos) {
					oos.writeObject(fileDecisionMessage);
					oos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void processFileSend(SendFileMessage msg) {
		if (((SendFileMessage) msg).isAccept()) {
			try {
				btnSendFile.setEnabled(true);
				String DrcUser = msg.getSrcUser();
				FileInputStream fis = new FileInputStream(fileSelectPath);
				int port = ((SendFileMessage) msg).getPort();
				System.out.println("filePort:" + port);
				SendFile frame = new SendFile(DrcUser, fileSelectPath, port, fis, fileLength);
				frame.show();
				fileName = null;
				fileSelectPath = null;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "对方拒绝了文件传输");
			btnSendFile.setEnabled(true);
			fileName = null;
			fileSelectPath = null;
		}
	}

	class RecieveFileHandler implements Runnable {

		private InputStream inputStream;
		private long fileSize;
		private FileOutputStream fos;

		public RecieveFileHandler(SSLSocket sslSocket, FileOutputStream fos, long fileSize) {
			try {
				this.inputStream = sslSocket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.fos = fos;
			this.fileSize = fileSize;
		}

		public void run() {
			System.out.println("文件总长:" + fileSize);
			int sum = 0;
			int n = 0;
			byte[] buffer = new byte[1024];
			try {
				while ((n = inputStream.read(buffer)) != -1) {
					sum = sum + n;
					fos.write(buffer, 0, n);
					fos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					inputStream.close();
					JOptionPane.showMessageDialog(null, "文件传输结束");
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public SSLSocket createSSLSocket() throws Exception {
		String passphrase = "123456";
		char[] password = passphrase.toCharArray();
		String trustStoreFile = "mykeys.keystore";
		KeyStore ts = KeyStore.getInstance("PKCS12");
		ts.load(new FileInputStream(trustStoreFile), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts);
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, tmf.getTrustManagers(), null); // 客户端这里password可以去掉
		SSLSocketFactory factory = sslContext.getSocketFactory();
		socket = (SSLSocket) factory.createSocket("localhost", port);
		return (SSLSocket) socket;
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
		serverSocket = (SSLServerSocket) factory.createServerSocket(0);
		return (SSLServerSocket) serverSocket;
	}
}
