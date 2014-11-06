package fr.webeduc.rss.model;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class Feed {
	private String title;
	private String description;
	private String pubDate;
	private String link;
	private String language;
	private List<Item> items;

	public Feed(){
		this.items = new ArrayList<Item>();
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}

	public void addItem(Item item){
		this.items.add(item);
	}

	public JsonObject toJson(){
		JsonObject result = new JsonObject();
		result.putString("title", title);
		result.putString("link", link);
		result.putString("description", description);
		result.putString("pubDate", pubDate);
		result.putString("language", language);

		JsonArray itemsJson = new JsonArray();
		for(Item i : this.items){
			itemsJson.add(i.toJson());
		}

		result.putArray("Items", itemsJson);

		return result;
	}

}
