package com.tom.newsbuddy.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import java.util.StringTokenizer;

import javax.mail.internet.MimeUtility;





import com.tom.newsbuddy.adapters.NewsAdapter;
import com.tom.newsbuddy.classes.Articles;
import com.tom.newsbuddy.classes.Groups;
import com.tom.newsbuddy.classes.Server;
import com.tom.newsbuddy.handlers.DatabaseHandler;

import android.os.Handler;
import android.widget.TextView;

public class Newsreader implements Runnable {
	private Socket echoSocket = null;
	private BufferedWriter out = null;
	private BufferedReader in = null;
	private String buffer = null;
	private String address = null;
	private String port = null;
	private ArrayList<Groups> newsgroups = null;
	private TextView output = null;
	private String outputmessages = null;
	private Handler handler = null;
	private static final int treshold = 50;
	
	HashMap<String, Articles> articles;
	private NewsAdapter adapter = null;
	
	public Newsreader(String address, String port, TextView output) {
		this.port = port;
		this.address = address;
		this.output = output;
		this.outputmessages = "Called Newslistener...\r\n";
		handler = new Handler();
	}

	protected void finalize() {
		if (echoSocket != null && echoSocket.isConnected()) {
			try {
				echoSocket.close();
			} catch (IOException e) {
				appendToOutput(e.getMessage());
			}
		}
	}

	void appendToOutput(String str) {
		outputmessages = str + "\r\n" + outputmessages;
		handler.removeCallbacks(bla);
		handler.post(bla);
	}

	Runnable bla = new Runnable() {
		public void run() {

			int to = 550;
			String addon = outputmessages;
			if (outputmessages.length() < to) {
				addon += output.getText().toString();
			}

			if (addon.length() < to) {
				to = addon.length();
			}
			output.setText(addon.substring(0, to));

			outputmessages = "";
		}
	};
	
	void updateNewsgroupsList() {
		handler.removeCallbacks(updatenewslist);
		handler.post(updatenewslist);
	}
	Runnable updatenewslist = new Runnable() {
		public void run() {
			ArrayList<Articles> listarticles = new ArrayList<Articles>(articles.values());
			adapter.setListOfArticles(listarticles);
			adapter.notifyDataSetChanged();
		}
	};
	
	public boolean connectSocket() {
		appendToOutput("Connecting...");
		if (this.echoSocket == null) {
			try {
				echoSocket = new Socket(this.address,
						Integer.parseInt(this.port));
				out = new BufferedWriter(new OutputStreamWriter(
						echoSocket.getOutputStream()));

				Charset charset = Charset.forName("ISO-8859-1");
				CharsetDecoder decoder = charset.newDecoder();
				in = new BufferedReader(new InputStreamReader(
						echoSocket.getInputStream(),decoder));
				if ((buffer = in.readLine()).charAt(0) == '2') {
					appendToOutput(buffer);
				}

			} catch (UnknownHostException e) {
				appendToOutput("Don't know about host: " + this.address);
				return false;

			} catch (IOException e) {
				appendToOutput("Couldn't get I/O for " + "the connection to: "
						+ this.address);

				return false;
			}
		} else {
			appendToOutput("Socket already connected :) ");
		}
		return true;
	}

	void sendCommand(String cmd) throws IOException, Exception {
		appendToOutput("Sending command \"" + cmd + "\"...");
		out.write(cmd + "\015\012");
		out.flush();
		if ((buffer = in.readLine()) == null)
			throw new IOException();
		else {
			if (buffer.length() < 1)
				throw new IOException();

			if (buffer.charAt(0) != '2')
				throw new Exception("Command \"" + cmd + "\" failed.");
		}
	}

