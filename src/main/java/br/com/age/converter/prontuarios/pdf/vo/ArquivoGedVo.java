package br.com.age.converter.prontuarios.pdf.vo;

public class ArquivoGedVo {

	private byte[] arquivo;
	
	private String rowid;
	private String codigoGed;
	private String codigoArquivoGed;
	private String codigoEmpresa;
	private String codigoEmpresaPrincipal;
	private String nomeDoArquivo;
	private String nomeDoDiretorio;
	private String dataUpload;
	private String horaUpload;
	private String codigoTipoDocumento;
	private String vaultId;
	private String codigoSequencialFicha;
	private String codigoTipoGed;
	private String codigoFuncionario;
	private String codigoUsuarioAssinatura;
	private String orderBy;
	private String dataFinal;
	private String codigoEmpresaNavegacao;
	private String comentarioAdicionalLog;
	
	private int quantidadeAssinaturasPermitidas;
	private int quantidadeAssinaturas;
	private int codigoResponsavelAcao;
	
	private boolean envioAutomatico;
	private boolean assinadoDigitalmente;
	private boolean buscaGedPorDataUpload;
	private boolean acessoSocNet;
	private boolean possuiQuantidadeAsssinaturaAutomaticaSesi;
	private boolean downloadPortal;
	
	public String getRowid() {
		return rowid;
	}

	public void setRowid(String rowid) {
		this.rowid = rowid;
	}

	public String getCodigoGed() {
		return codigoGed;
	}

	public void setCodigoGed(String codigoGed) {
		this.codigoGed = codigoGed;
	}

	public String getCodigoArquivoGed() {
		return codigoArquivoGed;
	}

	public void setCodigoArquivoGed(String codigoArquivoGed) {
		this.codigoArquivoGed = codigoArquivoGed;
	}

	public String getCodigoEmpresa() {
		return codigoEmpresa;
	}

	public void setCodigoEmpresa(String codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}

	public String getCodigoEmpresaPrincipal() {
		return codigoEmpresaPrincipal;
	}

	public void setCodigoEmpresaPrincipal(String codigoEmpresaPrincipal) {
		this.codigoEmpresaPrincipal = codigoEmpresaPrincipal;
	}

	public String getNomeDoArquivo() {
		return nomeDoArquivo;
	}

	public void setNomeDoArquivo(String nomeDoArquivo) {
		this.nomeDoArquivo = nomeDoArquivo;
	}

	public String getNomeDoDiretorio() {
		return nomeDoDiretorio;
	}

	public void setNomeDoDiretorio(String nomeDoDiretorio) {
		this.nomeDoDiretorio = nomeDoDiretorio;
	}

	public String getDataUpload() {
		return dataUpload;
	}

	public void setDataUpload(String dataUpload) {
		this.dataUpload = dataUpload;
	}

	public String getHoraUpload() {
		return horaUpload;
	}

	public void setHoraUpload(String horaUpload) {
		this.horaUpload = horaUpload;
	}

	public String getCodigoTipoDocumento() {
		return codigoTipoDocumento;
	}

	public void setCodigoTipoDocumento(String codigoTipoDocumento) {
		this.codigoTipoDocumento = codigoTipoDocumento;
	}

	public String getVaultId() {
		return vaultId;
	}

	public void setVaultId(String vaultId) {
		this.vaultId = vaultId;
	}

	public int getQuantidadeAssinaturasPermitidas() {
		return quantidadeAssinaturasPermitidas;
	}

	public void setQuantidadeAssinaturasPermitidas(int quantidadeAssinaturasPermitidas) {
		this.quantidadeAssinaturasPermitidas = quantidadeAssinaturasPermitidas;
	}

	public int getQuantidadeAssinaturas() {
		return quantidadeAssinaturas;
	}

	public void setQuantidadeAssinaturas(int quantidadeAssinaturas) {
		this.quantidadeAssinaturas = quantidadeAssinaturas;
	}

	public boolean isAssinadoDigitalmente() {
		return assinadoDigitalmente;
	}

	public void setAssinadoDigitalmente(boolean assinadoDigitalmente) {
		this.assinadoDigitalmente = assinadoDigitalmente;
	}

	public int getCodigoResponsavelAcao() {
		return codigoResponsavelAcao;
	}

	public void setCodigoResponsavelAcao(int codigoResponsavelAcao) {
		this.codigoResponsavelAcao = codigoResponsavelAcao;
	}

	public String getCodigoSequencialFicha() {
		return codigoSequencialFicha;
	}

	public void setCodigoSequencialFicha(String codigoSequencialFicha) {
		this.codigoSequencialFicha = codigoSequencialFicha;
	}

	public String getCodigoTipoGed() {
		return codigoTipoGed;
	}

	public void setCodigoTipoGed(String codigoTipoGed) {
		this.codigoTipoGed = codigoTipoGed;
	}

	public String getCodigoFuncionario() {
		return codigoFuncionario;
	}

	public void setCodigoFuncionario(String codigoFuncionario) {
		this.codigoFuncionario = codigoFuncionario;
	}

	public byte[] getArquivo() {
		return arquivo;
	}

	public void setArquivo(byte[] arquivo) {
		this.arquivo = arquivo;
	}

	public boolean isEnvioAutomatico() {
		return envioAutomatico;
	}

	public void setEnvioAutomatico(boolean envioAutomatico) {
		this.envioAutomatico = envioAutomatico;
	}

	public String getCodigoUsuarioAssinatura() {
		return codigoUsuarioAssinatura;
	}

	public void setCodigoUsuarioAssinatura(String codigoUsuarioAssinatura) {
		this.codigoUsuarioAssinatura = codigoUsuarioAssinatura;
	}

	public boolean isBuscaGedPorDataUpload() {
		return buscaGedPorDataUpload;
	}

	public void setBuscaGedPorDataUpload(boolean buscaGedPorDataUpload) {
		this.buscaGedPorDataUpload = buscaGedPorDataUpload;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(String dataFinal) {
		this.dataFinal = dataFinal;
	}

	public boolean isAcessoSocNet() {
		return acessoSocNet;
	}

	public void setAcessoSocNet(boolean acessoSocNet) {
		this.acessoSocNet = acessoSocNet;
	}

	public boolean isPossuiQuantidadeAsssinaturaAutomaticaSesi() {
		return possuiQuantidadeAsssinaturaAutomaticaSesi;
	}

	public void setPossuiQuantidadeAsssinaturaAutomaticaSesi(boolean possuiQuantidadeAsssinaturaAutomaticaSesi) {
		this.possuiQuantidadeAsssinaturaAutomaticaSesi = possuiQuantidadeAsssinaturaAutomaticaSesi;
	}

	public boolean isDownloadPortal() {
		return downloadPortal;
	}

	public void setDownloadPortal(boolean downloadPortal) {
		this.downloadPortal = downloadPortal;
	}

	public String getCodigoEmpresaNavegacao() {
		return codigoEmpresaNavegacao;
	}

	public void setCodigoEmpresaNavegacao(String codigoEmpresaNavegacao) {
		this.codigoEmpresaNavegacao = codigoEmpresaNavegacao;
	}

	public String getComentarioAdicionalLog() {
		return comentarioAdicionalLog;
	}

	public void setComentarioAdicionalLog(String comentarioAdicionalLog) {
		this.comentarioAdicionalLog = comentarioAdicionalLog;
	}
	
}
