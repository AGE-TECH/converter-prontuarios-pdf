package br.com.age.converter.prontuarios.pdf.vo;

public class QRCodeVo {

	private String codigoEmpresa;
	private String codigoDocumento;
	
	private boolean permitirVerificacaoDadosQrcode;
	
	public String getCodigoEmpresa() {
		return codigoEmpresa;
	}
	
	public void setCodigoEmpresa(String codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}
	
	public String getCodigoDocumento() {
		return codigoDocumento;
	}
	
	public void setCodigoDocumento(String codigoDocumento) {
		this.codigoDocumento = codigoDocumento;
	}

	public boolean isPermitirVerificacaoDadosQrcode() {
		return permitirVerificacaoDadosQrcode;
	}

	public void setPermitirVerificacaoDadosQrcode(boolean permitirVerificacaoDadosQrcode) {
		this.permitirVerificacaoDadosQrcode = permitirVerificacaoDadosQrcode;
	}
	
}
