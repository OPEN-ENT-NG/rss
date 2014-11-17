begin transaction
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|createchannel'
set a.name='net.atos.entng.rss.controller.RssController|createchannel';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|deletechannel'
set a.name='net.atos.entng.rss.controller.RssController|deleteChannel';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|getchannel'
set a.name='net.atos.entng.rss.controller.RssController|getchannel';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|getchannels'
set a.name='net.atos.entng.rss.controller.RssController|getchannels';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|getfeedItems'
set a.name='net.atos.entng.rss.controller.RssController|getfeedItems';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|updatechannel'
set a.name='net.atos.entng.rss.controller.RssController|updateChannel';
match (a:Action) where a.name='fr.wseduc.rss.controller.RssController|view'
set a.name='net.atos.entng.rss.controller.RssController|view';
commit