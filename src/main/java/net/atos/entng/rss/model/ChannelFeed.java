package net.atos.entng.rss.model;

import io.vertx.core.json.JsonObject;
import net.atos.entng.rss.constants.Field;
import net.atos.entng.rss.helpers.IModelHelper;

public class ChannelFeed implements IModel<ChannelFeed> {
    private String title;
    private String link;
    private Integer show;

    public ChannelFeed(JsonObject channelFeed) {
        this.title = channelFeed.getString(Field.TITLE, null);
        this.link = channelFeed.getString(Field.LINK, null);
        this.show = channelFeed.getInteger(Field.SHOW, null);
    }

    public String getTitle() {
        return title;
    }

    public ChannelFeed setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public ChannelFeed setLink(String link) {
        this.link = link;
        return this;
    }

    public Integer getShow() {
        return show;
    }

    public ChannelFeed setShow(Integer show) {
        this.show = show;
        return this;
    }

    @Override
    public JsonObject toJson() {
        return IModelHelper.toJson(this, false, false);
    }
}
