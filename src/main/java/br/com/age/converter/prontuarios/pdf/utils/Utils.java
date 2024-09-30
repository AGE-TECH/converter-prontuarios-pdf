package br.com.age.converter.prontuarios.pdf.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import br.com.age.converter.prontuarios.pdf.enums.FormatoMascaraEnum;
import br.com.age.converter.prontuarios.pdf.vo.SoParamVo;

public class Utils {
	
	protected Utils() {
	}

	private static final String DD_BARRA_MM_BARRA_YYYY = "dd/MM/yyyy";
	private static final String VAZIO = "";
	private static final String FORMATO_DATA_DD_MM_YYYY = "dd/MM/yyyy";
	
	public static boolean isNullOrEmpty(String string){
		return string == null || string.trim().isEmpty();
	}
	
	public static boolean isNullOrEmptyOrZero(String string){
		return isNullOrEmpty(string) || string.equals("0");
	 }
	
	public static String retiraConteudoStringEntreDuasPalavras(String texto, String palavra1, String palavra2){
		int inicio = 0;
		int fim    = 0;
		String textoRetirar;
		String retornoTexto = texto;
		
		while (retornoTexto.contains(palavra1) && retornoTexto.contains(palavra2)){
			inicio = retornoTexto.indexOf(palavra1);
			fim = retornoTexto.indexOf(palavra2) + palavra2.length();
			textoRetirar = retornoTexto.substring(inicio, fim);
			retornoTexto = retornoTexto.replace(textoRetirar, VAZIO);
		}
		
		return retornoTexto;
	}
	
	public static boolean find(String campoBusca, String regex){
		return find(campoBusca, regex, true);
	}
	
