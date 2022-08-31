package com;

import com.rpc.api.DownLoadService;

import java.util.Scanner;

public class ClientBootstrap {
    static Scanner scanner;
    public static void main(String[] args) {
        Client client = new Client(7000);

        DownLoadService downLoadService  = (DownLoadService) client.getProxy(DownLoadService.class);
        if ((downLoadService.download("https://www.baidu.com/img/flexible/logo/pc/result.png") == null)) {
            System.out.println("fail");
        }else{
            System.out.println("success");
        }


    }
}
