package com.dianping.nimbus.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileDownload extends HttpServlet {
	private static final Log LOG = LogFactory.getLog(FileDownload.class);
	private static final long serialVersionUID = 1L;
	private static final String DOWNLOAD_REST_PREFIX = "/polestar/query/download/";

	private String polestarHost;

	@Override
	public void init() {
		polestarHost = getServletContext().getInitParameter("polestar-host");
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String filename = URLDecoder.decode(request.getPathInfo(), "UTF-8");
		if (filename.startsWith("/")) {
			filename = filename.substring(1);
		}
		String getFileRequest = polestarHost + DOWNLOAD_REST_PREFIX + filename;
		LOG.info("request download filename:" + getFileRequest);
		response.sendRedirect(getFileRequest);
	}
}
