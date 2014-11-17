begin transaction
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|getchannel'
set a.name='net.atos.entng.rss.controllers.RssController|getchannel';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|updateChannel'
set a.name='net.atos.entng.rss.controllers.RssController|updateChannel';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|deleteChannel'
set a.name='net.atos.entng.rss.controllers.RssController|deleteChannel';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|view'
set a.name='net.atos.entng.rss.controllers.RssController|view';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|getchannels'
set a.name='net.atos.entng.rss.controllers.RssController|getchannels';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|createchannel'
set a.name='net.atos.entng.rss.controllers.RssController|createchannel';
match (a:Action) where a.name='fr.wseduc.rss.controllers.RssController|getfeedItems'
set a.name='net.atos.entng.rss.controllers.RssController|getfeedItems';
commit