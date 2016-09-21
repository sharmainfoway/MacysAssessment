package com.example.ravi.macysassessment;

/**
 * Created by Ravi on 9/19/2016.
 */

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;


/**
 * Created by Ravi on 9/19/2016.
 */

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AsyncFrag extends Fragment {
    private static ArrayList<File> fileList;
    private static ArrayList<File> allFileList;

    private final File sdcard = new File(System.getenv("SECONDARY_STORAGE"));
    private static long avgFileSize=0;
    public static final String TAG_HEADLESS_FRAGMENT = "headless_fragment";
    private int progress = 0;


    public static interface TaskStatusCallback {
        void onPreExecute();

        void onProgressUpdate(int progress);

        void onPostExecute();

        void onCancelled();
    }
    TaskStatusCallback mStatusCallback;
    BackgroundTask mBackgroundTask;
    boolean isTaskExecuting = false;

    /**
     * Called when a fragment is first attached to its activity.
     * onCreate(Bundle) will be called after this.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStatusCallback = (TaskStatusCallback)activity;
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        fileList = new ArrayList<File>();
        allFileList = new ArrayList<File>();

    }

    /**
     * Called when the fragment is no longer attached to its activity. This is called after onDestroy().
     */
    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
        mStatusCallback = null;
    }

    private class BackgroundTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if(mStatusCallback != null)
                mStatusCallback.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            traverse(sdcard);
            largestFiles();
            averageFileSize();
            publishProgress(progress);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mStatusCallback != null)
                mStatusCallback.onPostExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(mStatusCallback != null)
                mStatusCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onCancelled(Void result) {
            if(mStatusCallback != null)
                mStatusCallback.onCancelled();
        }

    }

    public void startBackgroundTask() {
        if(!isTaskExecuting){
            mBackgroundTask = new BackgroundTask();
            mBackgroundTask.execute();
            isTaskExecuting = true;
        }
    }

    public void cancelBackgroundTask() {
        if(isTaskExecuting){
            mBackgroundTask.cancel(true);
            isTaskExecuting = false;
        }
    }

    public void updateExecutingStatus(boolean isExecuting){
        this.isTaskExecuting = isExecuting;
    }



    public void traverse (File dir) {


        if (dir.exists()) {
            File[] files = dir.listFiles();
            progress=10;
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if(i==files.length/2){
                    progress=50;
                }
                else if(i==files.length/3){
                    progress=75;
                }


                if (file.isDirectory()) {
                    traverse(file);
                } else {
                    fileList.add(file);
                    allFileList.add(file);
                }
            }
            progress=100;
        }
    }

    public void averageFileSize(){
        long length=0;
        for(int i=0;i<fileList.size();i++){
            length+=fileList.get(i).length();
        }
        avgFileSize= length/fileList.size();

    }
    public void largestFiles(){
        Collections.sort(fileList, new FileSizeComparator());
       fileList.subList(10, fileList.size()).clear();
    }
    public static ArrayList<File> getAllFiles(){
        return allFileList;
    }

    public static long getAverage(){
        return avgFileSize;
    }

    public static ArrayList<File> getLargestFiles(){
        return fileList;
    }
}


class FileSizeComparator implements Comparator<File> {
    public int compare(File a, File b) {
        long aSize = a.length();
        long bSize = b.length();
        if (aSize == bSize) {
            return 0;
        } else {
            return Long.compare(bSize, aSize);
        }
    }


}