	String getTextResponse() throws IOException {
		String text = "";

		while (true) {
			buffer = in.readLine();

			if (buffer == null)
				throw new IOException("End of text not expected.");

			if (buffer.length() < 1)
				continue;

			buffer += "\n";

			// check for end of text
			if (buffer.charAt(0) == '.') {
				if (buffer.charAt(1) != '.')
					break; // no doubt, text ended

				// we've got a doubled period here
				// collapse the doubled period to a single one
				buffer = buffer.substring(1, buffer.length());
			}

			appendToOutput(buffer);
			text += buffer;
		}

		return text;
	}

	
	public ArrayList<Groups> listNewsgroups(Server mServer) throws IOException {

		try {
			sendCommand("LIST");
		} catch (Throwable e) {
			return new ArrayList<Groups>();
		}

		newsgroups = new ArrayList<Groups>();
		// get names of all newsgroups
		while (true) {
			buffer = in.readLine();

			if (buffer == null) {
				throw new IOException("End of text not expected.");

			}

			if (buffer.length() < 1)
				continue;

			if (buffer.charAt(0) == '.') // check for end of listing
				break;

			StringTokenizer st = new StringTokenizer(buffer, " ");
			String token = st.nextToken();
			appendToOutput(token);
			Groups group =  new Groups();
			group.setName(token.toString());
			group.setHigh(st.nextToken().toString());
			group.setLow(st.nextToken().toString());
			group.setPermission(st.nextToken().toString());
			group.setServer_fkid(mServer.getId());
			newsgroups.add(group);
		}
		return newsgroups;
	}

