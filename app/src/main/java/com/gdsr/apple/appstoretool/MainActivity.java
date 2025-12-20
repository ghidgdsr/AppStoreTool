package com.gdsr.apple.appstoretool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import android.view.Window;

public class MainActivity extends Activity {

    private EditText etAppName;
    private Spinner spinnerCountry;
    private Button btnSearch;
    private ProgressBar progressBar;
    private ListView listViewResults;
    private TextView tvResultCount;

    // 存储查询结果
    private String QueryResult = "";
    private List<AppInfo> appList = new ArrayList<>();
    private AppListAdapter adapter;

    // 国家/地区代码映射
    private static final String[] COUNTRIES = {"中国大陆", "美国", "中国香港", "台湾", "日本", "韩国","英国", "澳大利亚"};
    private static final String[] COUNTRY_CODES = {"CN", "US", "HK", "TW", "JP", "KR", "GB", "AU"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSpinner();
        setupButtonListener();
        setupListView();
        setupListViewClickListener();
    }

    private void initViews() {
        etAppName = (EditText) findViewById(R.id.etAppName);
        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listViewResults = (ListView) findViewById(R.id.listViewResults);
        tvResultCount = (TextView) findViewById(R.id.tvResultCount);
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
            this, 
            android.R.layout.simple_spinner_item, 
            COUNTRIES
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(spinnerAdapter);
    }

    private void setupListView() {
        adapter = new AppListAdapter();
        listViewResults.setAdapter(adapter);
    }

