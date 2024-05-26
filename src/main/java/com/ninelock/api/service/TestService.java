package com.ninelock.api.service;

import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.ninelock.api.response.QntDetectResponse;
import com.ninelock.api.response.QntRobotResponse;
import com.ninelock.api.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {

    public Result<?> dbtest(){
        String reqUrl = "http://localhost:8001/api/v1/detect/getDetectIndexList";
        HttpResponse response = HttpRequest.get(reqUrl).timeout(60 * 1000).execute();
        System.out.println(response);
        if(response.isOk()){
            String result = response.body();
            System.out.println(result);
            QntDetectResponse qntRobotResponse = JSONUtil.toBean(result, QntDetectResponse.class);
            if(qntRobotResponse.getSuccess()){
                Object data = qntRobotResponse.getData();
                return Result.ok(data);
            }
            String message = qntRobotResponse.getMessage();
            return Result.error(message, null);
        }
        return Result.ok();
    }
}
