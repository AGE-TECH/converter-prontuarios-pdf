package br.com.age.converter.prontuarios.pdf.vo;

public class LogVo {

	private int codigoEmpresa;
	private int codigoUsuario;
	private int codigoResponsavelUsuario;

	private String acao;
	private String nomePrograma;
	private String obs;
	private String programa;
	private String codigoLog;
	
	private boolean socnet;
	private boolean exibeLogNavegacao;
	
	public String getAcao() {
		return acao;
	}
	
	public void setAcao(String acao) {
		this.acao = acao;
	}
	
	public String getObs() {
		return obs;
	}
	
	public void setObs(String obs) {
		this.obs = obs;
	}
	
	public String getPrograma() {
		return programa;
	}
	
	public void setPrograma(String programa) {
		this.programa = programa;
	}
	
	public String getCodigoLog() {
		return codigoLog;
	}
	
	public void setCodigoLog(String codigoLog) {
		this.codigoLog = codigoLog;
	}

	public String getNomePrograma() {
		return nomePrograma;
	}

	public void setNomePrograma(String nomePrograma) {
		this.nomePrograma = nomePrograma;
	}

	public int getCodigoUsuario() {
		return codigoUsuario;
	}

	public void setCodigoUsuario(int codigoUsuario) {
		this.codigoUsuario = codigoUsuario;
	}

	public int getCodigoEmpresa() {
		return codigoEmpresa;
	}

	public void setCodigoEmpresa(int codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}

	public boolean isSocnet() {
		return socnet;
	}

	public void setSocnet(boolean socnet) {
		this.socnet = socnet;
	}

	public boolean isExibeLogNavegacao() {
		return exibeLogNavegacao;
	}

	public void setExibeLogNavegacao(boolean exibeLogNavegacao) {
		this.exibeLogNavegacao = exibeLogNavegacao;
	}

	public int getCodigoResponsavelUsuario() {
		return codigoResponsavelUsuario;
	}

	public void setCodigoResponsavelUsuario(int codigoResponsavelUsuario) {
		this.codigoResponsavelUsuario = codigoResponsavelUsuario;
	}


}
