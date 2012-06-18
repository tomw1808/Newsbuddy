package com.tom.newsbuddy;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.tom.newsbuddy.classes.Server;
import com.tom.newsbuddy.handlers.DatabaseHandler;
import com.tom.newsbuddy.R;

public class NewsBuddyActivity extends ListActivity {
	static final private int BACK_ID = Menu.FIRST;
	static final private int CLEAR_ID = Menu.FIRST + 1;
	static final private int SHOW_ID = Menu.FIRST + 2;
	
	ArrayList<Server> servers = null;
	private Runnable viewServers;
	private Runnable addServer;
	private Dialog m_ProgressDialog = null;
	private EditText mAddress;
	private EditText mPort;
	private TextView progressOutput;
	private ServersAdapter m_adapter;
	DatabaseHandler db = null;
	
	Integer currentTag;
	
	public NewsBuddyActivity() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.newsbuddyactivity);

		((Button) findViewById(R.id.btn_connect))
				.setOnClickListener(mConnectListener);
		mAddress = (EditText) findViewById(R.id.text_address);
		mPort = (EditText) findViewById(R.id.text_port);
		m_ProgressDialog = new Dialog(NewsBuddyActivity.this);
		m_ProgressDialog.setContentView(R.layout.progress_dialog);
		db = new DatabaseHandler(this);
		progressOutput = (TextView) m_ProgressDialog.findViewById(R.id.message);

		m_ProgressDialog.setTitle("Behind the Scenes...");

		
		servers = new ArrayList<Server>();
		servers = (ArrayList<Server>)getLastNonConfigurationInstance();
		if(servers == null) {
			servers = new ArrayList<Server>();
		}

		this.m_adapter = new ServersAdapter(this, R.layout.row, servers);
		setListAdapter(this.m_adapter);
		
		viewServers = new Runnable() {
			@Override
			public void run() {
				servers.clear();
				servers = db.getAllServers();
				runOnUiThread(returnRes);
			}
		};
		Thread thread = new Thread(null, viewServers,
				"MagentoBackground");
		thread.start();
		m_ProgressDialog.show();
		m_ProgressDialog.setCancelable(false);
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
		mAddress.clearFocus();
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

	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (servers != null && servers.size() > 0) {
				m_adapter.notifyDataSetChanged();
				m_adapter.clear();
				for (int i = 0; i < servers.size(); i++)
					m_adapter.add(servers.get(i));
			}
			m_ProgressDialog.dismiss();
			m_adapter.notifyDataSetChanged();
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

	/**
	 * A call-back for when the user presses the back button.
	 */
	OnClickListener mConnectListener = new OnClickListener() {
		public void onClick(View v) {
			addServer = new Runnable() {
				@Override
				public void run() {
					Server svr = new Server();
					svr.setAddress(mAddress.getText().toString());
					svr.setPort(mPort.getText().toString());
					db.addServerIfItsNotExistent(svr);
					servers.clear();
					servers = db.getAllServers();
					runOnUiThread(returnRes);
				}
			};
			Thread thread = new Thread(null, addServer,
					"MagentoBackground");
			thread.start();
			m_ProgressDialog.show();
			m_ProgressDialog.setCancelable(false);
		}
	};
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.context_menu, menu);
	    currentTag = (Integer)v.getTag();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.context_delete_server:
	        	Server o = servers.get(currentTag);
	        	db.deleteServer(o);
	        	m_adapter.remove(o);
	        	m_adapter.notifyDataSetChanged();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	private class ServersAdapter extends ArrayAdapter<Server> {

		private ArrayList<Server> items;

		public ServersAdapter(Context context, int textViewResourceId,
				ArrayList<Server> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.newsbuddyactivity_server_row, null);
			}
			Server o = items.get(position);
			v.setTag(new Integer(position));
			if (o != null) {
				TextView address = (TextView) v.findViewById(R.id.address);
				TextView port = (TextView) v.findViewById(R.id.port);
				
				if (address != null) {
					address.setText(o.getAddress());
				}
				if (port != null) {
					port.setText(o.getPort());
				}
			}
			registerForContextMenu(v);
			v.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Server item = (Server) getListAdapter().getItem((Integer)v.getTag());
					Toast.makeText(NewsBuddyActivity.this, item.getAddress() + " selected", Toast.LENGTH_SHORT).show();
					Intent showgroups = new Intent(NewsBuddyActivity.this, ShowGroups.class);
					showgroups.putExtra("address", item.getAddress());
					showgroups.putExtra("port", item.getPort());
					showgroups.putExtra("serverid", item.getId());
					startActivity(showgroups);
				}
			}
					
					);
			return v;
		}
	}


}