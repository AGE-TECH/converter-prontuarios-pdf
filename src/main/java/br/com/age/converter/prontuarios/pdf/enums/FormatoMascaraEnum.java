package br.com.age.converter.prontuarios.pdf.enums;

public enum FormatoMascaraEnum {
	CEI("(\\d{2})(\\d{3})(\\d{5})(\\d{2})", "$1.$2.$3/$4", 12);
	
	private String formato;
	private String mascara;
	private int tamanho;
	
	FormatoMascaraEnum(String formato, String mascara, int tamanho){
		this.formato = formato;
		this.mascara = mascara;
		this.tamanho = tamanho;
	}

	public String getFormato() {
		return formato;
	}

	public int getTamanho() {
		return tamanho;
	}

	public String getMascara() {
		return mascara;
	}
}
