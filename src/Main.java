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

        priceMap.put("ETH_max_per", 0.5);
        priceMap.put("ETH_min_per", 0.5);
        priceMap.put("BTC_max_per", 0.5);
        priceMap.put("BTC_min_per", 0.5);
        priceMap.put("BCH_max_per", 0.5);
        priceMap.put("BCH_min_per", 0.5);
        priceMap.put("XRP_max_per", 0.5);
        priceMap.put("XRP_min_per", 0.5);
        priceMap.put("ETC_max_per", 0.5);
        priceMap.put("ETC_min_per", 0.5);
        /*패턴3 에서 사용*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String nowDate = sdf.format(new Date());
        priceMap.put("nowDate", nowDate);
        /*-------------*/
        useCurrency = new JSONArray();
        for(String currency : currencies) {
            useCurrency.put(currency);
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

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
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

    static Runnable etc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETC");
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
                        case "BCH":
                            thread.add(new Thread(bch));
                            break;
                        case "XRP":
                            thread.add(new Thread(xrp));
                            break;
                        case "ETC":
                            thread.add(new Thread(etc));
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

