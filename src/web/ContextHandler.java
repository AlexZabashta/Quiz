package web;

import com.sun.net.httpserver.HttpHandler;

public interface ContextHandler extends HttpHandler {
	String getContext();
}
