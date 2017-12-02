package common;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class Call {
    static Logger log = LoggerFactory.getLogger(Decide.class);

    String apiKey = "";
    String secretKey = "";
    private Api_Client api = null;

    public Call() {
        api = new Api_Client(apiKey,
                secretKey);
    }

    public JSONObject getResult(String alias, String currency, String order_id) {
        String result = "";
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", currency);
        rgParams.put("payment_currency", "KRW");

        try {

            /*구매 거래 주문 등록 또는 진행 중인 거래 취소*/
            if(alias.equals("OR_CANCEL")) {
                rgParams.put("count", "10");
                rgParams.put("currency", currency);
                rgParams.put("order_id", order_id);
                result = api.callApi("/trade/cancel", rgParams);
            /*구매 거래 주문 등록 또는 진행 중인 거래*/
            } else if(alias.equals("OR_ASK")) {
                rgParams.put("type", "ask");
                rgParams.put("count", "2");
                rgParams.put("after", Util.getRemoveMinuteTime(-1));
                rgParams.put("currency", currency);
                rgParams.put("order_id", order_id);
                result = api.callApi("/info/orders", rgParams);
            /*판매 거래 주문 등록 또는 진행 중인 거래*/
            } else if(alias.equals("OR_BID")) {
                rgParams.put("type", "bid");
                rgParams.put("count", "2");
                rgParams.put("after", Util.getRemoveMinuteTime(-1));
                rgParams.put("currency", currency);
                rgParams.put("order_id", order_id);
                result = api.callApi("/info/orders", rgParams);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        JSONObject jo = new JSONObject(result);
        return jo;
    }

    /* 빗썸 url 호출 */
    public JSONObject getResult(String alias, String currency) {
        String result = "";
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", currency);
        rgParams.put("payment_currency", "KRW");


        try {
            /*회원 거래 내역*/
            if(alias.equals("UT")) {
                rgParams.put("searchGb", "0");
                rgParams.put("currency", currency);
                result = api.callApi("/info/user_transactions", rgParams);
            /*회원 구매 체결 내역*/
            } else if(alias.equals("OD_ASK")) {
                rgParams.put("type", "ask");
                result = api.callApi("/info/order_detail", rgParams);
            /*회원 판매 체결 내역*/
            } else if(alias.equals("OD_BID")) {
                rgParams.put("type", "bid");
                result = api.callApi("/info/order_detail", rgParams);
            /*거래소 회원 지갑 정보*/
            } else if(alias.equals("BL")) {
                rgParams.put("currency", currency);
                result = api.callApi("/info/balance", rgParams);
            /*거래소 판/구매 등록 대기 또는 거래 중 내역 정보*/
            } else if (alias.equals("OB")) {
                result = api.callApi("/public/orderbook/"+currency, rgParams);
            /*거래소 회원 정보*/
            } else if (alias.equals("ACC")) {
                result = api.callApi("/info/account", rgParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug(result);
        JSONObject jo = new JSONObject(result);
        return jo;
    }

    /* 거래소에서 가져온 정보 재정리 */
    public JSONObject getConfig(String currency) {
        JSONObject config = new JSONObject();

        /*user transaction 정의*/

        try {
            JSONObject ut = getResult("UT", currency);
            JSONArray uta = ut.getJSONArray("data");
            HashMap dateMap = new HashMap();
            HashMap searchMap = new HashMap();
            List dateList = new LinkedList();
            for(int i=0; i<uta.length(); i++) {
                JSONObject o = uta.getJSONObject(i);
                String search = o.getString("search");
                if(search.equals("1") || search.equals("2")) {
                    dateMap.put(o.getString("transfer_date"),o.getString(currency.toLowerCase()+"1krw"));
                    searchMap.put(o.getString("transfer_date"),search);
                    dateList.add(o.getString("transfer_date"));
                }
            }
            DescendingStr descendingStr = new DescendingStr();
            Collections.sort(dateList, descendingStr);

            config.put("price", dateMap.get(dateList.get(0)));
            config.put("search", searchMap.get(dateList.get(0)));
            /* 최근거래 판매 또는 구매 금액*/

        } catch(IndexOutOfBoundsException e) {
            config.put("price", "0");
        }

        /* orderbook 정의*/
        try {
            JSONObject ob = getResult("OB", currency);
            List askList = new LinkedList();
            HashMap quantity = new HashMap();
            List bidList = new LinkedList();
            ob = ob.getJSONObject("data");
            config.put("currency", ob.getString("order_currency"));
            JSONArray askJA = ob.getJSONArray("asks");
            JSONArray bidJA = ob.getJSONArray("bids");
            /*판매(매도) 설정 asc 사야될 코인*/
            for(int i=0; i<askJA.length(); i++) {
                JSONObject jo = askJA.getJSONObject(i);
                askList.add(jo.getString("price"));
                quantity.put(jo.getString("price"), jo.getString("quantity"));
            }
            AscendingStr ascendingStr= new AscendingStr();
            Collections.sort(askList, ascendingStr);

            JSONArray putAr = new JSONArray();
            for(int ii =0; ii<askList.size(); ii++) {
                JSONObject pjo = new JSONObject();
                pjo.put("price", askList.get(ii));
                pjo.put("quantity", quantity.get(askList.get(ii)));
                putAr.put(pjo);
            }
            config.put("asks",putAr);

            /*구매(매수) 설정 desc 팔아야될 코인*/
            for(int i=0; i<bidJA.length(); i++) {
                JSONObject jo = bidJA.getJSONObject(i);
                bidList.add(jo.getString("price"));
                quantity.put(jo.getString("price"), jo.getString("quantity"));
            }
            DescendingStr descendingStr = new DescendingStr();
            Collections.sort(bidList, descendingStr);

            putAr = new JSONArray();
            for(int ii =0; ii<bidList.size(); ii++) {
                JSONObject pjo = new JSONObject();
                pjo.put("price", bidList.get(ii));
                pjo.put("quantity", quantity.get(bidList.get(ii)));
                putAr.put(pjo);
            }
            config.put("bids",putAr);

            /*지갑 정보 정의*/
            JSONObject bl = getResult("BL", currency);
            JSONObject bljo = bl.getJSONObject("data");
            String avacoin = bljo.getString("available_"+currency.toLowerCase()); /*잔여 코인*/
            int avakrw = bljo.getInt("available_krw"); /*잔여 금액*/
            String usecoin = bljo.getString("in_use_"+currency.toLowerCase()); /*사용 코인*/
            int usekrw = bljo.getInt("in_use_krw"); /*사용 금액*/
            config.put("avacoin", avacoin );
            config.put("avakrw", avakrw );
            config.put("usecoin", usecoin );
            config.put("usekrw", usekrw );


        } catch(Exception e) {
            log.debug(e.getMessage());
        }

        return config;
    }

    /*판매 로직*/
    public int sell(String currency, String bidPrice, String units) {
        int flag = 2;
        log.debug("판매 수량 : " + units);
        if(units.equals("0")) {
            log.debug("수량 부족");
            flag = 3;
        } else {
            HashMap<String, String> rgParams = new HashMap<String, String>();
            rgParams.put("order_currency", currency);
            rgParams.put("payment_currency", "KRW");
            rgParams.put("units", units);
            rgParams.put("price", String.valueOf(bidPrice));
            rgParams.put("type", "ask");
            String rr = api.callApi("/trade/place", rgParams);
            log.debug("판매 result : " + rr);
            JSONObject rrJO = new JSONObject(rr);

            if (rrJO.getString("status").equals("0000")) {
                flag = 1;
            }
        }
        return flag;
    }


    /*구매 로직*/
    public int buy(String currency, String askPrice, String units) {
        log.debug("구매 수량 : " + units);
        int  flag = 2;
        if(units.equals("0")) {
            log.debug("금액 부족");
            flag = 3;
        } else {
            HashMap<String, String> rgParams = new HashMap<String, String>();
            rgParams.put("order_currency", currency);
            rgParams.put("payment_currency", "KRW");
            rgParams.put("units", units);
            rgParams.put("price", String.valueOf(askPrice));
            rgParams.put("type", "bid");
            String rr = api.callApi("/trade/place", rgParams);
            JSONObject rrJO = new JSONObject(rr);
            if (rrJO.getString("status").equals("0000")) {
                log.debug("구매 결과 진입");
                flag = 1;
            }
        }
        return flag;
    }

    /*maker fee 판매*/
    public int makerSell(String currency, JSONArray bidPrices, String units) {
        log.debug("판매 수량 : " + units);
        int  flag = 2;
        if(units.equals("0")) {
            log.debug("판매수량 부족");
            flag = 3;
        } else {
            HashMap<String, String> rgParams = new HashMap<String, String>();
            rgParams.put("currency", currency);

            String temp_unit = units;
            boolean sellSuc = false;
            for(int i=0; i<bidPrices.length(); i++) {
                JSONObject bidJO = bidPrices.getJSONObject(0);
                String bidQuantity = bidJO.getString("quantity");
                String bidPrice = bidJO.getString("price");
//                rgParams.put("price", String.valueOf(bidPrice));
                if(!sellSuc) {

                    if(Double.parseDouble(temp_unit) <= Double.parseDouble(bidQuantity)) {
                        rgParams.put("units", temp_unit);
                    } else {
                        temp_unit = String.valueOf(Double.parseDouble(temp_unit) - Double.parseDouble(bidQuantity));
                        rgParams.put("units", temp_unit);
                    }

                    /*maker 구매*/
                    String rr = api.callApi("/trade/market_sell", rgParams);
                    /*구매 debug*/
//                    String rr = "{\"status\":\"0000\"}";
                    JSONObject rrJO = new JSONObject(rr);
                    if(rrJO.getString("status").equals("0000")) {
                        /* 구매 체결 확인 */
                        JSONArray sellResult = rrJO.getJSONArray("data");
                        JSONObject orBid = getResult("OR_ASK", currency, rrJO.getString("order_id"));
                        log.debug(orBid.toString());
                        double remainingUnit = Double.parseDouble(temp_unit) - Double.parseDouble(sellResult.getJSONObject(0).getString("units"));
                        int ru = (int) remainingUnit;
                        temp_unit = String.valueOf(ru);
                        if(orBid.getString("status").equals("5600")) {
                            log.debug("조회된 내역이 없음");
                            if (Integer.parseInt(temp_unit) == 0) {
                                sellSuc = true;
                                flag = 1;
                            }
                        } else if(orBid.getString("status").equals("0000")) {
                            getResult("OR_CANCEL", currency, orBid.getString("order_id"));
                            if (Integer.parseInt(temp_unit) == 0) {
                                sellSuc = true;
                                flag = 1;
                            }
                        }

                    }
                }
            }
        }
        return flag;
    }


    /*maker fee 구매*/
    public int makerBuy(String currency, JSONArray askPrices, String krw) {
        log.debug("구매 금액: " + krw);
        int  flag = 2;
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("currency", currency);
        String temp_krw = krw;
        boolean buySuc = false;
        for(int i=0; i<askPrices.length(); i++) {
            JSONObject askJO = askPrices.getJSONObject(0);
            String askQuantity = askJO.getString("quantity");
            String askPrice = askJO.getString("price");
            double mv = Integer.parseInt(askPrice) * Double.parseDouble(Util.getCurrencyMinQuantity(currency));
            String minValue = String.valueOf((int)mv);
            rgParams.put("price", String.valueOf(askPrice));
            String temp_unit = Util.krwToUnits(currency, krw, askPrice);
            if(!buySuc) {

                if(Double.parseDouble(temp_unit) <= Double.parseDouble(askQuantity)) {
                    rgParams.put("units", temp_unit);
                } else {
                    String.valueOf(Double.parseDouble(temp_unit) - Double.parseDouble(askQuantity));
                    krw = Util.decimalRemove(Integer.parseInt(krw) - (Integer.parseInt(minValue) * Double.parseDouble(askQuantity)));
                    rgParams.put("units", askQuantity);
                }

                /*판매 호출*/
                String rr = api.callApi("/trade/market_buy", rgParams);
                /*판매 debug*/
//                String rr = "{\"status\":\"0000\"}";
                JSONObject rrJO = new JSONObject(rr);

                if(rrJO.getString("status").equals("0000")) {
                    /* 구매 체결 확인 */
                    JSONObject orAsk = getResult("OR_BID",currency,rrJO.getString("order_id"));
                        /* 구매 체결 확인 debug */
//                    JSONObject orAsk = new JSONObject("{\"status\":\"5600\"}");
                    log.debug(orAsk.toString());
                    if(orAsk.getString("status").equals("5600")) {
                        log.debug("서버오류 order 체크 제외");
                        /*금액비교*/
                        JSONArray buyResult = rrJO.getJSONArray("data");
                        krw = String.valueOf(Integer.parseInt(krw) - buyResult.getJSONObject(0).getInt("total"));
                        if(Integer.parseInt(minValue) > Integer.parseInt(krw)) {
                            buySuc = true;
                            flag = 1;
                        }
                    } else if(orAsk.getString("status").equals("0000")) {
                        getResult("OR_CANCEL", currency, orAsk.getString("order_id"));
                        if(Integer.parseInt(minValue) > Integer.parseInt(krw)) {
                            buySuc = true;
                            flag = 1;
                        }
                    }
                }
            }
        }
        return flag;
    }

    /*구매해야될 currency 갯수*/
    public int getBuyCurrencyCnt(JSONObject config) {
        int avaCnt = 1;
        JSONArray useCurrency = config.getJSONArray("useCurrency");
        String currency = config.getString("currency");
        for(int i=0; i<useCurrency.length(); i++) {
            if(!currency.equals(useCurrency.getString(i))) {
                JSONObject ut = getResult("UT", currency);
                JSONArray utJa = ut.getJSONArray("data");
                HashMap utMap = new HashMap();
                List utList = new LinkedList();
                for(int ii=0; ii< utJa.length(); ii++) {
                    JSONObject utJo = utJa.getJSONObject(ii);
                    String utSearch = utJo.getString("search");
                    if(utSearch.equals("1") || utSearch.equals("2")) {
                        utMap.put(utJo.getString("transfer_date"), utJo.getString("search"));
                        utList.add(utJo.getString("transfer_date"));
                    }
                }
                DescendingStr descendingStr = new DescendingStr();
                Collections.sort(utList, descendingStr);
                try {
                    String lastSearch = (String) utMap.get((String) utList.get(0));
                    if (lastSearch.equals("2")) {
                        avaCnt++;
                    }
                } catch(Exception e) {
                    avaCnt++;
                }
            }
        }
        return avaCnt;
    }

    // 내림차순
    class Descending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

    }

    // 오름차순
    class Ascending implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }

    }
    // 내림차순
    class DescendingStr implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o2.compareTo(o1);
        }

    }

    // 오름차순
    class AscendingStr implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }

    }
}
