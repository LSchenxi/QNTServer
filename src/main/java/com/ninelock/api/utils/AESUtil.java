package com.ninelock.api.utils;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class AESUtil {

    public static String getKey(String data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
        String format1 = format.format(new Date());
        String pk1 = format1+data;
        int length1 = pk1.length();
        if(length1 < 24){
            for(int i = 0; i< 24 - length1; i++){
                pk1 = pk1 + "0";
            }
        } else if(length1 > 24){
            pk1 = pk1.substring(0, 24);
        }
        return pk1;
    }

    public static String encryptCBC(String content, String key){
        AES aes = new AES(Mode.valueOf("CBC"), Padding.PKCS5Padding, key.getBytes(), key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
        String encrypt = aes.encryptHex(content);
        return encrypt;
    }

}
