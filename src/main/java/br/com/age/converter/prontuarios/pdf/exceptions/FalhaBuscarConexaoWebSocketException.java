package br.com.age.converter.prontuarios.pdf.exceptions;

public class FalhaBuscarConexaoWebSocketException extends Exception {

	private static final long serialVersionUID = 1L;

	public FalhaBuscarConexaoWebSocketException(Throwable e) {
		super(e);
	}

	public FalhaBuscarConexaoWebSocketException(String str) {
		super(str);
	}

	public FalhaBuscarConexaoWebSocketException(String str, Throwable e) {
		super(str, e);
	}
	
}
