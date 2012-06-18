package com.tom.newsbuddy;

import java.io.IOException;
import java.util.ArrayList;

import com.tom.newsbuddy.classes.Groups;
import com.tom.newsbuddy.classes.Server;
import com.tom.newsbuddy.handlers.DatabaseHandler;
import com.tom.newsbuddy.models.Newsreader;

import com.tom.newsbuddy.R;
import com.tom.newsbuddy.ReadNewsgroup;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowGroups extends ListActivity {
	static final private int BACK_ID = Menu.FIRST;
	static final private int CLEAR_ID = Menu.FIRST + 1;
	static final private int SHOW_ID = Menu.FIRST + 2;
	
	private TextView progressOutput;
	private TextView mAddress;
	private TextView mPort;
	private Server mServer;
	
	
	ArrayList<Groups> newsgroups = null;
	private NewsgroupsAdapter m_adapter = null;
	private Dialog m_ProgressDialog = null;
	private Newsreader newsclass = null;
	private DatabaseHandler db = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.newsreader_activity);
		db = new DatabaseHandler(this);
		
		((Button) findViewById(R.id.btn_refresh))
				.setOnClickListener(mConnectListener);
		m_ProgressDialog = new Dialog(ShowGroups.this);
		m_ProgressDialog.setContentView(R.layout.progress_dialog);
		progressOutput = (TextView) m_ProgressDialog.findViewById(R.id.message);

		m_ProgressDialog.setTitle("Behind the Scenes...");
		mAddress = (TextView) findViewById(R.id.textview_address);
		mPort = (TextView) findViewById(R.id.textview_port);
		
		Bundle extras = getIntent().getExtras();
		mAddress.setText(extras.getString("address"));
		mPort.setText(extras.getString("port"));
		mServer = db.getServer(extras.getInt("serverid"));
		
		newsclass = new Newsreader(mServer.getAddress(),
				mServer.getPort(), progressOutput);

		newsgroups = new ArrayList<Groups>();
		newsgroups = (ArrayList<Groups>)getLastNonConfigurationInstance();
		if(newsgroups == null) {
			newsgroups = new ArrayList<Groups>();
		}

		this.m_adapter = new NewsgroupsAdapter(this, R.layout.row, newsgroups);
		setListAdapter(this.m_adapter);

		if(db.getGroupsCount(mServer.getId()) == 0) {
			Thread thread = new Thread(null, viewNewsgroups,
					"MagentoBackground");
			thread.start();
			m_ProgressDialog.show();
			m_ProgressDialog.setCancelable(false);
		} else {
			try {
				newsgroups = db.getAllGroups(mServer.getId());
			} catch (Exception e) {
				appendToOutput(e.getMessage());
			}
			runOnUiThread(returnRes);
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    return this.m_adapter.items;
	}

	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}	/**
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
		menu.add(0, SHOW_ID, 0, R.string.show).setShortcut('2', 'd');

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

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (newsgroups != null && newsgroups.size() > 0) {
				m_adapter.notifyDataSetChanged();
				m_adapter.clear();
				for (int i = 0; i < newsgroups.size(); i++) {
					m_adapter.add(newsgroups.get(i));
				}
			}
			m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
		}
	};
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Groups item = (Groups) getListAdapter().getItem(position);
		Toast.makeText(this, item.getName() + " selected", Toast.LENGTH_LONG).show();
		Intent readNewsgroup = new Intent(this, ReadNewsgroup.class);

		db.addGroupIfItsNotExistent(item);
		try {
			Groups group = db.getGroupByName(item.getName(), mServer.getId());
			readNewsgroup.putExtra("groupsid", group.getId());
			readNewsgroup.putExtra("serverid", mServer.getId());
			item.setServer_fkid(mServer.getId());
			startActivity(readNewsgroup);
		} catch (Exception e) {
			Toast.makeText(this, "Write issues encountered! Something went wrong.", Toast.LENGTH_LONG).show();
		}
		

	}

	void appendToOutput(String str) {
		m_ProgressDialog.show();
		progressOutput.append(str + "\r\n");
		String text = null;

		text = progressOutput.getText().toString();
		text = str + "\r\n" + text;
		progressOutput.setText(text);
	}

	private Runnable viewNewsgroups = new Runnable() {
		@Override
		public void run() {
			if (newsclass.connectSocket() == true) {
				 newsgroups.clear();
				 try {
					newsgroups = newsclass.listNewsgroups(mServer);
				} catch (IOException e) {
					appendToOutput(e.getMessage());
				}
				 runOnUiThread(returnRes);
			}
		}
	};
	/**
	 * A call-back for when the user presses the back button.
	 */
	OnClickListener mConnectListener = new OnClickListener() {
		public void onClick(View v) {
			Thread thread = new Thread(null, viewNewsgroups,
					"MagentoBackground");
			thread.start();
			m_ProgressDialog.show();
			m_ProgressDialog.setCancelable(false);
		}
	};

	private class NewsgroupsAdapter extends ArrayAdapter<Groups> {

		private ArrayList<Groups> items;

		public NewsgroupsAdapter(Context context, int textViewResourceId,
				ArrayList<Groups> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row, null);
			}
			Groups group = items.get(position);
			if (group != null) {
				TextView ng = (TextView) v.findViewById(R.id.newsgroupname);
				if (ng != null) {
					ng.setText(group.getName());
				}
				
			}
			return v;
		}
	}
}
