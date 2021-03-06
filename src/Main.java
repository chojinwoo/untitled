import common.Call;
import common.Decide;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {

    static Logger log = LoggerFactory.getLogger(Main.class);
    static String last_price = "0";
    static HashMap priceMap = new HashMap();
    static JSONArray useCurrency = null;

    static void init(List<String> currencies) {
        /*설정값 정의 최대 최소 퍼센트 정의*/

        priceMap.put("BTC_max_per", 1);
        priceMap.put("BTC_min_per", 1);
        priceMap.put("ETH_max_per", 2);
        priceMap.put("ETH_min_per", 2);
        priceMap.put("DASH_max_per", 2);
        priceMap.put("DASH_min_per", 2);
        priceMap.put("LTC_max_per", 3);
        priceMap.put("LTC_min_per", 3);
        priceMap.put("ETC_max_per", 5);
        priceMap.put("ETC_min_per", 5);
        priceMap.put("XRP_max_per", 5);
        priceMap.put("XRP_min_per", 5);
        priceMap.put("BCH_max_per", 1.5);
        priceMap.put("BCH_min_per", 1.5);
        priceMap.put("XMR_max_per", 3);
        priceMap.put("XMR_min_per", 3);
        priceMap.put("ZEC_max_per", 3);
        priceMap.put("ZEC_min_per", 3);
        priceMap.put("QTUM_max_per", 5);
        priceMap.put("QTUM_min_per", 3);
        priceMap.put("BTG_max_per", 3);
        priceMap.put("BTG_min_per", 3);

        /*패턴3 에서 사용*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String nowDate = sdf.format(new Date());
        priceMap.put("nowDate", nowDate);



        /*-------------*/
        useCurrency = new JSONArray();
        for(String currency : currencies) {
            useCurrency.put(currency);

            priceMap.put(currency+"sellCnt", "1");
            priceMap.put(currency+"dateCnt", "1");
            priceMap.put(currency+"threadCnt", "1");
        }
    }

    static Runnable btc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BTC");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable eth = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETH");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable dash = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("DASH");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable ltc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("LTC");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable etc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETC");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable xrp = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("XRP");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable xmr = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("XMR");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable zec = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ZEC");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable qtum = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("QTUM");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    static Runnable btg = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BTG");
        config.put("useCurrency", useCurrency);
        decide.pattern3(config, priceMap);
        log.debug("------------------------------------------------------------------------------------------");
    };

    public static void main(String args[]) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        try {

            /*
            *  통화 종류 선택
            *  BTC : 비트코인, ETH : 이더리움, BCH : 비트코인캐쉬, XRP : 리플 ETC : 이더리움 클래식
            *  */
            List<String> currencies = new LinkedList<String>();
            for(String arg : args) {
                currencies.add(arg);
            }

            /*기본 설정값 정의*/
            init(currencies);

            while(true) {


                List<Thread> thread = new ArrayList();
                for(String currency : currencies) {
                    switch(currency) {
                        case "BTC":
                            thread.add(new Thread(btc));
                            break;
                        case "ETH":
                            thread.add(new Thread(eth));
                            break;
                        case "DASH":
                            thread.add(new Thread(dash));
                            break;
                        case "LTC":
                            thread.add(new Thread(dash));
                            break;
                        case "ETC":
                            thread.add(new Thread(etc));
                            break;
                        case "XRP":
                            thread.add(new Thread(xrp));
                            break;
                        case "BCH":
                            thread.add(new Thread(bch));
                            break;
                        case "XMR":
                            thread.add(new Thread(xmr));
                            break;
                        case "ZEC":
                            thread.add(new Thread(xrp));
                            break;
                        case "QTUM":
                            thread.add(new Thread(bch));
                            break;
                        case "BTG":
                            thread.add(new Thread(xmr));
                            break;
                    }
                }

                for(int i=0; i<thread.size(); i++) {
                    thread.get(i).start();
                }

                Thread.sleep( 1200);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

