# SolrWaybackRootProxy
Using the solrwaybackrootproxy will improve playback in SolrWayback. It can redirect and fix leaked resources when running in socks4 mode.
This module is optional for the SolrWayback web-application. Even when not running in Socks mode, SolrWaybackRootProxy can
fix and direct leaks that are relative ie. '../images/test.png'. Without proxy mode it can not fix absolute URLs, since they will leak
to the live web.

## Install
The web-application must be installed in the same Tomcat as SolrWayback, and it must be installed in the root context of the Tomcat.
An easy way to do this is just to rename solrwaybackrootproxy.war to ROOT.war.