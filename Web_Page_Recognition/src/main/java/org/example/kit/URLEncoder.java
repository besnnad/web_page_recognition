package org.example.kit;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

public class URLEncoder {
	private static final boolean[] doNotNeedEncoding;
	private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	static{
		doNotNeedEncoding = new boolean[128];
		int i;
		for(i = 'a'; i <= 'z'; i++){
			doNotNeedEncoding[i] = true;
		}
		for(i = 'A'; i <= 'Z'; i++){
			doNotNeedEncoding[i] = true;
		}
		for(i = '0'; i <= '9'; i++){
			doNotNeedEncoding[i] = true;
		}
		doNotNeedEncoding[' '] = true; // encoding a space to a + is done in the encode() method
		doNotNeedEncoding['-'] = true;
		doNotNeedEncoding['_'] = true;
		doNotNeedEncoding['.'] = true;
		doNotNeedEncoding['*'] = true;
	}

	private Charset charset = null;
	private CharArrayWriter charArrayWriter = new CharArrayWriter();

	public URLEncoder(){
		try{
			charset = Charset.forName("UTF-8");
		}catch(Exception e){
		}
	}
	/**
	 * @param enc The name of a supported
	 *            <a href="../lang/package-summary.html#charenc">character
	 *            encoding</a>.
	 */
	public URLEncoder(String enc) throws UnsupportedEncodingException{
		if(enc == null)
			throw new NullPointerException("charsetName");
		try{
			charset = Charset.forName(enc);
		}catch(IllegalCharsetNameException | UnsupportedCharsetException e){
			throw new UnsupportedEncodingException(enc);
		}
	}

	/**
	 * Translates a string into {@code application/x-www-form-urlencoded}
	 * format using a specific encoding scheme. This method uses the
	 * supplied encoding scheme to obtain the bytes for unsafe
	 * characters.
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilities.</em>
	 *
	 * @param s {@code String} to be translated.
	 * @return the translated {@code String}.
	 * @throws UnsupportedEncodingException If the named encoding is not supported
	 * @see URLDecoder#decode(String, String)
	 * @since 1.6
	 */
	public String encoding(String s) throws UnsupportedEncodingException{
		return ecd(s, charArrayWriter, charset);
	}

	public static String encode(String str, String enc) throws UnsupportedEncodingException{
		Charset charset;
		if(enc == null)
			enc = "UTF-8";
		try{
			charset = Charset.forName(enc);
		}catch(IllegalCharsetNameException | UnsupportedCharsetException e){
			throw new UnsupportedEncodingException(enc);
		}
		CharArrayWriter charArrayWriter = new CharArrayWriter();
		return ecd(str, charArrayWriter, charset);
	}

	private static String ecd(String str, CharArrayWriter charArrayWriter, Charset charset){
		boolean needToChange = false;
		int length = str.length();
		StringBuilder out = new StringBuilder(length * 4 / 3);
		for(int i = 0; i < length; ){
			int c = (int)str.charAt(i);
			if(c < 128 && doNotNeedEncoding[c]){
				if(c == ' '){
					out.append('+');
					needToChange = true;
				}else
					out.append((char)c);
				i++;
			}else{
				do{
					charArrayWriter.write(c);
					if(c >= 0xD800 && c <= 0xDBFF){
						if((i + 1) < length){
							int d = (int)str.charAt(i + 1);
							if(d >= 0xDC00 && d <= 0xDFFF){
								charArrayWriter.write(d);
								i++;
							}
						}
					}
					i++;
				}while(i < length && !((c = (int)str.charAt(i)) < 128 && doNotNeedEncoding[c]));
				charArrayWriter.flush();
				byte[] ba = charArrayWriter.toString().getBytes(charset);
				for(int j = 0; j < ba.length; j++){
					out.append('%');
					out.append(HEX_CHARS[(ba[j] >> 4) & 0xF]);
					out.append(HEX_CHARS[ba[j] & 0xF]);
				}
				charArrayWriter.reset();
				needToChange = true;
			}
		}
		return (needToChange ? out.toString() : str);
	}

	public static String urlEncode(Map<String,Object> data, String enc) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder();
		int c = 0;
		URLEncoder ue = new URLEncoder(enc);
		for(Map.Entry<String,Object> i : data.entrySet()){
			if(c > 0)
				sb.append("&");
			try{
				sb.append(ue.encoding(i.getKey())).append("=").append(ue.encoding(StringKit.notNull(i.getValue())));
			}catch(UnsupportedEncodingException e){
			}
			c++;
		}
		return sb.toString();
	}
}

