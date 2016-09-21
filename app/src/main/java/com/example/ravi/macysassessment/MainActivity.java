package com.example.ravi.macysassessment;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements AsyncFrag.TaskStatusCallback,
        View.OnClickListener {

    private AsyncFrag mFragment;
    private ProgressBar mProgressBar;
    private TextView mProgressvalue;
    private ListView largestFilesListView;
    private ArrayList<File> largestFilesArrList;
    private ArrayList<String> fileExtensions = new ArrayList<String>();
    private StringBuilder builder = new StringBuilder();
    private TextView avgFileSize;
    private TextView fileExtFreq;

    private Button shareButton;
    private Button start;

    private String emailText;
    private Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        largestFilesListView= (ListView)findViewById(R.id.listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressvalue = (TextView) findViewById(R.id.progressValue);
        emailText="";
        fileExtFreq=(TextView) findViewById(R.id.textView2);
        avgFileSize = (TextView) findViewById(R.id.avgFileSize);
        shareButton = (Button) findViewById(R.id.button);
        start = (Button) findViewById(R.id.start);



        if (savedInstanceState != null) {
            int progress = savedInstanceState.getInt("progress_value");
            mProgressvalue.setText(progress + "%");
            mProgressBar.setProgress(progress);
        }

        FragmentManager mMgr = getFragmentManager();
        mFragment = (AsyncFrag) mMgr
                .findFragmentByTag(AsyncFrag.TAG_HEADLESS_FRAGMENT);

        if (mFragment == null) {
            mFragment = new AsyncFrag();
            mMgr.beginTransaction()
                    .add(mFragment, AsyncFrag.TAG_HEADLESS_FRAGMENT)
                    .commit();
        }
    }
private void getFreq(){
fileExtFreq.append("\n");
   for(int i=0;i<AsyncFrag.getAllFiles().size();i++){
      fileExtensions.add(getFileExtension(AsyncFrag.getAllFiles().get(i).getName()));
   }
    Set<String> unique = new HashSet<String>(fileExtensions);
    for (String key : unique) {
        builder.append(key + ": " + Collections.frequency(fileExtensions, key)+"\n\n");
        fileExtFreq.append(key + ": " + Collections.frequency(fileExtensions, key)+"\n\n");
    }
}
    private String getFileExtension(String file){
        int dot = 0;
        for(int i = 0;i<file.length()-1;i++){
            if(file.charAt(file.length()-(1+i)) == '.'){
                dot = i;
                break;
            }
        }
        return file.substring(file.length()-(dot+1),file.length());
    }

    public void makeEmailIntent(){
        emailText=getMyStringMessage(largestFilesArrList);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,emailText);

        shareButton.setEnabled(true);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));

            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("progress_value", mProgressBar.getProgress());
    }


    // Background task Callbacks

    @Override
    public void onPreExecute() {
        Toast.makeText(getApplicationContext(), "onPreExecute",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostExecute() {
        Toast.makeText(getApplicationContext(), "onPostExecute",
                Toast.LENGTH_SHORT).show();
        shareButton.setEnabled(false);
        start.setEnabled(false);
        avgFileSize.append(AsyncFrag.getAverage()+" bytes");
        largestFilesArrList=AsyncFrag.getLargestFiles();
        final CustomListView customArrayAdapter = new CustomListView(this, R.layout.custom_list_view,largestFilesArrList);



        largestFilesListView.setAdapter(customArrayAdapter);
        Parcelable state=largestFilesListView.onSaveInstanceState();
        largestFilesListView.onRestoreInstanceState(state);

        if (mFragment != null){
            mFragment.updateExecutingStatus(false);}
        makeEmailIntent();

    }

    @Override
    public void onCancelled() {
        Toast.makeText(getApplicationContext(), "onCancelled",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProgressUpdate(int progress) {
        mProgressvalue.setText(progress + "%");
        mProgressBar.setProgress(progress);
    }

    /**
     * Called when a view has been clicked
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.start:
                if (mFragment != null)
                    mFragment.startBackgroundTask();

                break;
            case R.id.cancel:
                if (mFragment != null)
                    mFragment.cancelBackgroundTask();

                break;


        }
    }
    public String getMyStringMessage(ArrayList<File> files){
        getFreq();
        builder.append("Average File Size: "+AsyncFrag.getAverage()+" bytes"+"\n\n\n");
        for(int i=0;i<files.size();i++) {
            builder.append(files.get(i).getAbsoluteFile()+" Size: "+files.get(i).length()+"\n");
        }
        return builder.toString();
    }
}
 class CustomListView extends ArrayAdapter<File>{
     private  final ArrayList<File> fileList;

     private final Activity context;

     public CustomListView(Activity context, int resource, ArrayList<File> objects) {
         super(context, R.layout.custom_list_view, objects);
         this.context = context;
         fileList=objects;

     }
     public View getView(int position,View view,ViewGroup parent){
         LayoutInflater inflater = context.getLayoutInflater();
         View rowView =  inflater.inflate(R.layout.custom_list_view, null, true);
         TextView fileName = (TextView) rowView.findViewById(R.id.fileName);
         TextView fileSize = (TextView) rowView.findViewById(R.id.size);
         fileName.setText(fileList.get(position).getName());
         fileSize.setText(Double.toString(fileList.get(position).length()));

         return rowView;
     }

 }
