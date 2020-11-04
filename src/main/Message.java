package main;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1705703883214597078L;
	private String srcUser;
	private String dstUser;

	public Message(String srcUser, String dstUser) {
		this.srcUser = srcUser;
		this.dstUser = dstUser;
	}

	public String getSrcUser() {
		return srcUser;
	}

	public void setSrcUser(String srcUser) {
		this.srcUser = srcUser;
	}

	public String getDstUser() {
		return dstUser;
	}

	public void setDstUser(String dstUser) {
		this.dstUser = dstUser;
	}
}

class ChatMessage extends Message {
	private static final long serialVersionUID = -2524099131367593960L;
	private String msgContent;

	public ChatMessage(String srcUser, String dstUser, String msgContent) {
		super(srcUser, dstUser);
		this.msgContent = msgContent;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public boolean isPubChatMessage() {
		return getDstUser().equals("");
	}
}

class UserStateMessage extends Message {
	private static final long serialVersionUID = 5321645244646135843L;
	private boolean userOnline;

	public UserStateMessage(String srcUser, String dstUser, boolean userOnline) {
		super(srcUser, dstUser);
		this.userOnline = userOnline;
	}

	public boolean isUserOnline() {
		return userOnline;
	}

	public boolean isUserOffline() {
		return !userOnline;
	}

	public void setUserOnline(boolean userOnline) {
		this.userOnline = userOnline;
	}

	public boolean isPubUserStateMessage() {
		return getDstUser().equals("");
	}
}

class LoginMessage extends Message {
	private static final long serialVersionUID = 8918840133818686357L;
	private String passwdString;

	public LoginMessage(String srcUser, String dstUser, String passwdString) {
		super(srcUser, dstUser);
		this.passwdString = passwdString;
	}

	public String getPasswd() {
		return passwdString;
	}

}

class LoginJudgment extends Message {
	private static final long serialVersionUID = 2859330911802335172L;
	private Boolean judgBoolean;

	public LoginJudgment(String srcUser, String dstUser, Boolean judgBoolean) {
		super(srcUser, dstUser);
		this.judgBoolean = judgBoolean;
	}

	public Boolean getJudgment() {
		return judgBoolean;
	}
}

class RegisterMessage extends Message {
	private static final long serialVersionUID = 7560062562907418171L;
	private String passwdString;
	private String userPhoneString;

	public RegisterMessage(String srcUser, String dstUser, String passwdString, String userPhoneString) {
		super(srcUser, dstUser);
		this.passwdString = passwdString;
		this.userPhoneString = userPhoneString;
	}

	public String getPasswd() {
		return passwdString;
	}

	public String getPhone() {
		return userPhoneString;
	}

}

class RegisterJudgment extends Message {
	private static final long serialVersionUID = 8262473595140319866L;
	private Boolean judgBoolean;

	public RegisterJudgment(String srcUser, String dstUser, Boolean judgBoolean) {
		super(srcUser, dstUser);
		this.judgBoolean = judgBoolean;
	}

	public Boolean getJudgment() {
		return judgBoolean;
	}
}

class ForcedMessage extends Message {
	private static final long serialVersionUID = -6885865025897177832L;
	private boolean state = false;

	public ForcedMessage(String srcUser, String dstUser, boolean state) {
		super(srcUser, dstUser);
		this.state = state;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}

class FileMessage extends Message {
	private String filename;
	private long filelength;

	public FileMessage(String srcUser, String dstUser, String filename) {
		super(srcUser, dstUser);
		this.filename = filename;
	}

	public FileMessage(String srcUser, String dstUser, String filename, long filelength) {
		super(srcUser, dstUser);
		this.filename = filename;
		this.filelength = filelength;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getFilelength() {
		return filelength;
	}

	public void setFilelength(long filelength) {
		this.filelength = filelength;
	}

}

class SendFileMessage extends Message {
	private boolean accept;
	private int port;

	public SendFileMessage(String srcUser, String dstUser) {
		super(srcUser, dstUser);
	}

	public SendFileMessage(String srcUser, String dstUser, boolean accept, int port) {
		super(srcUser, dstUser);
		this.accept = accept;
		this.port = port;
	}

	public boolean isAccept() {
		return accept;
	}

	public void setAccept(boolean accept) {
		this.accept = accept;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
