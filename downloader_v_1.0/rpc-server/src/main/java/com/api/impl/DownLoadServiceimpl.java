package com.api.impl;

import com.api.DownLoadService;
import com.api.manage.DownLoadManager;
import com.api.manage.SliceInfo;
import com.api.manage.SlicePageInfo;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.api.manage.DownLoadManager.getTotalSize;
import static com.api.manage.DownLoadManager.splitPage;

public class DownLoadServiceimpl implements DownLoadService {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private String uuid;
    CountDownLatch threads;
    CountDownLatch mainLatch;
    static  final String tempPath = "E://Java";

    @Override
    public File download(String downloadUrl) {
        long size = getTotalSize(downloadUrl);
        SlicePageInfo slicePageInfo = splitPage(size);
        String uuids = UUID.randomUUID().toString();
        uuid = "test.png";
        threads = new CountDownLatch(Math.toIntExact(slicePageInfo.getPages()));
        mainLatch = new CountDownLatch(1);

        executorService.execute(() -> {
            try {
                threads.await();
                DownLoadManager.mergeFileTranTo(tempPath, uuid, slicePageInfo.getPages());
                mainLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        for (SliceInfo sliceInfo : slicePageInfo.getSliceInfoList()) {
            executorService.submit(() -> {
                DownLoadManager.download(tempPath, downloadUrl, sliceInfo, uuid);
                threads.countDown();
            });
        }

        try{
            mainLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new File(tempPath,uuid);

    }
}
