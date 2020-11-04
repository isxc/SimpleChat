package main;


import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.HeadlessException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.SystemColor;

public class Register extends JFrame {

	private final int port = 9999;
	private SSLSocket socket;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	private String localUserName;
	private String password;

	private JPanel contentPane;
	private JTextField textFieldUserName;
	private JPasswordField passwordFieldPwd;
	private JButton btnRegister;
	private JTextField textFieldPhone;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Register frame = new Register();
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
	public Register() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JLabel lblNewLabel = new JLabel("\u6CE8\u518C");
		lblNewLabel.setForeground(SystemColor.textHighlight);
		lblNewLabel.setFont(new Font("隶书", Font.PLAIN, 24));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);

		JPanel panelCenter = new JPanel();
		contentPane.add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("\u7528\u6237\u540D\uFF1A");
		lblNewLabel_1.setBounds(25, 47, 58, 15);
		panelCenter.add(lblNewLabel_1);

		textFieldUserName = new JTextField();
		textFieldUserName.setBounds(84, 44, 246, 21);
		panelCenter.add(textFieldUserName);
		textFieldUserName.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("\u5BC6\u7801\uFF1A");
		lblNewLabel_2.setBounds(25, 98, 58, 15);
		panelCenter.add(lblNewLabel_2);

		passwordFieldPwd = new JPasswordField();
		passwordFieldPwd.setBounds(84, 92, 246, 21);
		panelCenter.add(passwordFieldPwd);

		JLabel lblNewLabel_3 = new JLabel("\u53F7\u7801\uFF1A");
		lblNewLabel_3.setBounds(25, 142, 58, 15);
		panelCenter.add(lblNewLabel_3);

		textFieldPhone = new JTextField();
		textFieldPhone.setBounds(84, 139, 246, 21);
		panelCenter.add(textFieldPhone);
		textFieldPhone.setColumns(10);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);

		btnRegister = new JButton("\u7ACB\u5373\u6CE8\u518C");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String userName = textFieldUserName.getText().trim();
				String passwd = passwordFieldPwd.getText().trim();
				String phone = textFieldPhone.getText().trim();
				if (userName.length() > 0) {
					if (passwd.length() > 0) {
						if (phone.length() > 0) {
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
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							RegisterMessage registerMessage = new RegisterMessage(userName, "", passwd, phone);
							try {
								synchronized (oos) {
									oos.writeObject(registerMessage);
									oos.flush();
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								RegisterJudgment registerJudgment = (RegisterJudgment) ois.readObject();
								if (registerJudgment.getJudgment() == true) {
									JOptionPane.showMessageDialog(null, "注册成功！");
								} else {
									JOptionPane.showMessageDialog(null, "注册失败！该用户已存在！");
								}
							} catch (HeadlessException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

						} else {
							JOptionPane.showMessageDialog(null, "请输入号码！");
						}
					} else {
						JOptionPane.showMessageDialog(null, "请输入密码！");
					}
				}

			}
		});
		panelSouth.add(btnRegister);
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
		sslContext.init(null, tmf.getTrustManagers(), null); 
		SSLSocketFactory factory = sslContext.getSocketFactory();
		socket = (SSLSocket) factory.createSocket("localhost", port);
		return (SSLSocket) socket;
	}
}
