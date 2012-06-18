package com.tom.newsbuddy;


import java.io.IOException;

import com.tom.newsbuddy.classes.Articles;
import com.tom.newsbuddy.classes.Groups;
import com.tom.newsbuddy.classes.Server;
import com.tom.newsbuddy.handlers.DatabaseHandler;
import com.tom.newsbuddy.models.Newsreader;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ReadArticle extends Activity {
	
	private Articles mArticle = null;
	private Server mServer = null;
	private Groups mGroup = null;
	
	private Dialog m_ProgressDialog;
	private TextView progressOutput;
	private Newsreader newsclass = null;
	private Runnable getBody = null;
	private DatabaseHandler db = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_article);
		db = new DatabaseHandler(this);
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		try {
			mArticle = db.getArticle(extras.getInt("articlesid"));
		} catch (Exception e2) {
			Toast.makeText(this, "This Article is not existing", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mServer = db.getServer(extras.getInt("serverid"));
		try {
			mGroup = db.getGroup(extras.getInt("groupsid"));
		} catch (Exception e1) {
			Toast.makeText(this, "This Newsgroup is not existing", Toast.LENGTH_LONG).show();
			db.deleteGroup(mGroup);
			finish();
			return;
		}
		m_ProgressDialog = new Dialog(ReadArticle.this);
		m_ProgressDialog.setContentView(R.layout.progress_dialog);
		progressOutput = (TextView) m_ProgressDialog.findViewById(R.id.message);

		m_ProgressDialog.setTitle("Behind the Scenes...");
		newsclass = new Newsreader(mServer.getAddress(), mServer.getPort(), progressOutput);
		getBody = new Runnable() {
			@Override
			public void run() {
				if(mArticle.getContent() == null) {
					if (newsclass.connectSocket() == true) {
						try {
								mArticle = newsclass.updateBody(mArticle);
								db.updateArticle(mArticle);
							
						} catch (IOException e) {
							appendToOutput(e.getMessage());
						}
					}
				}
				runOnUiThread(returnRes);
			}
		};
		 Thread thread = new Thread(null, getBody,
		 "MagentoBackground");
		 thread.start();
		 m_ProgressDialog.show();

	}
	
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
			TextView content = (TextView) findViewById(R.id.newsBody);
			content.setText(mArticle.getContent());

			TextView from = (TextView) findViewById(R.id.articlefrom);
			from.setText(mArticle.getFrom());
			

			TextView subject = (TextView) findViewById(R.id.articlesubject);
			subject.setText(mArticle.getSubject());
			TextView date = (TextView) findViewById(R.id.articledate);
			if(mArticle.getDate() != null) {
				date.setText(mArticle.getDate().toLocaleString());
			}
			m_ProgressDialog.dismiss();
		}
	};
}