	public static boolean find(String campoBusca, String regex, boolean caseSensitive){
		final Pattern pattern = (caseSensitive ? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
		final Matcher matcher = pattern.matcher(campoBusca);
		return matcher.find(0);
	}
	
	public static int toInt(String stringComNumero){
		try {
			if (!isNullOrEmpty(stringComNumero)) {
				return Integer.parseInt(stringComNumero.trim());
			}
		} catch (final Exception e) {
			return 0;
		}
		return 0;
	}
	
	public static String formataHora(String segundos){
		try {
			if (!isNullOrEmpty(segundos)) {
				if (segundos.contains(":")) {
					return segundos;
				}
				int numero = Integer.parseInt(segundos);
				final int horas = (numero / 3600);
				numero = (numero - (horas * 3600));
				int minutos = (numero / 60);
				minutos = ((minutos < 0) ? minutos * (-1) : minutos);
				
				return formataZeros(horas, 2) + ":" + formataZeros(minutos, 2);
			}
			return "00:00";
		} catch (final Exception e) {
			return VAZIO;
		}
	}
	
	private static String formataZeros(int num, int casas) {
		String retorno = String.valueOf(num);
		while (retorno.length() < casas) {
			retorno = "0" + retorno;
		}
		return retorno;
	}
	
	private static String formataZeros(String num, int casas) {
		String retorno = num == null ? "" : num;
		while (retorno.length() < casas) {
			retorno = "0" + retorno;
		}	 	  
		return retorno;
	}
	
	public static String dataFormatada(java.util.Date data){
		return dataFormatada(data, FORMATO_DATA_DD_MM_YYYY);
	}
	
	private static String dataFormatada(java.util.Date date,String formato){
		try {
			if (date != null && !isNullOrEmpty(formato)) {
				return DateTimeFormatter.ofPattern(formato).format(convertLocalDateViaMilisecond(date));
			}
			return "";
		} catch (final DateTimeException e) {
			return "";
		} catch (final Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static LocalDate convertLocalDateViaMilisecond(final Date data) {
		return Instant.ofEpochMilli(data.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static String insereMascaraCEI(String texto){
		return formataGeral(texto, FormatoMascaraEnum.CEI);
	}
	
	public static String formataGeral(final String campo, final FormatoMascaraEnum formatoMascara){
		if (!isNullOrEmpty(campo)) {
			final Pattern pattern = Pattern.compile(formatoMascara.getFormato());
			final Matcher matcher = pattern.matcher(formataZeros(retiraMascaraPontoTracoHifen(campo).trim(), formatoMascara.getTamanho()));
			if (matcher.matches()) {
				return matcher.replaceAll(formatoMascara.getMascara());
			}
		}
		return VAZIO;
	}
	
	public static String retiraMascaraPontoTracoHifen(String campo){
		if (isNullOrEmpty(campo)) {
			return VAZIO;
		}
		return campo.replaceAll("[./-]", VAZIO);
	}
	
	public static String verificaUrlImagem(String html, SoParamVo soParamVo){
		String urlImagem = null;
		String htmlReplace = html;
		
		Pattern pattern = Pattern.compile("<img(.*)src(.*)>");
		Matcher matcher = pattern.matcher(htmlReplace);
		
		if(matcher.find()){
			urlImagem = soParamVo.getJavamenu();
			urlImagem = urlImagem.substring(0,(urlImagem.indexOf("/",(urlImagem.indexOf("//")+2))) +1 );
			htmlReplace = htmlReplace.replaceAll("src=\"../"," src=\""+urlImagem+"");
			htmlReplace = htmlReplace.replace("https://","http://");
		}
		
		return htmlReplace;
	}
	
	public static boolean toBoolean(String stringComZeroUm){
		if (!isNullOrEmpty(stringComZeroUm) && (stringComZeroUm.equals("1") || stringComZeroUm.equalsIgnoreCase("SIM")
				|| stringComZeroUm.equalsIgnoreCase("S") || stringComZeroUm.equalsIgnoreCase("true") || stringComZeroUm.equalsIgnoreCase("on")
				|| stringComZeroUm.equalsIgnoreCase("yes"))) {
			return true;
		}
		return false;
	}
	
	public static List<String> getListaArquivosDiretorio(String diretorioImp) {				
		final List<String> nomeArquivo = new ArrayList<>();
		final File diretorio = new File(diretorioImp);
		final File[] dirArq = diretorio.listFiles();
		
		if (dirArq != null) {
			for (int u = 0; u < dirArq.length; u++) {
				nomeArquivo.add(dirArq[u].getName().toString());
			}
		}
		return nomeArquivo;
	}
	
	public static boolean isValoresIguais(String valorOriginal, String ... valoresComparacao) {
		for (final String valorComparacao  : valoresComparacao) {
			if(valorOriginal == null) {
				return valorComparacao == null;
			} else if (!valorOriginal.equals(valorComparacao)) {
				return false;
			}
		}
		return true;
	}
	
	public static String getDataDoDia() {
		return DateTimeFormatter.ofPattern("dd/MM/uuuu").format(LocalDate.now());
	}
	
	public static String replaceAllAccent(String replacement) {
		if (replacement == null) {
			return null;
		}
		
		return replacement
			.replaceAll("[ãáàâäª]", "a").replaceAll("[ãáàâä]".toUpperCase(), "A")
			.replaceAll("[éèêë]", "e").replaceAll("[éèêë]".toUpperCase(), "E")
			.replaceAll("[íìîï]", "i").replaceAll("[íìîï]".toUpperCase(), "I")
			.replaceAll("[õóòôöº]", "o").replaceAll("[õóòôö]".toUpperCase(), "O")
			.replaceAll("[úùûü]", "u").replaceAll("[úùûü]".toUpperCase(), "U")
			.replace("ç", "c").replace("Ç", "C")
			.replace("ñ", "n").replace("Ñ", "N")
			.replace("&", "");
	}
	
	public static Date toDate(String date){
		try {
			if (!isNullOrEmpty(date)) {
				final DateFormat dataEntrada = new SimpleDateFormat(DD_BARRA_MM_BARRA_YYYY);
				dataEntrada.setLenient(false);
				return dataEntrada.parse(date);
			}
			return null;
		} catch (final Exception e) {
			return null;
		}
	}
	
	public static long toLong(String stringComNumero){
		try{
			return Long.parseLong(stringComNumero);
		} catch(Exception e){
			return 0;
		}	
	}
	
	public static java.sql.Date getSqlDateFromUtilDate2(java.util.Date data){
		java.sql.Date novaData = null;
		if (data != null){
			novaData = new java.sql.Date(data.getTime());
		}
		return novaData;
	}
	
	public static String normalizarTexto(String texto, String dataDoDia) {
		if(isNullOrEmpty(texto)) return texto;
		
		String normalizado = replaceAllAccent(texto)
				.replaceAll("[^\\w\\d\\s\\-,._\\[\\]\\(\\)]", "")
				.replaceAll("\\s+", " ")
				.trim();
		String baseName = FilenameUtils.getBaseName(normalizado);
		String extension = FilenameUtils.getExtension(normalizado);
		
		if (baseName != null) {
			baseName = baseName.trim();
		}
		
		if (extension != null) {
			extension = extension.trim();
		}
		
		if(isNullOrEmpty(baseName)) {
			baseName = MessageFormat.format("Consulta_{0}", String.valueOf(toDate(dataDoDia).getTime()));
		}
		
		if(isNullOrEmpty(extension)) {
			return baseName;
		}
		
		return MessageFormat.format("{0}.{1}", baseName, extension);
	}
	
	public static boolean isNullOrEmptyList(Collection<?> collection){
		return ((collection == null) || (collection.isEmpty()));
	}
	
}
