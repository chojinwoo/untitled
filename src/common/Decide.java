package common;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class Decide {
    private double btc_min_per = 3;
    private double btc_max_per = 3;
    Call call = new Call();

    public int pattern1(JSONObject config, HashMap price) {

        int result = 0;
        System.out.println(config);
        System.out.println(price);

        String myKrw = String.valueOf(config.getInt("avakrw"));
        String avaCoin = config.getString("avacoin");
        String currency = config.getString("currency");
        String currency_price = (String) price.get(currency+"price");
        String currency_search = (String) price.get(currency+"search");
        String min_money = Util.getMinMoney(currency_price, (Double) price.get(currency+"_min_per"));
        String max_money = Util.getMaxMoney(currency_price, (Double) price.get(currency+"_max_per"));
        System.out.println("min : " + min_money + " max : " + max_money);
        /*order book 내역*/
        JSONArray asksJA = config.getJSONArray("asks");
        List asksList = new LinkedList();
        asksList.add(asksJA.getJSONObject(0).getString("price"));
        asksList.add(asksJA.getJSONObject(1).getString("price"));
        Collections.reverse(asksList);
        JSONArray bidsJA = config.getJSONArray("bids");
        List bidsList = new LinkedList();
        bidsList.add(bidsJA.getJSONObject(0).getString("price"));
        bidsList.add(bidsJA.getJSONObject(1).getString("price"));
        Collections.sort(bidsList);

        String ask_price1 = (String)asksList.get(0);
        String ask_price2 = (String)asksList.get(1);

        String bid_price1 = (String)bidsList.get(0);
        String bid_price2 = (String)bidsList.get(1);

        System.out.println(currency + " start");
        if(currency_search == null || currency_search.equals("0")) {
            String search = config.getString("search");
            System.out.println(currency + "초기값"); /* 1: 구매 2: 판매*/
            System.out.println("판매금액 주입");
            price.put(currency + "price", ask_price1);
            price.put(currency+"search", "2");
            price.put(currency+"cnt", "0");
            if(search.equals("2")) {
                price.put(currency+"search", "1");
                price.put(currency + "price", bid_price1);
            }

        } else {
            currency_search = (String) price.get(currency+"search");
            if (currency_search.equals("1")) {
//                /*구매*/
                if(Integer.parseInt(currency_price) > Integer.parseInt(ask_price1)) {
                    System.out.println("최근 판매액 보다 낮음");
                    if(Integer.parseInt(min_money) > Integer.parseInt(ask_price1)) {
                        System.out.println("1% 하락");
                        price.put(currency+"price", ask_price1);
                        price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) + 1));
                    }
                } else {

                    price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) - 1));
                    if(Integer.parseInt(max_money) < Integer.parseInt(ask_price1)) {
                        if(Integer.parseInt((String)price.get(currency+"cnt")) < 1) {
                            System.out.println("구매 1%상승");
                            System.out.println("SUCCESS 구매 프로세서");
                            int avaCnt = call.getAvaCnt(config); /* 구매할 비트코인종류의 카운트 */
                            String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                            boolean flag = call.buy(currency, ask_price1, Util.krwToUnits(currency, myKrw, ask_price1));

                            if (!flag) {
                                System.out.println("구매 실패");
                                flag = call.buy(currency, ask_price2, Util.krwToUnits(currency, krw, ask_price2));
                            }

                            if(flag) {
                                price.put(currency + "price", bid_price1);
                                price.put(currency + "search", "1");
                            }
                        }

                        System.out.println("1% 상승 하락 카운트 초기화 현재 카운트  : " + price.get(currency+"cnt"));
                        price.put(currency+"cnt", "0");
                    }
                }
            } else if (currency_search.equals("2")) {
            /*판매*/
                if(Integer.parseInt(currency_price) < Integer.parseInt(bid_price1)) {
                    System.out.println("최근 거래 금액보다 높음");
                    if(Integer.parseInt(max_money) < Integer.parseInt(bid_price1)) {
                        System.out.println("1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(currency+"price", bid_price1);
                        System.out.println(price);
                    }
                } else if(Integer.parseInt(currency_price) > Integer.parseInt(bid_price1)) {
                    System.out.println("최근 거래 금액보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(bid_price1)) {
                        System.out.println("1%하락 판매");
                        System.out.println("SUCCESS 판매 프로세스 실행");
                        boolean flag = call.sell(currency, bid_price1, Util.getUnits(currency, avaCoin));
                        if(!flag) {
                            System.out.println("구매 실패");
                            flag = call.buy(currency, bid_price2, Util.krwToUnits(currency, myKrw, bid_price2));
                        }
                        if(flag) {
                            price.put(currency+"price", ask_price1);
                            price.put(currency+"search", "2");
                        }
                    }
                }
            }
        }
        return result;
    }
}