package com.ninelock.api.utils;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ninelock.api.entity.RealOfferSymbol;
import com.ninelock.api.mapper.RealOfferSymbolMapper;
import com.ninelock.api.response.QntRobotResponse;

public class SendHttpReqUtil implements Runnable{
    private String url;
    private String body;
    private String bodyLength;
    private RealOfferSymbolMapper realOfferSymbolMapper;
    private RealOfferSymbol realOfferSymbol;

    public SendHttpReqUtil(String url, String body, String bodyLength, RealOfferSymbolMapper realOfferSymbolMapper, RealOfferSymbol realOfferSymbol ){
        this.url = url;
        this.body = body;
        this.bodyLength = bodyLength;
        this.realOfferSymbolMapper = realOfferSymbolMapper;
        this.realOfferSymbol = realOfferSymbol;
    }

    @Override
    public void run() {
        try {
            String result = "";
            if(!"".equals(bodyLength)){
                HttpResponse response = HttpRequest.post(url).body(body).header(Header.CONTENT_TYPE, "application/json").header("Data-Length", bodyLength).timeout(60 * 1000).execute();
                System.out.println(response);
                result = response.body();
                QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                if(response.isOk() && qntRobotResponse.getSuccess()){
                    realOfferSymbolMapper.updateById(realOfferSymbol);
                }
            } else {
                HttpResponse response = HttpRequest.post(url).body(body).header(Header.CONTENT_TYPE, "application/json").timeout(60 * 1000).execute();
                System.out.println(response);
                result = response.body();
                QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                if(response.isOk() && qntRobotResponse.getSuccess()){
                    realOfferSymbolMapper.updateById(realOfferSymbol);
                }
            }
            System.out.println(result);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
