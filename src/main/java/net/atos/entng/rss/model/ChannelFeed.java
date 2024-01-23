package net.atos.entng.rss.model;

import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;

public class ChannelFeed implements IModel<ChannelFeed> {
    private String title;
    private String link;
    private Integer show;
    public ChannelFeed() {
    }

    public ChannelFeed(JsonObject channelFeed) {
        this.title = channelFeed.getString(Field.TITLE, null);
        this.link = channelFeed.getString(Field.LINK, null);
        this.show = channelFeed.getInteger(Field.SHOW, null);
    }

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

    public Integer getShow() {
        return show;
    }

    public void setShow(Integer show) {
        this.show = show;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
            .put(Field.TITLE, this.title)
            .put(Field.LINK, this.link)
            .put(Field.SHOW, this.show);
    }
}
