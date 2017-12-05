package common;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class Decide {
    Call call = new Call();
    static Logger log = LoggerFactory.getLogger(Decide.class);

    /* 구매 패턴 1*/
    public void pattern1(JSONObject config, HashMap price) {

        String myKrw = String.valueOf(config.getInt("avakrw"));
        String avaCoin = config.getString("avacoin");
        String currency = config.getString("currency");
        String currency_price = (String) price.get(currency+"price");
        String currency_search = (String) price.get(currency+"search");
        String min_money = Util.getMinMoney(currency_price, (Double) price.get(currency+"_min_per"));
        String max_money = Util.getMaxMoney(currency_price, (Double) price.get(currency+"_max_per"));
        log.debug(currency + " start");
        log.debug("default price : " +price.get(currency+"price"));
        log.debug("min : " + min_money + " max : " + max_money);
        /*order book 내역*/
        JSONArray asksJA = config.getJSONArray("asks");
        JSONArray bidsJA = config.getJSONArray("bids");

        String ask_price1 = asksJA.getJSONObject(0).getString("price");
        String ask_price2 = asksJA.getJSONObject(1).getString("price");

        String bid_price1 = bidsJA.getJSONObject(0).getString("price");
        String bid_price2 = bidsJA.getJSONObject(1).getString("price");

        if(currency_search == null || currency_search.equals("0")) {
            String search = config.getString("search");
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
                /*구매*/
                log.debug("ask1 : " + ask_price1 + " ask2 : " + ask_price2);
                if(Integer.parseInt(currency_price) > Integer.parseInt(ask_price2)) {
                    log.debug("구매 : 판매액 보다 낮음");
                    if(Integer.parseInt(min_money) > Integer.parseInt(ask_price2)) {
                        log.debug("1% 하락");
                        price.put(currency+"price", ask_price1);
                        price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) + 1));
                    }
                } else {
                    log.debug("구매 : 판매액 보다 높음");
                    price.put(currency+"cnt", String.valueOf(Integer.parseInt((String)price.get(currency+"cnt")) - 1));
                    if(Integer.parseInt(max_money) < Integer.parseInt(ask_price1)) {
                        if(Integer.parseInt((String)price.get(currency+"cnt")) < 1) {
                            log.debug("구매 : 1%상승");
                            log.debug("SUCCESS 구매 프로세서");
                            int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                            String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                            int flag = call.buy(currency, ask_price1, Util.krwToUnits(currency, krw, ask_price1));

                            log.debug(config.toString());
                            log.debug("구매 결과값 : " + flag);
                            switch (flag) {
                                case 1 :
                                    price.put(currency + "price", bid_price1);
                                    price.put(currency + "search", "2");
                                    break;
                                case 2:
                                    log.debug("구매 실패");
                                    int subFlag = call.buy(currency, ask_price2, Util.krwToUnits(currency, krw, ask_price2));
                                    switch(subFlag) {
                                        case 1 :
                                            price.put(currency + "price", bid_price1);
                                            price.put(currency + "search", "2");
                                            break;
                                        case 3 :
                                            log.debug("잔액부족");
                                            break;
                                    }
                                    break;
                                case 3:
                                    log.debug("잔액부족");
                                    break;
                            }
                        } else {
                            log.debug("구매 : 1% 상승 하락 카운트 초기화 현재 카운트  : " + price.get(currency + "cnt"));
                            price.put(currency + "cnt", "0");
                        }
                    }
                }
            } else if (currency_search.equals("2")) {
            /*판매*/
                log.debug("bid1 : " + bid_price1 + " bid2 : " + bid_price2);
                if(Integer.parseInt(currency_price) < Integer.parseInt(bid_price1)) {
                    log.debug("판매 : 구매 금액보다 높음");
                    if(Integer.parseInt(max_money) < Integer.parseInt(bid_price1)) {
                        log.debug("1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(currency+"price", bid_price1);
                    }
                } else if(Integer.parseInt(currency_price) > Integer.parseInt(bid_price1)) {
                    log.debug("판매 : 구매 금액보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(bid_price1)) {
                        log.debug("1%하락 판매");
                        log.debug("SUCCESS 판매 프로세스 실행");
                        log.debug(config.toString());
                        int flag = call.sell(currency, bid_price1, Util.getDecimalRemoveUnits(currency, avaCoin));

                        log.debug("판매 결과값 : " + flag);
                        switch (flag) {
                            case 1:
                                price.put(currency+"price", ask_price1);
                                price.put(currency+"search", "1");
                                break;
                            case 2:
                                log.debug("판매 실패");
                                int subFlag = call.sell(currency, bid_price1, Util.getDecimalRemoveUnits(currency, avaCoin));
                                switch (subFlag) {
                                    case 1:
                                        price.put(currency+"price", ask_price1);
                                        price.put(currency+"search", "1");
                                        break;
                                    case 3:
                                        log.debug("수량부족");
                                        break;
                                }
                                break;
                            case 3:
                                log.debug("수량부족");
                                break;
                        }
                    }
                }
            }
        }
    }


    /* 지정가 거래 maker fee*/
    public void pattern2(JSONObject config, HashMap price) {
        String myKrw = String.valueOf(config.getInt("avakrw"));
        String avaCoin = config.getString("avacoin");
        String currency = config.getString("currency");
        String currency_price = (String) price.get(currency+"price");
        String currency_search = (String) price.get(currency+"search");
        String min_money = Util.getMinMoney(currency_price, (Double) price.get(currency+"_min_per"));
        String max_money = Util.getMaxMoney(currency_price, (Double) price.get(currency+"_max_per"));
        log.debug(currency + " start");
        log.debug("default price : " +price.get(currency+"price"));
        log.debug("min : " + min_money + " max : " + max_money);
        /*order book 내역*/
        JSONArray asksJA = config.getJSONArray("asks");
        JSONArray bidsJA = config.getJSONArray("bids");

        String ask_price1 = asksJA.getJSONObject(0).getString("price");
        String ask_price2 = asksJA.getJSONObject(1).getString("price");
        String ask_price3 = asksJA.getJSONObject(2).getString("price");
        String ask_price4 = asksJA.getJSONObject(3).getString("price");
        String ask_price5 = asksJA.getJSONObject(4).getString("price");

        String bid_price1 = bidsJA.getJSONObject(0).getString("price");
        String bid_price2 = bidsJA.getJSONObject(1).getString("price");
        String bid_price3 = bidsJA.getJSONObject(2).getString("price");
        String bid_price4 = bidsJA.getJSONObject(3).getString("price");
        String bid_price5 = bidsJA.getJSONObject(4).getString("price");

        if(currency_search == null || currency_search.equals("0")) {
            String search = "";
            price.put(currency+"cnt", "0");
            try {
                search = config.getString("search");
                if(search.equals("2")) { /* search = 2 일시 판매 완료 구매개시*/

                    log.debug("SUCCESS 구매 프로세서");
                    int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                    String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                    int flag = call.makerBuy(currency, asksJA, krw);
                    log.debug("구매 결과값 : " + flag);
                    switch (flag) {
                        case 1:
                            price.put(currency + "price", bid_price1);
                            price.put(currency + "search", "1");
                            break;
                        case 2:
                            log.debug("구매 실패");
                            break;
                        case 3:
                            log.debug("수량부족");
                            break;
                    }
                } else if(search.equals("1")) { /* search = 1 구매완료  판매개시*/

                    log.debug("SUCCESS 판매 프로세스 실행");
                    int flag = call.makerSell(currency, bidsJA, Util.getDecimalRemoveUnits(currency, avaCoin));
                    log.debug("판매 결과값 : " + flag);
                    switch (flag) {
                        case 1:
                            price.put(currency+"price", ask_price1);
                            price.put(currency+"search", "2");
                            break;
                        case 2:
                            log.debug("판매 실패");
                            break;
                        case 3:
                            log.debug("수량부족");
                            break;
                    }
                }
            } catch(Exception e) {
                log.debug("SUCCESS 구매 프로세서");
                int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                int flag = call.makerBuy(currency, asksJA, krw);
                log.debug("구매 결과값 : " + flag);
                switch (flag) {
                    case 1:
                        price.put(currency + "price", bid_price1);
                        price.put(currency + "search", "1");
                        break;
                    case 2:
                        log.debug("구매 실패");
                        break;
                    case 3:
                        log.debug("수량부족");
                        break;
                }
            }

        } else {
            currency_search = (String) price.get(currency+"search");
            if (currency_search.equals("1")) {/* search = 1 구매완료  판매개시*/
                /*판매*/
                log.debug("bid1 : " + bid_price1 + " bid2 : " + bid_price2 + " bid3 : " + bid_price3 + " bid4 : " + bid_price4 + " bid5 : " + bid_price5);
                if(Integer.parseInt(currency_price) < Integer.parseInt(bid_price1)) {
                    log.debug("판매 : 구매 금액보다 높음");
                    if(Integer.parseInt(max_money) < Integer.parseInt(bid_price1)) {
                        log.debug("1% 상승 최근구매액 현 매도 금액으로 재설정");
                        price.put(currency+"price", bid_price1);
                    }
                } else if(Integer.parseInt(currency_price) >= Integer.parseInt(bid_price1)) {
                    log.debug("판매 : 구매 금액보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(bid_price1)) {
                        log.debug("1%하락 판매");
                        log.debug("SUCCESS 판매 프로세스 실행");
                        int flag = call.makerSell(currency, bidsJA, Util.getDecimalRemoveUnits(currency, avaCoin));
                        log.debug("판매 결과값 : " + flag);
                        switch (flag) {
                            case 1:
                                price.put(currency+"price", ask_price1);
                                price.put(currency+"search", "2");
                                break;
                            case 2:
                                log.debug("판매 실패");
                                break;
                            case 3:
                                log.debug("수량부족");
                                break;
                        }

                    }
                }
            } else if (currency_search.equals("2")) {/* search = 2 일시 판매 완료 구매개시*/
                /*구매*/
                log.debug("ask1 : " + ask_price1 + " ask2 : " + ask_price2 + " ask3 : " + ask_price3 + " ask4 : " + ask_price4 + " ask5 : " + ask_price5);
                if(Integer.parseInt(currency_price) > Integer.parseInt(ask_price1)) {
                    log.debug("구매 : 판매액 보다 낮음");
                    if (Integer.parseInt(min_money) > Integer.parseInt(ask_price1)) {
                        log.debug("1% 하락");
                        price.put(currency + "price", ask_price1);
                        price.put(currency + "cnt", String.valueOf(Integer.parseInt((String) price.get(currency + "cnt")) + 1));
                    }

                } else {
                    log.debug("구매 : 판매액 보다 높음");
                    if ((Integer.parseInt(min_money) < Integer.parseInt(ask_price1))) {
                        price.put(currency + "cnt", String.valueOf(Integer.parseInt((String) price.get(currency + "cnt")) - 1));
                        if (Integer.parseInt(max_money) < Integer.parseInt(ask_price1)) {
                            if (Integer.parseInt((String) price.get(currency + "cnt")) < 1) {
                                log.debug("구매 : 1%상승");
                                log.debug("SUCCESS 구매 프로세서");
                                int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                                String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                                int flag = call.makerBuy(currency, asksJA, krw);
                                log.debug("구매 결과값 : " + flag);
                                switch (flag) {
                                    case 1:
                                        price.put(currency + "price", bid_price1);
                                        price.put(currency + "search", "1");
                                        break;
                                    case 2:
                                        log.debug("구매 실패");
                                        break;
                                    case 3:
                                        log.debug("수량부족");
                                        break;
                                }
                            } else {
                                log.debug("구매 : 1% 상승 하락 카운트 초기화 현재 카운트  : " + price.get(currency + "cnt"));
                                price.put(currency + "cnt", "0");
                            }
                        }
                    }
                }
            }
        }
    }

    /*구매 패턴3
     * 금일 구매 금액으로 기준금액 정의
     * 자정시 기준금액 현재 판매금액으로 초기화
     * 기준금액에서 3프로 빠질시 판매
     * 3프로 빠진 금액 복귀시 재구매
     * */

    public void pattern3(JSONObject config, HashMap priceMap) {
        boolean loopFlag = true;
        /*order book 내역
         * 최대 최저 금액*/
        JSONArray asksJA = config.getJSONArray("asks");
        JSONArray bidsJA = config.getJSONArray("bids");

        String ask_price1 = asksJA.getJSONObject(0).getString("price"); /* 매도금액 */
        String ask_price2 = asksJA.getJSONObject(1).getString("price"); /* 매도금액 */
        String bid_price1 = bidsJA.getJSONObject(0).getString("price"); /* 매수금액 */
        String bid_price2 = bidsJA.getJSONObject(1).getString("price"); /* 매수금액 */

        String myKrw = String.valueOf(config.getInt("avakrw"));/*남은 현금*/
        String avaCoin = config.getString("avacoin"); /* 남은 코인 */
        String currency = config.getString("currency"); /* 선택한 통화 */
        String search = config.getString("search"); /* 1 : 구매 완료 2: 판매 완료 */
        try {
            String price = config.getString("price"); /* 마지막 거래 금액 */
            priceMap.put(currency + "price", price);
            priceMap.put(currency + "search", search);
        }catch(Exception e) {
            priceMap.put(currency+"price",ask_price1);
            priceMap.put(currency + "search", "2");
        }

        /*하루가 넘어가는날에 기준값 초기화*/
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        String nowDate = sdf.format(new Date());
//        if(!nowDate.equals((String)priceMap.get("nowDate"))) {
//            log.debug("자정 기본값을 초기화 합니다.");
//            priceMap.put("nowDate", nowDate);
//            if(String.valueOf(priceMap.get(currency+"search")).equals("1")) {
//                log.debug("판매중 : " + ask_price1 + "원으로 초기화");
//                priceMap.put(currency + "price", ask_price1);
//            } else {
//                log.debug("가격 하락및 해당금액에 합당하지 못하여 초기화 값 설정 실패");
//                log.debug("수동으로 구매해 주세요.");
//                loopFlag = false;
//            }
//        }
        /* 3시간 단위 기준값 초기화*/
        int dateCnt = 1;
        SimpleDateFormat sdf2 = new SimpleDateFormat("kk");
        int hour = Integer.parseInt(sdf2.format(new Date()));
        if(hour % 2 ==0) {
            if(dateCnt == 1) {
                if(String.valueOf(priceMap.get(currency+"search")).equals("1")) {
                    log.debug("판매중 : " + ask_price1 + "원으로 초기화");
                    priceMap.put(currency + "price", ask_price1);
                } else {
                    log.debug("가격 하락및 해당금액에 합당하지 못하여 초기화 값 설정 실패");
                    log.debug("수동으로 구매해 주세요.");
                    loopFlag = false;
                }
            }
            dateCnt++;
        } else {
            dateCnt = 1;
        }

        if(loopFlag) {
            log.debug(currency + "시작");
            log.debug("현재 금액 : " + myKrw + " 현금 코인 : " + avaCoin + " 최종 거래금액 : " + priceMap.get(currency+"price"));
            log.debug("매도1 : " + ask_price1 + " 매도2 : " + ask_price2);
            log.debug("매수1 : " + bid_price1 + " 매수2 : " + bid_price2);

            /* 구매 완료 판매 개시 */
            if (String.valueOf(priceMap.get(currency + "search")).equals("1")) {
                log.debug("구매 완료 판매 개시");
                /*판매 금액 - 현금액의 2%제외한 금액*/
                String min_money = Util.getMinMoney((String) priceMap.get(currency + "price"), 1.5);
                log.debug("최소금액 : " + min_money);

                if (Integer.parseInt(bid_price1) < Integer.parseInt(min_money)) { /*매수 금액 이 최소금액 보다 작을때 현재 코인 판매*/
                    int flag = call.sell(currency, bid_price1, Util.getDecimalRemoveUnits(currency, avaCoin));
                    log.debug("판매 결과값 : " + flag);
                    switch (flag) {
                        case 1:
                            log.debug("판매 완료");
                            priceMap.put(currency + "price", min_money);
                            priceMap.put(currency + "sellCnt", "1");
                            break;
                        case 2:
                            log.debug("판매 실패");
                            int subFlag = call.sell(currency, bid_price2, Util.getDecimalRemoveUnits(currency, avaCoin));
                            switch (subFlag) {
                                case 1:
                                    log.debug("재판매 성공");
                                    priceMap.put(currency + "price", min_money);
                                    priceMap.put(currency + "sellCnt", "1");
                                    break;
                                case 3:
                                    log.debug("수량부족");
                                    break;
                            }
                            break;
                        case 3:
                            log.debug("수량부족");
                            break;
                    }
                }
                /* 판매완료 구매개시 */
            } else if (String.valueOf(priceMap.get(currency + "search")).equals("2")) {
                log.debug("판매완료 구매개시");
                String sellCnt = (String) priceMap.get(currency + "sellCnt");
                log.debug("1% 미만 카운트 : " + sellCnt);
                String currencyPrice = (String) priceMap.get(currency + "price");
                /*구매 카운트 체크 1%구분으로 확인*/
                String min_money = Util.getMinMoney(currencyPrice, 1);
                log.debug("최소금액 : " + min_money);
                if (Integer.parseInt(min_money) > Integer.parseInt(ask_price1)) {
                    int cnt = Integer.parseInt(sellCnt);
                    priceMap.put(currency + "sellCnt", String.valueOf(cnt + 1));
                }

                if (Integer.parseInt(currencyPrice) <= Integer.parseInt(ask_price1)) {
                    if (Integer.parseInt(sellCnt) > 2) {
                        log.debug("BUY 구매 프로세서");
                        int avaCnt = call.getBuyCurrencyCnt(config); /* 구매할 비트코인종류의 카운트 */
                        String krw = String.valueOf(Integer.parseInt(myKrw) / avaCnt);
                        int flag = call.buy(currency, ask_price1, Util.krwToUnits(currency, krw, ask_price1));
                        switch (flag) {
                            case 1:
                                log.debug("구매 완료");
                                priceMap.put(currency + "sellCnt", "1");
                                break;
                            case 2:
                                log.debug("구매 실패");
                                int subFlag = call.buy(currency, ask_price2, Util.krwToUnits(currency, krw, ask_price2));
                                switch (subFlag) {
                                    case 1:
                                        log.debug("재구매 완료");
                                        break;
                                    case 3:
                                        log.debug("잔액부족");
                                        break;
                                }
                                break;
                            case 3:
                                log.debug("잔액부족");
                                break;
                        }
                    }
                }
            }
        }
    }
}