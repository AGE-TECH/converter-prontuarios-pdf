package br.com.age.converter.prontuarios.pdf.vo;

public class InfraConfiguracaoVo {

	private String rowid;
	private String codigo;
	private String nomeLabel;
	private String valorLabel;
	private String tipoConfiguracao;
	private String valorLinux;
	private String valorRetorno;

	private boolean situacao;
	
	public String getRowid() {
		return rowid;
	}
	
	public void setRowid(String rowid) {
		this.rowid = rowid;
	}
	
	public String getCodigo() {
		return codigo;
	}
	
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public String getNomeLabel() {
		return nomeLabel;
	}
	
	public void setNomeLabel(String nomeLabel) {
		this.nomeLabel = nomeLabel;
	}
	
	public String getValorLabel() {
		return valorLabel;
	}
	
	public void setValorLabel(String valorLabel) {
		this.valorLabel = valorLabel;
	}
	
	public String getTipoConfiguracao() {
		return tipoConfiguracao;
	}
	
	public void setTipoConfiguracao(String tipoConfiguracao) {
		this.tipoConfiguracao = tipoConfiguracao;
	}
	
	public boolean isSituacao() {
		return situacao;
	}
	
	public void setSituacao(boolean situacao) {
		this.situacao = situacao;
	}
	
	public String getValorLinux() {
		return valorLinux;
	}
	
	public void setValorLinux(String valorLinux) {
		this.valorLinux = valorLinux;
	}

	public String getValorRetorno() {
		return valorRetorno;
	}

	public void setValorRetorno(String valorRetorno) {
		this.valorRetorno = valorRetorno;
	}
	
}
