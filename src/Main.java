import common.Call;
import common.Decide;
import common.Util;
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
    static int max_per = 1;
    static int min_per = 1;
    static HashMap priceMap = new HashMap();


    static Runnable btc = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BTC");
        HashMap map = mapSetting(config);
        int result = decide.pattern1(map, priceMap);
        if(result == 1) {/* 1 : 구매*/
            call.buy((String) map.get("currency"), (Integer) map.get("ask1"), Util.krwToUnits((Integer) map.get("avakrw"), (Integer) map.get("ask1")));
        } else if(result == 2) {/* 2 : 판매*/
            call.sell((String) map.get("currency"), (Integer) map.get("bid1"), (String) map.get("avacoin"));
        }
    };

    static Runnable eth = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("ETH");
        HashMap map = mapSetting(config);
        int result = decide.pattern1(map, priceMap);
        if(result == 1) {/* 1 : 구매*/
            call.buy((String) map.get("currency"), (Integer) map.get("ask1"), Util.krwToUnits((Integer) map.get("avakrw"), (Integer) map.get("ask1")));
        } else if(result == 2) {/* 2 : 판매*/
            call.sell((String) map.get("currency"), (Integer) map.get("bid1"), (String) map.get("avacoin"));
        }
    };

    static Runnable bch = () -> {
        String threadName = Thread.currentThread().getName();
        Call call = new Call();
        Decide decide = new Decide();
        JSONObject config = call.getConfig("BCH");
        HashMap map = mapSetting(config);
        int result = decide.pattern1(map, priceMap);
        if(result == 1) {/* 1 : 구매*/
            boolean flag = call.buy((String) map.get("currency"), (Integer) map.get("ask1"), Util.krwToUnits((Integer) map.get("avakrw"), (Integer) map.get("ask1")));
            if(!flag) {
                call.buy((String) map.get("currency"), (Integer) map.get("ask2"), Util.krwToUnits((Integer) map.get("avakrw"), (Integer) map.get("ask2")));
            }
        } else if(result == 2) {/* 2 : 판매*/
            boolean flag = call.sell((String) map.get("currency"), (Integer) map.get("bid1"), Util.getUnits((String) map.get("avacoin")));
            if(!flag) {
                call.sell((String) map.get("currency"), (Integer) map.get("bid2"), Util.getUnits((String) map.get("avacoin")));
            }
        }
    };

    private static HashMap mapSetting(JSONObject config) {
        HashMap set = new HashMap();

        set.put("avacoin", config.getString("avacoin"));
        set.put("usekrw", config.getInt("usekrw"));
        set.put("price", config.getString("price"));
        set.put("avakrw", config.getInt("avakrw"));
        set.put("usecoin", config.getString("usecoin"));
        set.put("currency", config.getString("currency"));

        double current_money = config.getDouble("avakrw");
        /*구매*/
        JSONArray ja= config.getJSONArray("asks");
        JSONObject ask1 = ja.getJSONObject(0);
        JSONObject ask2 = ja.getJSONObject(1);
        List<Integer> askPrice = new LinkedList();
        askPrice.add(ask1.getInt("price"));
        askPrice.add(ask2.getInt("price"));
        Collections.sort(askPrice);
        for(int i=0; i<askPrice.size(); i++) {
            if(ask1.getInt("price") == askPrice.get(i)) {
                set.put("ask"+(i+1), ask1.getInt("price"));
                set.put("ask_quantity"+(i+1), ask1.getString("quantity"));
            }
            if(ask2.getInt("price") == askPrice.get(i)) {
                set.put("ask"+(i+1), ask2.getInt("price"));
                set.put("ask_quantity"+(i+1), ask2.getString("quantity"));
            }
        }
        int price = Integer.parseInt((String)priceMap.get(config.getString("currency")+"price"));
        int min = (int) (price - (price  * (0.01 * min_per)));
        set.put("min_money", min);


        /*판매*/
        ja= config.getJSONArray("bids");
        JSONObject bid1 = ja.getJSONObject(0);
        JSONObject bid2 = ja.getJSONObject(1);
        List<Integer> bidPrice = new LinkedList();
        bidPrice.add(bid1.getInt("price"));
        bidPrice.add(bid2.getInt("price"));
        Collections.reverse(bidPrice);
        for(int i=0; i<bidPrice.size(); i++) {
            if(bid1.getInt("price") == bidPrice.get(i)) {
                set.put("bid"+(i+1), bid1.getInt("price"));
                set.put("bid_quantity"+(i+1), bid1.getString("quantity"));
            }
            if(bid2.getInt("price") == bidPrice.get(i)) {
                set.put("bid"+(i+1), bid2.getInt("price"));
                set.put("bid_quantity"+(i+1), bid2.getString("quantity"));
            }
        }
        int max = (int)(price + (price * (0.01 * max_per)));
        set.put("max_money", max);
        return set;
    }


    public static void main(String args[]) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
        try {
//            File file = new File("log_"+sdf.format(new Date())+".txt");
//            PrintStream printStream = new PrintStream(new FileOutputStream(file));
//            PrintStream sysout = System.out;
//            System.setOut(printStream);


            priceMap.put("BTCprice", last_price);/*테스트용*/
            priceMap.put("BTCsearch", "2"); /*테스트용*/
//            priceMap.put("ETHprice", last_price);/*테스트용*/
//            priceMap.put("ETHsearch", "2"); /*테스트용*/
//            priceMap.put("BCHprice", last_price);/*테스트용*/
//            priceMap.put("BCHsearch", "2"); /*테스트용*/
            while(true) {
                Thread thread1 = new Thread(btc);
                thread1.start();
//                Thread thread2 = new Thread(eth);
//                thread2.start();
//                Thread thread3 = new Thread(bch);
//                thread3.start();
                Thread.sleep(2000);
            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

