package sk.fiit.dp.refactor.command.sonarQube;

public class SonarProperties {
	private boolean isSonarEnabled; 
	private String hostName;
	private String loginName; 
	private String loginPassword;
	
	
	
	public boolean isSonarEnabled() {
		return isSonarEnabled;
	}
	public void setSonarEnabled(boolean isSonarEnabled) {
		this.isSonarEnabled = isSonarEnabled;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}	
}
