package com.example.servicepractice;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private DownloadListener downloadListener;

    private boolean isCancelled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        downloadListener = listener;
    }

    @Override
    protected void onPostExecute(Object o) {
        switch ((int) o){
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        int progress = (int)values[0];
        if(progress > lastProgress ){
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        long downloadLength = 0;
        String downloadURL =(String) objects[0];
        String fileName = downloadURL.substring(downloadURL.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(directory+fileName);
        if(file.exists()){
            downloadLength = file.length();
        }
        try {
            long contentLength = getConteneLength(downloadURL);
            if(contentLength==0)
                return TYPE_FAILED;
            else if(contentLength == downloadLength)
                return TYPE_SUCCESS;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().addHeader("RANGE","bytes="+downloadLength+"-").url(downloadURL).build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                savedFile.seek(downloadLength);
                byte[] b =new byte[1024];
                int total = 0;
                int  len;
                while((len = is.read(b)) != -1){
                    if(isCancelled){
                        return TYPE_CANCELED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total += len;
                        savedFile.write(b,0,len);
                        int progress = (int) ((total + downloadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if (is != null)
                    is.close();
                if(savedFile != null)
                    savedFile.close();
                if(isCancelled && file != null)
                    file.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    private long getConteneLength(String downloadURL) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(downloadURL).build();
        Response response = okHttpClient.newCall(request).execute();
        if(request != null && response.isSuccessful()){
            long contentLengt = response.body().contentLength();
            response.body().close();
            return contentLengt;
        }
        return 0;
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCancelled = false;
    }
}
