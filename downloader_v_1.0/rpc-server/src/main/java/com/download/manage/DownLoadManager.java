package com.download.manage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DownLoadManager {
    public final static long SLICE_SIZE = 1<<20;
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    public static long getTotalSize(String downloadUrl){
        //大小探测
        ResponseEntity<byte[]> responseEntity = DownLoadManager.getFileContentByUrlAndPosition(downloadUrl, 0, 1);
        HttpHeaders headers = responseEntity.getHeaders();
        String rangeBytes = headers.getFirst("Content-Range");

        if (Objects.isNull(rangeBytes)) {
            log.error("url:{},不支持分片下载", downloadUrl);
        }

        long allBytes = Long.parseLong(rangeBytes.split("/")[1]);
        log.info("文件总大小：{}M", allBytes / 1024.0 / 1024.0);
        return allBytes;

    }

    public static ResponseEntity<byte[]> getFileContentByUrlAndPosition(String downloadUrl, long start, long end) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Range", "bytes=" + start + "-" + end);

        org.springframework.http.HttpEntity<Object> httpEntity = new org.springframework.http.HttpEntity<>(httpHeaders);
        return REST_TEMPLATE.exchange(downloadUrl, HttpMethod.GET, httpEntity, byte[].class);
    }

    public static boolean download(String tempPath, String downloadUrl, SliceInfo sliceInfo, String fName) {
        log.info("下载分片文件：{}，分片序号 {}", fName, sliceInfo.getPage_size());
        log.info("downloading ... " + Thread.currentThread().getName());


        // 创建一个分片文件对象
        File file = new File(tempPath, sliceInfo.getPage_size() + "-" + fName);

        if (file.exists() && file.length() == SLICE_SIZE) {
            log.info("此分片文件 {} 已存在", sliceInfo.getPage_size());
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(file);) {
            ResponseEntity<byte[]> responseEntity = DownLoadManager.getFileContentByUrlAndPosition(downloadUrl, sliceInfo.getSt(), sliceInfo.getEd());

            byte[] body = responseEntity.getBody();
            if (body != null && body.length == 0) {
                log.warn("分片文件：{},没有内容", file.getName());
                return false;
            }
            // 将分片内容写入临时存储分片文件
            fos.write(body);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean mergeFileTranTo(String tempPath, String fName, long page) {
        log.info("merging...",Thread.currentThread().getName());
        try (FileChannel channel = new FileOutputStream(new File(tempPath, fName)).getChannel()) {
            for (long i = 1; i <= page; i++) {
                File file = new File(tempPath, i + "-" + fName);
                FileChannel fileChannel = new FileInputStream(file).getChannel();
                long size = fileChannel.size();
                for (long left = size; left > 0; ) {
                    left -= fileChannel.transferTo((size - left), left, channel);
                }
                fileChannel.close();
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }finally {
            return true;
        }

    }

    public static SlicePageInfo splitPage(long allBytes) {
        CopyOnWriteArrayList<SliceInfo> list = new CopyOnWriteArrayList<>();
        long size = allBytes;
        long left = 0;
        long page = 0;
        while (size > 0) {
            long start = 0;
            long end;
            start = left;
            //分页
            if (size < SLICE_SIZE) {
                end = left + size;
            } else {
                end = left += SLICE_SIZE;
            }
            size -= SLICE_SIZE;
            page++;
            if (start != 0) {
                start++;
            }
            log.info("页码：{}，开始位置：{}，结束位置：{}", page, start, end);
            final SliceInfo sliceInfo = new SliceInfo(start, end, page);
            list.add(sliceInfo);
        }
        SlicePageInfo slicePageInfo = new SlicePageInfo();
        slicePageInfo.setSliceInfoList(list);
        slicePageInfo.setPages(page);
        return slicePageInfo;
    }
}