    private void setupListViewClickListener() {
        listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // 获取点击的应用信息
                    AppInfo selectedApp = appList.get(position);

                    // 启动详情页面
                    Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                    intent.putExtra("appInfo", selectedApp);
                    startActivity(intent);
                }
            });
    }

    private void setupButtonListener() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performSearch();
                }
            });
    }

    private void performSearch() {
        String appName = etAppName.getText().toString().trim();

        if (appName.isEmpty()) {
            Toast.makeText(this, R.string.empty_app_name, Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取选中的国家代码
        int selectedPosition = spinnerCountry.getSelectedItemPosition();
        String countryCode = COUNTRY_CODES[selectedPosition];

        // 开始网络请求
        startNetworkRequest(appName, countryCode);
    }

    private void startNetworkRequest(final String appName, final String countryCode) {
        progressBar.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(false);
        listViewResults.setVisibility(View.GONE);
        tvResultCount.setVisibility(View.GONE);

        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 构建请求URL
                        String encodedAppName = URLEncoder.encode(appName, "UTF-8");
                        String urlString = "https://itunes.apple.com/search?term=" + 
                            encodedAppName + "&country=" + 
                            countryCode + "&entity=software&limit=40";

                        URL url = new URL(urlString);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);

                        int responseCode = connection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(inputStream, "UTF-8"));

                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                            reader.close();
                            inputStream.close();

                            // 保存结果到变量
                            QueryResult = response.toString();

                            // 解析JSON数据（需要解析截图信息）
                            parseJsonResponse(QueryResult);

                            // 在主线程显示结果
                            runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showResultsOnUI();
                                    }
                                });

                        } else {
                            showErrorOnUI(R.string.network_error);
                        }

                        connection.disconnect();

                    } catch (final Exception e) {
                        e.printStackTrace();
                        showErrorOnUI(R.string.network_error);
                    } finally {
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    btnSearch.setEnabled(true);
                                }
                            });
                    }
                }
            }).start();
    }

    // 在 parseJsonResponse 方法中，添加新字段的解析：
    private void parseJsonResponse(String jsonResponse) {
        try {
            appList.clear();

            // 直接解析JSON
            String jsonStr = jsonResponse;
            if (jsonStr.startsWith("jsonp")) {
                // 去除jsonp包裹函数
                int startIndex = jsonStr.indexOf('(');
                int endIndex = jsonStr.lastIndexOf(')');
                if (startIndex != -1 && endIndex != -1) {
                    jsonStr = jsonStr.substring(startIndex + 1, endIndex);
                }
            }

            JSONObject jsonObject = new JSONObject(jsonStr);

            JSONArray resultsArray = jsonObject.optJSONArray("results");
            if (resultsArray != null && resultsArray.length() > 0) {
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject appObject = resultsArray.getJSONObject(i);

                    AppInfo appInfo = new AppInfo();
                    appInfo.setTrackName(appObject.optString("trackName", "未知应用"));
                    appInfo.setArtworkUrl100(appObject.optString("artworkUrl100", ""));
                    appInfo.setBundleId(appObject.optString("bundleId", ""));
                    appInfo.setArtistName(appObject.optString("artistName", "未知开发者"));
                    appInfo.setTrackViewUrl(appObject.optString("trackViewUrl", ""));
                    appInfo.setDescription(appObject.optString("description", "暂无简介"));
                    appInfo.setMinimumOsVersion(appObject.optString("minimumOsVersion", "未知"));
                    appInfo.setVersion(appObject.optString("version", ""));
                    appInfo.setReleaseNotes(appObject.optString("releaseNotes", ""));

                    // 新增字段
                    appInfo.setReleaseDate(appObject.optString("releaseDate", ""));
                    appInfo.setCurrentVersionReleaseDate(appObject.optString("currentVersionReleaseDate", ""));
                    appInfo.setSellerUrl(appObject.optString("sellerUrl", ""));

                    // 评分和大小字段
                    appInfo.setAverageUserRating(appObject.optDouble("averageUserRating", 0.0));

                    // 处理文件大小
                    String fileSizeStr = appObject.optString("fileSizeBytes", "0");
                    try {
                        long fileSizeBytes = Long.parseLong(fileSizeStr);
                        appInfo.setFileSizeBytes(fileSizeBytes);
                    } catch (NumberFormatException e) {
                        appInfo.setFileSizeBytes(0);
                    }

                    // 解析截图URLs
                    JSONArray screenshotUrls = appObject.optJSONArray("screenshotUrls");
                    List<String> screenshots = new ArrayList<>();

                    if (screenshotUrls != null && screenshotUrls.length() > 0) {
                        // 使用 iPhone 截图
                        for (int j = 0; j < Math.min(screenshotUrls.length(), 10); j++) {
                            screenshots.add(screenshotUrls.getString(j));
                        }
                    }
                    appInfo.setScreenshotUrls(screenshots);

                    appList.add(appInfo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 在 showResultsOnUI() 方法中，搜索完成后一次性加载所有图标
    private void showResultsOnUI() {
        if (appList.isEmpty()) {
            Toast.makeText(this, "未找到相关应用", Toast.LENGTH_SHORT).show();
            return;
        }

        // 一次性加载所有图标
        loadAllAppIcons();

        // 更新适配器
        adapter.notifyDataSetChanged();

        // 显示结果数量
        tvResultCount.setText(String.format(getString(R.string.result_count), appList.size()));
        tvResultCount.setVisibility(View.VISIBLE);

        // 显示列表
        listViewResults.setVisibility(View.VISIBLE);
    }

    private void showErrorOnUI(final int errorResId) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, errorResId, Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void loadAllAppIcons() {
        for (int i = 0; i < appList.size(); i++) {
            final AppInfo appInfo = appList.get(i);

            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(appInfo.getArtworkUrl100());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();

                            InputStream input = connection.getInputStream();
                            final Bitmap bitmap = BitmapFactory.decodeStream(input);

                            // 存储到AppInfo对象中
                            appInfo.setIconBitmap(bitmap);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
        }

        // 所有图标开始加载后，1秒后刷新列表（让用户先看到文字）
        new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            }, 1000);
    }
    
    // 自定义适配器
    private class AppListAdapter extends ArrayAdapter<AppInfo> {

        public AppListAdapter() {
            super(MainActivity.this, R.layout.item_app, appList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_app, parent, false);
            }

            AppInfo currentApp = appList.get(position);

            TextView tvAppName = (TextView) itemView.findViewById(R.id.tvAppName);
            TextView tvDeveloper = (TextView) itemView.findViewById(R.id.tvDeveloper);
            TextView tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            ImageView ivAppIcon = (ImageView) itemView.findViewById(R.id.ivAppIcon);

            tvAppName.setText(currentApp.getTrackName());
            tvDeveloper.setText(currentApp.getArtistName());
           
            // 设置圆角
            ivAppIcon.setBackgroundResource(R.drawable.rounded_corner);
            ivAppIcon.setClipToOutline(true);

            // 直接使用已加载的位图，避免异步加载
            if (currentApp.getIconBitmap() != null) {
                ivAppIcon.setImageBitmap(currentApp.getIconBitmap());
            } else {
                // 如果还没加载完，显示默认图标
                ivAppIcon.setImageResource(R.drawable.rounded_corner);
            }

            return itemView;
        }
    }

    // 异步加载图标
    private void loadAppIcon(final ImageView imageView, final String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(imageUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();

                        InputStream input = connection.getInputStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(input);

                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    // 提供获取查询结果的公共方法
    public String getQueryResult() {
        return QueryResult;
    }

    // 提供获取应用列表的方法
    public List<AppInfo> getAppList() {
        return appList;
    }
}
