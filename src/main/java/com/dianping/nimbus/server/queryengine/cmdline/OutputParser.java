package com.dianping.nimbus.server.queryengine.cmdline;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.nimbus.client.bo.HiveQueryOutputBo;

public class OutputParser {
	private static final Log logger = LogFactory.getLog(OutputParser.class);

	private final static char DELIMITER = '\t';
	private static OutputParser instance;

	private OutputParser() {
	}

	public static OutputParser getInstance() {
		if (instance == null) {
			synchronized (OutputParser.class) {
				if (instance == null) {
					instance = new OutputParser();
				}
			}
		}
		return instance;
	}

	public HiveQueryOutputBo parse(InputStream is, int limit)
			throws IOException {
		HiveQueryOutputBo result = new HiveQueryOutputBo();
		LineIterator it = IOUtils.lineIterator(is, BasicUtils.ENCODING);
		try {
			// the first line is column names
			int columnCount = 0;
			if (it.hasNext()) {
				if (logger.isDebugEnabled()) {
					logger.debug("start to set columns names to HiveQueryOutputBo");
				}
				String[] fieldSchema = parseOneLine(it.nextLine());
				if (fieldSchema != null) {
					logger.info("field schema: " + StringUtils.join(fieldSchema, '\t'));
					result.setFieldSchema(fieldSchema);
					columnCount = fieldSchema.length;
				}
			}
			int lineNum = 0;
			while (it.hasNext() && lineNum < limit) {
				String data = it.nextLine();
				String[] fieldsData = parseOneLine(data);
				if (fieldsData != null && columnCount == fieldsData.length) {
					result.addOneRow(parseOneLine(data));
				}
				lineNum++;
			}
			logger.info("data row count: " + result.getData().size());
		} finally {
			it.close();
		}
		return result;
	}

	private String[] parseOneLine(String line) {
		String[] fields = StringUtils.splitPreserveAllTokens(line, DELIMITER);
		if (fields == null || fields.length <= 0) {
			return null;
		}
		return fields;
	}
}
