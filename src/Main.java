import common.Call;
import common.Decide;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {
    static String last_price = "0";
    static HashMap priceMap = new HashMap();
    static JSONArray useCurrency = null;

    static void init(String[] currencies) {
        /*설정값 정의 최대 최소 퍼센트 정의*/

        priceMap.put("ETH_max_per", 1.0);
        priceMap.put("ETH_min_per", 1.0);
        priceMap.put("BTC_max_per", 0.5);
        priceMap.put("BTC_min_per", 0.5);
        priceMap.put("BCH_max_per", 1.0);
        priceMap.put("BCH_min_per", 1.0);
        priceMap.put("XRP_max_per", 1.0);
        priceMap.put("XRP_min_per", 1.0);
        priceMap.put("ETC_max_per", 1.0);
        priceMap.put("ETC_min_per", 1.0);
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
        decide.pattern2(config, priceMap);
    };

    static Runnable eth = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETH");
        config.put("useCurrency", useCurrency);
        decide.pattern2(config, priceMap);
    };

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
        config.put("useCurrency", useCurrency);
        decide.pattern2(config, priceMap);
    };

    static Runnable xrp = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("XRP");
        config.put("useCurrency", useCurrency);
        decide.pattern2(config, priceMap);
    };

    static Runnable etc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETC");
        config.put("useCurrency", useCurrency);
        decide.pattern2(config, priceMap);
    };

    public static void main(String args[]) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        try {
//            File file = new File("logs/log_"+sdf.format(new Date())+".txt");
//            PrintStream printStream = new PrintStream(new FileOutputStream(file));
//            PrintStream sysout = System.out;
//            System.setOut(printStream);

            /*
            *  통화 종류 선택
            *  BTC : 비트코인, ETH : 이더리움, BCH : 비트코인캐쉬, XRP : 리플 ETC : 이더리움 클래식
            *  */
            String[] currencies = new String[]{"BTC"};

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
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

