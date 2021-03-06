package com.dianping.nimbus.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.nimbus.client.bo.HiveQueryInputBo;
import com.dianping.nimbus.client.bo.HiveQueryOutputBo;
import com.dianping.nimbus.client.bo.QueryErrorBo;
import com.dianping.nimbus.client.bo.QueryFavoriteBo;
import com.dianping.nimbus.client.css.TableResources;
import com.dianping.nimbus.client.service.HiveQueryServiceAsync;
import com.dianping.nimbus.client.service.LoginServiceAsync;
import com.dianping.nimbus.client.widget.AutoCompleteTextArea;
import com.dianping.nimbus.client.widget.HiveKeyword;
import com.dianping.nimbus.client.widget.SimpleAutoCompletionItems;
import com.dianping.nimbus.shared.util.UUID;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@UrlPatternEntryPoint("hivequery.html(\\\\?gwt.codesvr=127.0.0.1:9997)?")
public class HiveQuery extends LoginComponent implements EntryPoint {
	private static HiveQueryUiBinder uiBinder = GWT
			.create(HiveQueryUiBinder.class);

	interface HiveQueryUiBinder extends UiBinder<Widget, HiveQuery> {
	}

	@UiField(provided = true)
	CellTable<String[]> cellTable;
	@UiField(provided = true)
	SimplePager pager;
	@UiField
	ListBox dbListBox;
	@UiField
	ListBox engineListBox;
	@UiField
	ListBox queryFavoriteListBox;
	@UiField(provided = true)
	TextArea hqlTextArea;
	@UiField
	TextArea progressTextArea;
	@UiField
	CheckBox isStoreFile;
	@UiField
	ListBox saveRecordsListBox;
	@UiField
	Button submitBut;
	@UiField
	Button submitQPBut;
	@UiField
	Button killQueryBut;
	@UiField
	Button saveQuery;
	@UiField
	Button submitError;

	private Map<String, String> queryFavoriteMap = new HashMap<String, String>();
	private List<String[]> data = new ArrayList<String[]>();
	private String queryid = "";
	private Boolean isQueryStopped = false;
	private Timer timer = null;

	private List<IndexedColumn> indexedColumns = new ArrayList<IndexedColumn>();

