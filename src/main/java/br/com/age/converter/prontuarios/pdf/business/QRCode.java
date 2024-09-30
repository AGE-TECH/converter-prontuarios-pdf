package br.com.age.converter.prontuarios.pdf.business;

import br.com.age.converter.prontuarios.pdf.dao.ConverterProntuariosPdfDao;
import br.com.age.converter.prontuarios.pdf.enums.ClassificacaoTipoProntuario;
import br.com.age.converter.prontuarios.pdf.utils.Utils;
import br.com.age.converter.prontuarios.pdf.vo.QRCodeVo;

public class QRCode {

	private static final String CODIGO_DOCUMENTO_TODOS = "0";
	private static final String CODIGO_DOCUMENTO_ASO = String.valueOf(ClassificacaoTipoProntuario.ASO.getIndex());
	private ConverterProntuariosPdfDao dao;
	
	public QRCode(ConverterProntuariosPdfDao dao) {
		this.dao = dao;
	}

	public boolean isDocumentoConfiguradoParaVerificacaoPorLink(QRCodeVo qrcodeVo) {
		return qrcodeVo != null
				&& !Utils.isNullOrEmptyOrZero(qrcodeVo.getCodigoDocumento())
				&& isDocumentoPassivelVerificacao(qrcodeVo)
				&& isConfiguradoVerificacaoQrCode(qrcodeVo);
	}
	
	private boolean isDocumentoPassivelVerificacao(QRCodeVo qrcodeVo) {
		return CODIGO_DOCUMENTO_TODOS.equals(qrcodeVo.getCodigoDocumento()) || CODIGO_DOCUMENTO_ASO.equals(qrcodeVo.getCodigoDocumento());
	}
	
	private boolean isConfiguradoVerificacaoQrCode(QRCodeVo qrCodeParaBuscaVo) {
		QRCodeVo qrCodeVo = getQrCodePorCodigoParaImpressao(qrCodeParaBuscaVo);
		return qrCodeVo != null && qrCodeVo.isPermitirVerificacaoDadosQrcode();
	}
	
	public QRCodeVo getQrCodePorCodigoParaImpressao(QRCodeVo qrcodeVo) {
		QRCodeVo qrcodeAuxVo = dao.getQrCodePorCodigoDocumento(qrcodeVo);
		
		if(qrcodeAuxVo == null){
			qrcodeAuxVo = new QRCodeVo();
			qrcodeAuxVo.setCodigoEmpresa(qrcodeVo.getCodigoEmpresa());				
			qrcodeAuxVo.setCodigoDocumento("0");
			qrcodeAuxVo = dao.getQrCodePorCodigoDocumento(qrcodeAuxVo);
		}

		if(qrcodeAuxVo == null){
			return null;
		}
		
		qrcodeVo.setPermitirVerificacaoDadosQrcode(qrcodeAuxVo.isPermitirVerificacaoDadosQrcode());
		
		return qrcodeVo;
	}
}
