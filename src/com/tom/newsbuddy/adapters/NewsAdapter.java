package com.tom.newsbuddy.adapters;

import java.util.ArrayList;
import java.util.Collections;
import com.tom.newsbuddy.R;
import com.tom.newsbuddy.classes.Articles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter {

	private ArrayList<Articles> items;
	private Context context;
	public NewsAdapter(Context context, ArrayList<Articles> listarticles) {
		super();
		this.items = buildArrayList(listarticles);

		this.context = context;
	}

	private ArrayList<Articles> buildArrayList(ArrayList<Articles> articles) {
		ArrayList<Articles> flat_list = new ArrayList<Articles>();
		Collections.sort(articles);
		for(int i = 0; i < articles.size(); i++) {
			Articles tmp = articles.get(i);
			flat_list.add(tmp);
			if(articles.get(i).getReply() != null && articles.get(i).getReply().size() > 0) {
				ArrayList<Articles> childs = new ArrayList<Articles>(articles.get(i).getReply().values());
				flat_list.addAll(buildArrayList(childs));
			}
		}
		return flat_list;
	}

	public void setListOfArticles(ArrayList<Articles> listarticles) {
		
		this.items = buildArrayList(listarticles);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = LayoutInflater.from(context);
			v = vi.inflate(R.layout.newsgroups_row, null);
		}
		Articles o = items.get(position);
		
		if (o != null) {
			TextView subject = (TextView) v.findViewById(R.id.subject);
			TextView from = (TextView) v.findViewById(R.id.from);

			if (subject != null) {
				LinearLayout.LayoutParams myParams = (LayoutParams) subject.getLayoutParams();
				myParams.leftMargin = 10*o.getLevel();

				LinearLayout.LayoutParams myParams_from = (LayoutParams) from.getLayoutParams();
				myParams_from.leftMargin = 10*o.getLevel();
				subject.setLayoutParams(myParams);
				subject.setText(o.getSubject());
				from.setLayoutParams(myParams_from);
				from.setText(o.getFrom());
				if(o.getDate() != null) {
					from.setText(o.getDate().toLocaleString() + " " +o.getFromShort());
				}
			}
		}
		return v;
	}

	@Override
	public int getCount() {
		
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public Context getContext() {
		return this.context;
	}
}