	private AsyncDataProvider<String[]> provider = new AsyncDataProvider<String[]>() {
		@Override
		protected void onRangeChanged(HasData<String[]> display) {
			int start = display.getVisibleRange().getStart();
			int end = start + display.getVisibleRange().getLength();
			end = end >= data.size() ? data.size() : end;
			List<String[]> sub = data.subList(start, end);
			updateRowData(start, sub);
		}
	};

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();
	private final LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	public void onModuleLoad() {
		if (getTokenid() == null || getTokenid().equals("")) {
			cleanup();
		} else {
			loginService.isAuthenticated(getTokenid(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								drawPanel();
							} else {
								cleanup();
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
							cleanup();
						}
					});
		}
	}

	private void drawPanel() {
		this.initialize();
		this.bind();

		hiveQueryService.getDatabases(getTokenid(),
				new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null) {
							dbListBox.clear();
							for (String res : result) {
								dbListBox.addItem(res);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});

		getFavoriteQuerys();
	}

	private void getFavoriteQuerys() {
		hiveQueryService.getFavoriteQuery(getRealuser(),
				new AsyncCallback<List<QueryFavoriteBo>>() {

					@Override
					public void onSuccess(List<QueryFavoriteBo> result) {
						if (result != null) {
							queryFavoriteListBox.clear();
							queryFavoriteListBox.addItem("--请选择--");
							for (QueryFavoriteBo res : result) {
								queryFavoriteListBox.addItem(res.getQueryName());
								queryFavoriteMap.put(res.getQueryName(),
										res.getHql());
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});
	}

	private void bind() {
		queryFavoriteListBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String queryName = queryFavoriteListBox
						.getItemText(queryFavoriteListBox.getSelectedIndex());
				if (queryFavoriteMap.get(queryName) != null) {
					hqlTextArea.setText(queryFavoriteMap.get(queryName));
				}
			}
		});

		hqlTextArea.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {
				NativeEvent ne = event.getNativeEvent();
				if (ne.getCtrlKey()) {
					if (ne.getKeyCode() == KeyCodes.KEY_ENTER) {
						submitBut.click();
					} else if (ne.getKeyCode() == KeyCodes.KEY_ALT) {
						submitQPBut.click();
					}
				}
			}
		});
	}

	@UiHandler("killQueryBut")
	void killQueryButtonHandlerClick(ClickEvent e) {
		if (!"".equals(queryid)) {
			hiveQueryService.stopQuery(queryid, new AsyncCallback<Boolean>() {

				@Override
				public void onSuccess(Boolean result) {
					if (result) {
						isQueryStopped = true;
						progressTextArea.setText("停止查询成功!");
					} else {
						Window.alert("停止查询失败");
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("停止查询失败");
				}
			});
		}
	}

	@UiHandler("submitError")
	void submitQueryError(ClickEvent e) {
		if (hqlTextArea.getValue().trim().equals("")) {
			Window.alert("输入框不能空");
		} else {
			QueryErrorBo qe = new QueryErrorBo();
			qe.setMode(engineListBox.getItemText(engineListBox
					.getSelectedIndex()));
			qe.setUsername(getRealuser());
			qe.setSql(hqlTextArea.getValue().trim());
			qe.setStatus(progressTextArea.getValue().trim());
			hiveQueryService.submitQueryError(qe, new AsyncCallback<Boolean>() {

				@Override
				public void onSuccess(Boolean result) {
					if (result == true) {
						Window.alert("提交错误成功");
					} else {
						Window.alert("提交错误失败");
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("提交错误失败" + caught.toString());
				}
			});
		}
	}

	@UiHandler("submitBut")
	void hqlSubmitButtonHandleClick(ClickEvent e) {
		String selectedText = hqlTextArea.getSelectedText().trim();
		if ("".equals(selectedText)) {
			selectedText = hqlTextArea.getValue().trim();
		}

		// filter local command or empty command
		if ("".equals(selectedText) || selectedText.startsWith("!")) {
			return;
		}

		HiveQueryInputBo hqInputBo = new HiveQueryInputBo();
		hqInputBo.setHql(selectedText);
		hqInputBo.setEngineMode(engineListBox.getItemText(engineListBox
				.getSelectedIndex()));
		hqInputBo.setDatabase(dbListBox.getItemText(dbListBox
				.getSelectedIndex()));
		hqInputBo.setResultLimit(Integer.parseInt(saveRecordsListBox
				.getItemText(saveRecordsListBox.getSelectedIndex())));
		hqInputBo.setTimestamp(new Date().getTime());
		hqInputBo.setUsername(getUsername());
		hqInputBo.setRealuser(getRealuser());
		hqInputBo.setTokenid(getTokenid());
		hqInputBo.setStoreResult(isStoreFile.getValue());
		queryid = UUID.createUUID();
		hqInputBo.setQueryid(queryid);

		submitBut.setEnabled(false);
		killQueryBut.setEnabled(true);
		isQueryStopped = false;

		progressTextArea.setText("正在执行语句 .........");
		clearCellTableData();
		hiveQueryService.getQueryResult(hqInputBo,
				new AsyncCallback<HiveQueryOutputBo>() {

					@Override
					public void onSuccess(HiveQueryOutputBo result) {
						cancelTimer();
						submitBut.setEnabled(true);
						killQueryBut.setEnabled(false);

						String errorMessage = result.getErrorMsg();
						if (errorMessage != null && !"".equals(errorMessage)) {
							progressTextArea.setText(errorMessage);
						}
						int rowCount = result.getData().size();
						int columnCount = result.getFieldSchema().length;
						for (int i = 0; i < columnCount; i++) {
							IndexedColumn col = new IndexedColumn(i);
							cellTable.addColumn(col, result.getFieldSchema()[i]);
							indexedColumns.add(col);
						}
						data = result.getData();
						cellTable.setRowCount(rowCount);
						provider = new AsyncDataProvider<String[]>() {
							@Override
							protected void onRangeChanged(
									HasData<String[]> display) {
								int start = display.getVisibleRange()
										.getStart();
								int end = start
										+ display.getVisibleRange().getLength();
								end = end >= data.size() ? data.size() : end;
								List<String[]> sub = data.subList(start, end);
								updateRowData(start, sub);
							}
						};
						provider.addDataDisplay(cellTable);
						provider.updateRowCount(data.size(), true);
					}

					@Override
					public void onFailure(Throwable caught) {
						cancelTimer();
						submitBut.setEnabled(true);
						killQueryBut.setEnabled(false);
						if (isQueryStopped) {
							progressTextArea.setText("停止查询成功!");
						}
					}
				});

		timer = new Timer() {
			public void run() {
				getQueryStatus(queryid);
			}
		};
		timer.schedule(2000);
		timer.scheduleRepeating(2000);
	}

	private void clearCellTableData() {
		provider = null;
		data = null;
		removeCellTableAllColumns(cellTable, indexedColumns);
		indexedColumns.clear();
		cellTable.setRowCount(0);
	}

	private void cancelTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void getQueryStatus(String queryid) {
		hiveQueryService.getQueryStatus(queryid, new AsyncCallback<String>() {

			@Override
			public void onSuccess(String result) {
				progressTextArea.setText(result);
				progressTextArea.getElement().setScrollTop(
						progressTextArea.getElement().getScrollHeight());
			}

			@Override
			public void onFailure(Throwable caught) {
				progressTextArea.setText("查询进度返回错误!");
			}
		});

	}

	@UiHandler("saveQuery")
	void saveQueryButtonHandleClick(ClickEvent e) {
		String latestSaveQueryName = "";
		if (queryFavoriteListBox.getItemCount() > 1) {
			latestSaveQueryName = queryFavoriteListBox.getItemText(1);
		}
		DialogBox dlg = new SaveQueryDialog(hiveQueryService, getRealuser(),
				hqlTextArea.getValue().trim(), latestSaveQueryName);
		dlg.center();
	}

	@UiHandler("submitQPBut")
	void queryPlanSubmitButtonHandleClick(ClickEvent e) {
		if ("".equals(hqlTextArea.getValue().trim())) {
			return;
		}

		progressTextArea.setText("正在获取执行计划 .........");

		hiveQueryService.getQueryPlan(getTokenid(), hqlTextArea.getValue(),
				dbListBox.getItemText(dbListBox.getSelectedIndex()),
				new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						progressTextArea.setText(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						progressTextArea.setText("获取执行计划失败 !");
					}
				});
	}

	private void removeCellTableAllColumns(CellTable<String[]> celltable,
			List<IndexedColumn> columns) {
		if (columns != null) {
			for (IndexedColumn col : columns) {
				celltable.removeColumn(col);
			}
		}
	}

	private void initialize() {
		hqlTextArea = new TextArea();
//		hqlTextArea.setCompletionItems(new SimpleAutoCompletionItems(
//				HiveKeyword.getKeywordsArray()));

		cellTable = new CellTable<String[]>(50,
				GWT.<TableResources> create(TableResources.class));

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		cellTable.setWidth("100%", false);

		constructSampleData();

		provider.addDataDisplay(cellTable);
		provider.updateRowCount(data.size(), true);

		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		pager.setDisplay(cellTable);

		Widget widget = uiBinder.createAndBindUi(this);
		killQueryBut.setEnabled(false);

		saveRecordsListBox.addItem("100000");
		saveRecordsListBox.addItem("500000");
		saveRecordsListBox.addItem("1000000");
		constructEngineListBox();

		RootPanel.get("HiveQuery").add(widget);
	}

	private void constructEngineListBox() {
//		if (Constants.SHARK_BANNED_USERS.contains(getUsername())) {
//			engineListBox.insertItem(QueryEngine.HIVE.getName(), 0);
//			engineListBox.insertItem(QueryEngine.SHARK.getName(), 1);
//		} else {
		engineListBox.insertItem(QueryEngine.SHARK.getName(), 0);
	    engineListBox.insertItem(QueryEngine.HIVE.getName(), 1);
//		}
	}

	private void constructSampleData() {
		String[] headers = new String[] { "col1", "col2", "col3", "col4",
				"col5", "col6" };

		for (int i = 0; i < headers.length; i++) {
			IndexedColumn col = new IndexedColumn(i);
			indexedColumns.add(col);
			cellTable.addColumn(col, headers[i]);
		}
		cellTable.setRowCount(data.size());
	}

	static class SaveQueryDialog extends DialogBox {

		public SaveQueryDialog(final HiveQueryServiceAsync hiveQueryService,
				final String username, final String hql,
				final String defaultQueryName) {
			setText("查询另存为");

			VerticalPanel vPanel = new VerticalPanel();
			vPanel.setSpacing(4);
			HorizontalPanel hPanel = new HorizontalPanel();
			hPanel.setSpacing(4);

			final HTML msg = new HTML("<br /><b>请输入保存的查询名：</b>", true);
			final TextBox queryTextBox = new TextBox();
			queryTextBox.setText(defaultQueryName);

			Button submitButton = new Button("确认", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String queryName = queryTextBox.getValue().trim();
					if (!"".equals(queryName) && !"".equals(hql)
							&& !"".equals(username)) {
						hiveQueryService.saveQuery(username, queryName, hql,
								new AsyncCallback<Boolean>() {

									@Override
									public void onSuccess(Boolean result) {
										hide();
									}

									@Override
									public void onFailure(Throwable caught) {
										hide();
									}
								});
					}
				}
			});

			Button closeButton = new Button("取消", new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			hPanel.add(submitButton);
			hPanel.add(closeButton);
			vPanel.add(msg);
			vPanel.add(queryTextBox);
			vPanel.add(hPanel);

			// setAnimationEnabled(true);
			setWidget(vPanel);
			setGlassEnabled(true);
		}

		public void onClick(Widget sender) {
			hide();
		}
	}
}
