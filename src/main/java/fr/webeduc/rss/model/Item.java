package fr.webeduc.rss.model;

import org.vertx.java.core.json.JsonObject;

public class Item {
	private String title;
	private String link;
	private String description;
	private String pubDate;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public JsonObject toJson(){
		JsonObject result = new JsonObject();
		result.putString("title", title);
		result.putString("link", link);
		result.putString("description", description);
		result.putString("pubDate", pubDate);
		return result;
	}
}
