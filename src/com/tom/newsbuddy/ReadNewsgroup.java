package com.tom.newsbuddy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import com.tom.newsbuddy.handlers.DatabaseHandler;
import com.tom.newsbuddy.models.Newsreader;
import com.tom.newsbuddy.adapters.NewsAdapter;
import com.tom.newsbuddy.classes.Articles;
import com.tom.newsbuddy.classes.Groups;
import com.tom.newsbuddy.classes.Server;


/**
 * Demonstrates expandable lists backed by a Simple Map-based adapter
 */
public class ReadNewsgroup extends Activity {

	static final private int BACK_ID = Menu.FIRST;
	static final private int CLEAR_ID = Menu.FIRST + 1;
	static final private int SHOW_ID = Menu.FIRST + 2;
	private Dialog m_ProgressDialog = null;
	private Newsreader newsclass = null;

	private TextView progressOutput;
	private Server mServer;
	private Groups mGroup;
	private HashMap<String, Articles> listofartices = null;

	private ArrayList<Articles> articles = null;
	private ListView list;
	private NewsAdapter newsAdapter;

	private DatabaseHandler db = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DatabaseHandler(this);
		articles = new ArrayList<Articles>();
		setContentView(R.layout.newsgroups_activity);
		list = (ListView) findViewById(R.id.newsgroups_list);
		articles = new ArrayList<Articles>();
		newsAdapter = new NewsAdapter(this, articles);
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		try {
			mGroup = db.getGroup(extras.getInt("groupsid"));
		} catch (Exception e1) {
			Toast.makeText(this, "This Newsgroup is not longer existing", Toast.LENGTH_LONG).show();
			db.deleteGroup(mGroup);
			this.finish();
		}
		mServer = db.getServer(extras.getInt("serverid"));
		list.setAdapter(newsAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				Articles art = (Articles) newsAdapter.getItem(position);
				Toast.makeText(getApplicationContext(), art.getSubject() + " selected", Toast.LENGTH_SHORT).show();
				Intent readArticle = new Intent(getApplicationContext(), ReadArticle.class);
				readArticle.putExtra("articlesid", art.getId());
				readArticle.putExtra("serverid", mServer.getId());
				readArticle.putExtra("groupsid", mGroup.getId());
				startActivity(readArticle);
			}
		});
		
		

		m_ProgressDialog = new Dialog(ReadNewsgroup.this);
		m_ProgressDialog.setContentView(R.layout.progress_dialog);
		progressOutput = (TextView) m_ProgressDialog.findViewById(R.id.message);

		m_ProgressDialog.setTitle("Behind the Scenes...");
		newsclass = new Newsreader(mServer.getAddress(), mServer.getPort(), progressOutput);

		
		 Thread thread = new Thread(null, viewNewsgroups, "MagentoBackground");
		 thread.start();
		 m_ProgressDialog.show();
		 //m_ProgressDialog.setCancelable(false);

	}

	private Runnable viewNewsgroups = new Runnable() {
		@Override
		public void run() {
			newsclass.initializeNewsgroupsList(mGroup, newsAdapter, db);
			if (newsclass.connectSocket() == true) {
				try {
					listofartices = newsclass.synchronizedNewsgroup(mGroup,newsAdapter, db);
					
				} catch (IOException e) {
					appendToOutput(mGroup.getName() + " encountered a Problem");
				}
				runOnUiThread(returnRes);
			}
		}
	};
	
	void appendToOutput(String str) {
		m_ProgressDialog.show();
		progressOutput.append(str + "\r\n");
		String text = null;

		text = progressOutput.getText().toString();
		text = str + "\r\n" + text;
		progressOutput.setText(text);
	}
	
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if(listofartices != null && listofartices.size() > 0) {
				updateView();
			}
			m_ProgressDialog.dismiss();
		}
	};
	
	public void updateView() {
		articles = new ArrayList<Articles>(listofartices.values());
		newsAdapter.setListOfArticles(articles);
		newsAdapter.notifyDataSetChanged();
	}
	/**
	 * Called when your activity's options menu needs to be created.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
		menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');
		menu.add(0, SHOW_ID, 0, R.string.show).setShortcut('1', 'c');

		return true;
	}

	/**
	 * Called right before your activity's option menu is displayed.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.
		menu.findItem(CLEAR_ID).setVisible(
				progressOutput.getText().length() > 0);

		return true;
	}

	/**
	 * Called when a menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case BACK_ID:
			finish();
			return true;
		case CLEAR_ID:
			progressOutput.setText("");
			return true;
		case SHOW_ID:
			m_ProgressDialog.show();
			m_ProgressDialog.setCancelable(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	
}