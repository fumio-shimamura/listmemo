package com.github.fumio_shimamura.listmemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ListView　用アダプタ
    SimpleAdapter mAdapter = null;
    // ListView に設定するデータ
    List<Map<String, String>> mList = null;
    // 日時+SPACE(YYYY/MM/DD HH:MM )の文字数は19
    public static final int DATE_LENGTH = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ListView 用アダプタのリストを生成
        mList = new ArrayList<Map<String, String>>();

        // ListView 用アダプタを生成
        mAdapter = new SimpleAdapter(
                this,
                mList,
                android.R.layout.simple_list_item_2,
                new String [] {"title", "content"},
                new int[] {android.R.id.text1, android.R.id.text2}
        );

        // ListView にアダプターをセット
        ListView list = (ListView)findViewById(R.id.listView);
        list.setAdapter(mAdapter);


        // ListView のアイテム選択イベント
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int pos, long id) {

                // 日時＋タイトルから日時を削除する
                String title_tmp = mList.get(pos).get("title");
                String title = removeDateFromTitle(title_tmp);

                // 編集画面に渡すデータをセットし、表示
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("NAME", mList.get(pos).get("filename"));
                //intent.putExtra("TITLE", mList.get(pos).get("title"));
                intent.putExtra("TITLE", title);
                intent.putExtra("CONTENT", mList.get(pos).get("content"));
                startActivity(intent);
            }
        });

        // ListView をコンテキストメニューに登録
        registerForContextMenu(list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // menu_main.xmlでidを定義している
        if (id == R.id.action_add) {
            // 編集画面への遷移処理
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ListView 用アダプタのデータをクリア
        mList.clear();

        // ここでファイルを読み込み、リスト表示処理を行う
        // アプリの保存フォルダ内のファイル一覧を取得
        String savePath = this.getFilesDir().getPath().toString();
        File[] files = new File(savePath).listFiles();
        // ファイル名の降順でソート
        Arrays.sort(files, Collections.reverseOrder());
        // テキストファイル(*.txt)を取得し、ListView用アダプタのリストにセット
        for (int i=0; i<files.length; i++) {
            String fileName = files[i].getName();
            if (files[i].isFile() && fileName.endsWith(".txt")) {
                String title_tmp;
                String title = null;
                String content = null;
                //　ファイルを読み込み
                try {
                    // ファイルオープン
                    InputStream in = this.openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    char[] buf = new char[(int)files[i].length()];
                    // タイトル（1行目）を読み込み
                    //title = reader.readLine();
                    title_tmp = reader.readLine();
                    // ファイル名とタイトルを連結して日時+タイトルを作成する
                    title = makeDateAndTitle(fileName,title_tmp);

                    // 内容（2行目以降）を読み込み
                    int num = reader.read(buf);
                    if (num > 0) {
                        content = new String(buf, 0, num);
                    }
                    // ファイルクローズ
                    reader.close();
                    in.close();
                } catch (Exception e) {
                    Toast.makeText(this, "File read error!", Toast.LENGTH_LONG).show();
                }

                // ListView用のアダプタにデータをセット
                Map<String, String> map = new HashMap<String, String>();
                map.put("filename", fileName);
                map.put("title", title);
                map.put("content", content);
                mList.add(map);
            }
        }
        // ListView のデータ変更を表示に反映
        mAdapter.notifyDataSetChanged();
    }


    // ファイル名とタイトルを連結して日時+タイトルを作成する
    private String makeDateAndTitle(String filename, String title){
        // ファイル名から日付を切り出す
        String file_date = filename.substring(0,8);
        // ファイル名から時刻を切り出す
        String file_time = filename.substring(9,13);
        // YYYY/MM/DD HH:MM + タイトルを返す
        return file_date.substring(0,4) + "-" +
                file_date.substring(4,6) + "-" +
                file_date.substring(6,8) + " " +
                file_time.substring(0,2) + ":" +
                file_time.substring(2,4) + "   " +
                title;
    }

    // 日時＋タイトルから日時を削除する
    private String removeDateFromTitle(String title){
        // 先頭19文字は日時+SPACE(YYYY/MM/DD HH:MM )のため削除する。文字数は19。
        return title.substring(DATE_LENGTH);
    }


    // コンテキストメニュー作成処理
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        getMenuInflater().inflate(R.menu.main_context, menu);
    }

    // コンテキストメニュー選択処理
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch(item.getItemId()) {
            case R.id.context_del:
                // [削除] 選択時の処理
                // ファイル削除
                if (this.deleteFile(mList.get(info.position).get("filename"))) {
                    Toast.makeText(this, R.string.msg_del, Toast.LENGTH_SHORT).show();
                }
                // リストからアイテム処理
                mList.remove(info.position);
                // ListView のデータ変更を表示に反映
                mAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return false;
    }
}
