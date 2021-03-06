package com.dianping.nimbus.server.queryengine.cmdline;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.nimbus.client.bo.HiveQueryInputBo;
import com.dianping.nimbus.client.bo.HiveQueryOutputBo;
import com.dianping.nimbus.server.queryengine.HiveQueryInput;
import com.dianping.nimbus.server.queryengine.HiveQueryOutput;
import com.dianping.nimbus.server.queryengine.IQueryEngine;
import com.dianping.nimbus.server.queryengine.cmdline.streamhandler.IStreamHandler;
import com.dianping.nimbus.server.queryengine.cmdline.streamhandler.StreamHandlerFactory;
import com.dianping.nimbus.server.queryengine.jdbc.DataFileStore;

public class HiveCmdLineQueryEngine implements IQueryEngine {

	private static final Log logger = LogFactory
			.getLog(HiveCmdLineQueryEngine.class);

	private static final String KILL_COMMAND_PREFIX = "Kill Command =";
	private static final int KILL_COMMAND_PREFIX_LENGTH = KILL_COMMAND_PREFIX
			.length();

	@Override
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input) {
		String username = input.getUsername();
		String hiveCmd = input.getHql().trim();
		String engineMode = input.getEngineMode();
		if (StringUtils.isEmpty(hiveCmd)) {
			logger.error("Input hqls is empty!");
			return null;
		}
		hiveCmd = "use " + input.getDatabase() + ";" + hiveCmd;

		boolean storeResultToFile = input.isStoreResult();
		String querId = input.getQueryid();
		int saveToFileRecordslimit = input.getResultLimit();

		if (StringUtils.isEmpty(username)) {
			logger.error("Input username is empty!");
			return null;
		}

		String statusLocation = getStatusFileLocation(input.getQueryid());
		String resultLocation = input.getResultLocation();
		logger.debug("statusLocation:" + statusLocation + " result location:" + resultLocation);

		IStreamHandler resultHandler = StreamHandlerFactory
					.createFileResultHandler(resultLocation);
		resultHandler.setShowLimit(500);
		if (saveToFileRecordslimit > 0) {
			resultHandler.setSaveRecordsLimit(saveToFileRecordslimit);
		}

		String ticketCache = "/tmp/" + username + ".ticketcache"; 
		if (engineMode.equals("shark")) {
			engineMode = "shark-witherror";
		}
		
		String cmd = joinString("bash -c \"",
				"export KRB5CCNAME=" ,ticketCache ,  ";", engineMode,  " --hiveconf hive.cli.print.header=true -e \\\"", hiveCmd,
				"\\\"\"");
		logger.info("realuser:" + input.getRealuser()  + ", cmd: " + cmd);
		
		HiveQueryOutputBo res = new HiveQueryOutputBo(); 
		res.setSuccess(false);
		
		try {
			int exitCode = -1;
			try {
				// execute cmd
				exitCode = ShellCmdExecutor.getInstance().execute(cmd, querId,
						resultHandler, statusLocation);
			} catch (ShellCmdExecException e) {
				logger.error(e);
			}
			
			if (exitCode != 0) {
				logger.error("Hive Command is NOT executed successfully! The exit code of hive command is "
						+ exitCode + " , query command: " + cmd);
				res.setErrorMsg(readStatusFileToString(querId));
				res.setSuccess(false);
				return res;
			}
			
			res = resultHandler.getResult();
			
			if (res == null) {
				res = new HiveQueryOutputBo();
				res.setSuccess(false);
				res.setErrorMsg(readStatusFileToString(querId));
				return res;
			}
			
			res.setSuccess(true);
			res.setErrorMsg(readStatusFileToString(querId));
		} finally {
			// remove data result file if user didn't request to store
			if (storeResultToFile) {
				res.setResultFileAbsolutePath(resultLocation);
			} else {
				FileUtils.deleteQuietly(new File(resultLocation));
			}
		}
		return res;
	}

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		return null;
	}

	@Override
	public String getQueryStatus(String queryId) {
		return readStatusFileToString(queryId); 
	}
	
	private static String readStatusFileToString(String queryId) {
		String statusFileLocation = getStatusFileLocation(queryId);
		try {
			return FileUtils.readFileToString(new File(statusFileLocation),
					BasicUtils.ENCODING);
		} catch (IOException e) {
			logger.error("Exception occurs in reading status file: "
					+ statusFileLocation, e);
			return StringUtils.EMPTY;
		}
	}

	private static String getStatusFileLocation(String queryId) {
		String statusFileLocation = joinLocation(
				DataFileStore.QUERY_STATUS_LOCATION, queryId + ".stat");
		return statusFileLocation;
	}

	private static String joinString(String... strings) {
		return BasicUtils.joinString(strings);
	}

	private static String joinLocation(String... locationParts) {
		return BasicUtils.joinLocation(locationParts);
	}

	@Override
	public Boolean stopQuery(String queryId) {
		try {
			ShellCmdExecutor.getInstance().stopRunningTask(queryId);
		} catch (Exception e) {
			logger.error("The running query for queryId: " + queryId
					+ "is NOT found!", e);
			return false;
		}
		// stop submitted hadoop job
		File statusFile = new File(getStatusFileLocation(queryId));
		if (statusFile.exists() && statusFile.canRead()) {
			try {
				LineIterator it = BasicUtils.lineIterator(statusFile,
						BasicUtils.ENCODING, false);
				while (it.hasNext()) {
					String line = it.next().toString().trim();
					if (StringUtils.isEmpty(line)) {
						continue;
					}
					if (line.startsWith(KILL_COMMAND_PREFIX)) {
						String killCommand = line.substring(
								KILL_COMMAND_PREFIX_LENGTH).trim();
						logger.info("Kill Command:" + killCommand);
						Runtime run = Runtime.getRuntime();
						Process p = run.exec(killCommand);
						if (p.waitFor() != 0) {
							logger.error("Kill Hadoop Job Failed, exitcode :"
									+ p.exitValue());
						} else {
							logger.info("Kill Hadoop Job Succeed");
						}
					}
				}
			} catch (Exception e) {
				logger.error(
						"Exception Occurs in stopping running hadoop task!", e);
				return false;
			}
		}
		return true;
	}
}
