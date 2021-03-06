package common;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Util {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String HMAC_SHA512 = "HmacSHA512";

    public static String base64Encode(byte[] bytes) {
		String bytesEncoded = Base64.encode(bytes);
		return bytesEncoded;
    }

    public static String hashToString(String data, byte[] key) {
		String result = null;
		Mac sha512_HMAC;
	
		try {
		    sha512_HMAC = Mac.getInstance("HmacSHA512");
		    System.out.println("key : " + new String(key));
		    SecretKeySpec secretkey = new SecretKeySpec(key, "HmacSHA512");
		    sha512_HMAC.init(secretkey);
	
		    byte[] mac_data = sha512_HMAC.doFinal(data.getBytes());
		    System.out.println("hex : " + bin2hex(mac_data));
		    result = Base64.encode(mac_data);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return result;
    }

    public static byte[] hmacSha512(String value, String key) {
		try {
		    SecretKeySpec keySpec = new SecretKeySpec(
			    key.getBytes(DEFAULT_ENCODING), HMAC_SHA512);
		    Mac mac = Mac.getInstance(HMAC_SHA512);
		    mac.init(keySpec);
		    return mac.doFinal(value.getBytes(DEFAULT_ENCODING));
		} catch (NoSuchAlgorithmException e) {
		    throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
		    throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
		    throw new RuntimeException(e);
		}
    }

    public static String asHex(byte[] bytes) {
    	return new String(Base64.encode(bytes));
    }

    public static String bin2hex(byte[] data) {
    	return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static String mapToQueryString(Map<String, String> map) {
		StringBuilder string = new StringBuilder();
	
		if (map.size() > 0) {
		    string.append("?");
		}
	
		for (Entry<String, String> entry : map.entrySet()) {
		    string.append(entry.getKey());
		    string.append("=");
		    string.append(entry.getValue());
		    string.append("&");
		}
	
		return string.toString();
    }

    /*입력된 금액으로 구매 최대 수량 확인*/
	public static String krwToUnits(String currency, String myMoney, String price) {
		double units  = Integer.parseInt(myMoney) / Double.parseDouble(price);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(getDecimalCnt(currency));
		nf.setRoundingMode(RoundingMode.DOWN);
		nf.setGroupingUsed(true);
		return nf.format(units);
	}

	/*입력된 수량 최소 구매수량 소수점 까지 자르기 */
	public static String getDecimalRemoveUnits(String currency, String units) {
		double unit = Double.parseDouble(units);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(getDecimalCnt(currency));
		nf.setRoundingMode(RoundingMode.DOWN);
		nf.setGroupingUsed(true);
		return nf.format(unit);
	}

	/* 입력된값 소수점 자르기 */
	public static String decimalRemove(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		nf.setRoundingMode(RoundingMode.DOWN);
		nf.setGroupingUsed(true);
		return nf.format(value);
	}

	/* 입력된값 소수점 자르기 */
	public static String decimalRemove(double value, int digit) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(digit);
		nf.setRoundingMode(RoundingMode.DOWN);
		nf.setGroupingUsed(true);
		return nf.format(value);
	}

	/*BTC: 0.001 | ETH: 0.01 | DASH: 0.01 | LTC: 0.1 | ETC: 0.1 | XRP: 10 | BCH: 0.001 | XMR: 0.01 | ZEC: 0.001 | QTUM: 0.1 | BTG: 0.01)
- 1회 최대 수량 (BTC: 300 | ETH: 2,500 | DASH: 4,000 | LTC: 15,000 | ETC: 30,000 | XRP: 2,500,000 | BCH: 1,200 | XMR: 10,000 | ZEC: 2,500 | QTUM: 30,000 | BTG: 1,200*/

	/*통화별 최소 수량 */
	public static String getCurrencyMinQuantity(String currency) {
		JSONObject decimal = new JSONObject();
		decimal.put("BTC", "0.001");
		decimal.put("ETH", "0.01");
		decimal.put("DASH", "0.01");
		decimal.put("LTC", "0.1");
		decimal.put("ETC", "0.1");
		decimal.put("XRP", "10");
		decimal.put("BCH", "0.001");
		decimal.put("XMR", "0.01");
		decimal.put("ZEC", "0.001");
		decimal.put("QTUM", "0.1");
		decimal.put("BTG", "0.01");
		return (String)decimal.get(currency);

	}

	/*통화별 최대 소수점 */
	public static int getDecimalCnt(String currency) {
		JSONObject decimal = new JSONObject();
		decimal.put("BTC", 3);
		decimal.put("ETH", 2);
		decimal.put("DASH", 2);
		decimal.put("LTC", 1);
		decimal.put("ETC", 1);
		decimal.put("XRP", 0);
		decimal.put("BCH", 3);
		decimal.put("XMR", 2);
		decimal.put("ZEC", 3);
		decimal.put("QTUM", 1);
		decimal.put("BTG", 2);
		return (int)decimal.get(currency);
	}

	/*현재 시간에 몇분 뺀 시간 구하기 */
	public static String getRemoveMinuteTime(int minute) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddkkmmss");
		// Java 시간 더하기

		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		// 분 더하기
		cal.add(Calendar.MINUTE, minute);

		return sdf.format(cal.getTime());
	}

	/*최소 금액 구하기*/
	public static String getMinMoney(String price, double min_per) {
		try {
			int s = (int) (Integer.parseInt(price) * (0.01 * min_per));
			return String.valueOf(Integer.parseInt(price) - s);
		} catch(Exception e) {
			return "0";
		}
	}

	/*최대 금액 구하기*/
	public static String getMaxMoney(String price, double max_per) {
		try {
			int s = (int) (Integer.parseInt(price) * (0.01 * max_per));
			return String.valueOf(Integer.parseInt(price) + s);
		} catch(Exception e) {
			return "0";
		}
	}


}