	public String getStatus() {

		return outputmessages;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public void initializeNewsgroupsList(Groups group, NewsAdapter colorExpListAdapter, DatabaseHandler db) {
		this.adapter = colorExpListAdapter;
		articles = new HashMap<String, Articles>();
		if(db.getArticlesCount(group.getId()) > 0) {
			try {
				ArrayList<Articles> db_articles = db.getAllArticles(group.getId(), "date");
				for(int i = 0; i<db_articles.size(); i++) {
					List<String> keys = new ArrayList<String>();
					String referencemap = db_articles.get(i).getReferenceMap();
					if(referencemap != null) {
						StringTokenizer st1 = new StringTokenizer(db_articles.get(i).getReferenceMap(), " ");
						while(st1.hasMoreTokens()) {
							keys.add(st1.nextToken());
						}
					}
					articles = recursiveParseArticles(articles, db_articles.get(i), keys, 0);
					updateNewsgroupsList();
				}
			} catch (Exception e) {
				appendToOutput(e.getMessage());
			}
		}
	}
	
	public HashMap<String, Articles> synchronizedNewsgroup(Groups group, NewsAdapter colorExpListAdapter, DatabaseHandler db)
			throws IOException {
		this.adapter = colorExpListAdapter;

		Locale loc = new Locale("en");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", loc);
		articles = new HashMap<String, Articles>();
		try {
			sendCommand("GROUP " + group.getName());
		} catch (IOException e) {
			appendToOutput("Error: could not access newsgroup \"" + group.getName()
					+ "\".");
			return articles;
		} catch (Exception e) {
			appendToOutput(e.getMessage());
			return articles;
		}

		StringTokenizer st = new StringTokenizer(buffer, " ");
		st.nextToken();
		st.nextToken();
		int firstArticleNumber = (new Integer(st.nextToken())).intValue(), lastArticleNumber = (new Integer(
				st.nextToken())).intValue();
		appendToOutput("First article #" + firstArticleNumber);
		appendToOutput("Last article #" + lastArticleNumber);
		int numberOfArticles = lastArticleNumber - firstArticleNumber;
		
		appendToOutput("Articles in Group:" + numberOfArticles);
		int lastArticleInDb = db.getLastArticleId(group.getId());
		int newArticles = lastArticleNumber - lastArticleInDb;
		appendToOutput("New Articles:" + newArticles);
		this.initializeNewsgroupsList(group, colorExpListAdapter, db);
		
		//point now to the right article or return if there are no new articles...
		try {
			if(db.getArticlesCount(group.getId()) > 0 && lastArticleInDb != 0 && newArticles <= 0) {
				return articles;
			} else if(db.getArticlesCount(group.getId()) > 0 && lastArticleInDb != 0 && newArticles > 0){
				if(newArticles > treshold) {
					appendToOutput("To many articles, loading "+String.valueOf(treshold));
					lastArticleInDb = lastArticleNumber - treshold;
				}
				sendCommand("head "+String.valueOf(lastArticleInDb));
				while(true) {
					buffer = in.readLine();
					if(buffer.charAt(0) == '.') {
						break;
					}
				}
				sendCommand("next");
			} else if(numberOfArticles > treshold) {

				appendToOutput("To many articles, loading "+String.valueOf(treshold));
				sendCommand("head "+String.valueOf(lastArticleNumber - treshold));
				while(true) {
					buffer = in.readLine();
					if(buffer.charAt(0) == '.') {
						break;
					}
				}
				sendCommand("next");
			}
		} catch (Exception e) {
			appendToOutput(e.getMessage());
		}
		while (true) {
			try {
				sendCommand("head");
				Articles article = new Articles();
				List<String> keys = new ArrayList<String>();
				
				while (true) {
					
					
					if (buffer == null) {
						throw new IOException("End of text not expected.");

					}

					if (buffer.length() < 1)
						continue;

					if (buffer.charAt(0) == '.') // check for end of listing
						break;

					buffer = decodeHeaderField(buffer);
					StringTokenizer st1 = new StringTokenizer(buffer, " ");
					String token = st1.nextToken();
					
					if (token.toLowerCase().contains("subject:")) {
						String subject = "";
						while(st1.hasMoreTokens()) {
							subject += st1.nextToken() + " ";
						}
						article.setSubject(subject);

						appendToOutput("Subject: " + subject);
					} else if (token.toLowerCase().contains("message-id:")) {
						String id = st1.nextToken();
						article.setMessageId(id);
						appendToOutput("ID: "+id);
					} else if (token.toLowerCase().contains("from:")) {
						String from = st1.nextToken();
						while(st1.hasMoreTokens()) {
							from += " "+st1.nextToken();
						}
						article.setFrom(from);
						appendToOutput("From: "+from);
					} else if (token.toLowerCase().contains("date") && !token.toLowerCase().contains("nntp")) {
						String date = st1.nextToken();
						while(st1.hasMoreTokens()) {
							date += " "+st1.nextToken();
						}
						Date d = sdf.parse(date);
						article.setDate(d);
						appendToOutput("Date: "+d.toLocaleString());
					} else if (token.toLowerCase().contains("221")) {
						String articleid = st1.nextToken();
						article.setArticleNumber(Integer.parseInt(articleid));
						appendToOutput("Article-ID: "+articleid);
					} else if (token.toLowerCase().contains("reference")) {
						String referencemap = "";
						while(st1.hasMoreTokens()) {
							String key = st1.nextToken();
							
							if(referencemap.length() != 0) {
								referencemap += " ";
							}
							referencemap += key;
							keys.add(key);
						}
						article.setReferenceMap(referencemap);
					}

					buffer = in.readLine();

				}
				//articles.put(article.getId(), article);
				article.setLevel(keys.size());
				article.setGroups_fkid(group.getId());
				db.addArticleIfItsNotExistent(article);
				this.initializeNewsgroupsList(group, colorExpListAdapter, db);

			} catch (Throwable e) {
				appendToOutput(e.getMessage());
				// there's been a problem with the current article, let's try to
				// get the next one
			}

			try {
				sendCommand("next");
			} catch (Exception e) {
				// "next" command failed
				// looks like there are no more articles in the current
				// newsgroup
				appendToOutput(e.getMessage());
				return articles;
			}
		}
	}
	
	private String getCharsetFromMessageID(String message_id) throws IOException{
		String charset = "ISO-8859-1";
		try {
			sendCommand("head " + message_id);
		} catch (IOException e) {
			appendToOutput("Error: could not access newsgroup \"" + message_id
					+ "\".");
			return charset;
		} catch (Exception e) {
			appendToOutput(e.getMessage());
			return charset;
		}
		
		while (true) {
			buffer = in.readLine();
			
			if (buffer == null) {
				throw new IOException("End of text not expected.");

			}

			if (buffer.length() < 1)
				continue;
			
			if (buffer.charAt(0) == '.') // check for end of listing
				break;
			
			if(buffer.toLowerCase().contains("charset=")) {
				appendToOutput(buffer);
				if(buffer.toLowerCase().contains("utf")) {
					charset = "UTF-8";
				} else {
					if (buffer.toLowerCase().contains(";")) {
						try {
							charset = buffer.substring(buffer.toLowerCase()
									.indexOf("charset=") + 8, buffer.indexOf(
									";",
									buffer.toLowerCase().indexOf("charset=")));
						} catch (StringIndexOutOfBoundsException e) {
							appendToOutput(e.getMessage());
						}
					}
				}
			}
		}
		
		return charset;
	}
	 /**
     * Convert a {@link Reader} instance to a String
     * @param reader The Reader instance
     * @return String
     */
    public static String readerToString(Reader reader) {
        String temp = null;
        BufferedReader bufReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        try {
            temp = bufReader.readLine();
            while (temp != null) {
                sb.append(temp);
                sb.append("\n");
                temp = bufReader.readLine();
            }
        } catch (IOException e) {
            return "";
        }
 
        return sb.toString();
    }
    
	public Articles updateBody(Articles article) throws IOException{
		String message_id = article.getMessageId();
		StringBuilder body = new StringBuilder();
		String charset = getCharsetFromMessageID(message_id);
		
		appendToOutput("Updating Body for ID: "+message_id);
		Charset charset1 = Charset.forName(charset);
		CharsetDecoder decoder = charset1.newDecoder();
		in = new BufferedReader(new InputStreamReader(
				echoSocket.getInputStream(),decoder));
		try {
			sendCommand("body " + message_id);
		} catch (IOException e) {
			appendToOutput("Error: could not access newsgroup \"" + message_id
					+ "\".");
			return article;
		} catch (Exception e) {
			appendToOutput(e.getMessage());
			return article;
		}
		
		appendToOutput("Charset = " + charset);

		

		while (true) {
			buffer = in.readLine();
			
			if (buffer == null) {
				throw new IOException("End of text not expected.");

			}

			if (buffer.length() < 1)
				continue;
			
			if (buffer.charAt(0) == '.') // check for end of listing
				break;
			//appendToOutput(new String(buffer.getBytes(),charset)+"\r\n");
			body.append(buffer+"\r\n");
		}
		article.setContent(body.toString());

		charset1 = Charset.forName("ISO-8859-1");
		decoder = charset1.newDecoder();
		in = new BufferedReader(new InputStreamReader(
				echoSocket.getInputStream(),decoder));
		return article;
	}
	
	private String decodeHeaderField(String input) {
		String output;
		if(input.toLowerCase().contains("=?")) {
			try {
				output = MimeUtility.decodeText(input);
			} catch (UnsupportedEncodingException e) {
				output = input;
			}
		} else {
			output = input;
		}
		return output;
	}
	
	private HashMap<String, Articles> recursiveParseArticles(HashMap<String, Articles> articles, Articles article, List<String> keys, int level) {
		if(keys.size() != 0) {
			String key = keys.get(0);
			Articles tmp = null;
			HashMap<String, Articles> articles_tmp = null;
			if(!articles.containsKey(key)) {
				tmp = new Articles();
				tmp.setSubject("Loading...");
				tmp.setId(0);
				tmp.setSortOrder(article.getSortOrder());
				tmp.setMessageId(key);
				tmp.setLevel(level);
				//tmp.setDate(article.getDate());
				tmp.setReply(new HashMap<String, Articles>());
				articles.put(key, tmp);
			}
			tmp = articles.get(key);
			if(tmp.getReply() == null) {
				articles_tmp = new HashMap<String, Articles>();
			} else {
				articles_tmp = tmp.getReply();
			}
			List<String> tmp_keys = keys.subList(1, keys.size());
			tmp.setReply(recursiveParseArticles(articles_tmp, article, tmp_keys, level));
			articles.put(key, tmp);
			return articles;
		} else {
			if(articles.containsKey(article.getMessageId())) {
				article.setReply(articles.get(article.getMessageId()).getReply());
			}
			articles.put(article.getMessageId(), article);
			return articles;
		}
	}

	
}

