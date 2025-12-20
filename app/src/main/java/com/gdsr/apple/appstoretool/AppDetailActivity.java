package com.gdsr.apple.appstoretool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.view.Window;

public class AppDetailActivity extends Activity {

    private AppInfo appInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // 获取传递的应用信息
        appInfo = (AppInfo) getIntent().getSerializableExtra("appInfo");
        if (appInfo == null) {
            finish();
            return;
        }

        // 使用纯Java代码创建界面
        createUIProgrammatically();
    }

    private void createUIProgrammatically() {
        // 创建主ScrollView
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        // 创建主垂直布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(30, 30, 30, 30);
        mainLayout.setBackgroundColor(Color.WHITE);

        // 1. 应用图标和名称行
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                         ViewGroup.LayoutParams.MATCH_PARENT,
                                         ViewGroup.LayoutParams.WRAP_CONTENT));
        headerLayout.setPadding(0, 0, 0, 30);

        // 应用图标（带圆角）
        ImageView appIcon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(250, 250);
        iconParams.setMargins(0, 0, 30, 0);
        appIcon.setLayoutParams(iconParams);
        // 设置圆角
        appIcon.setBackgroundResource(R.drawable.rounded_corner);
        appIcon.setClipToOutline(true);
        loadAppIcon(appIcon, appInfo.getArtworkUrl100());

        // 应用名称和评分区域
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                       0,
                                       ViewGroup.LayoutParams.WRAP_CONTENT,
                                       1));

        TextView appName = new TextView(this);
        appName.setText(appInfo.getTrackName());
        appName.setTextSize(24);
        appName.setTextColor(0xFF000000);
        appName.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView developer = new TextView(this);
        developer.setText("开发者: " + appInfo.getArtistName());
        developer.setTextSize(16);
        developer.setTextColor(0xFF666666);
        developer.setLayoutParams(new LinearLayout.LayoutParams(
                                      ViewGroup.LayoutParams.WRAP_CONTENT,
                                      ViewGroup.LayoutParams.WRAP_CONTENT));

        // 平均评分
        TextView ratingText = new TextView(this);
        double rating = appInfo.getAverageUserRating();
        if (rating > 0) {
            DecimalFormat df = new DecimalFormat("#.#");
            ratingText.setText("评分: " + df.format(rating) + " ★");
        } else {
            ratingText.setText("评分: 暂无");
        }
        ratingText.setTextSize(14);
        ratingText.setTextColor(0xFF2196F3); // 蓝色突出显示
        ratingText.setLayoutParams(new LinearLayout.LayoutParams(
                                       ViewGroup.LayoutParams.WRAP_CONTENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT));

        textLayout.addView(appName);
        textLayout.addView(developer);
        textLayout.addView(ratingText);

        headerLayout.addView(appIcon);
        headerLayout.addView(textLayout);

        // 2. 版本信息区域（包含App大小）
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.HORIZONTAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                       ViewGroup.LayoutParams.MATCH_PARENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT));
        infoLayout.setPadding(0, 0, 0, 30);

        // 左侧：版本信息
        LinearLayout leftInfo = new LinearLayout(this);
        leftInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1);
        leftInfo.setLayoutParams(leftParams);

        // 当前版本
        TextView version = new TextView(this);
        version.setText("版本: " + appInfo.getVersion());
        version.setTextSize(16);
        version.setTextColor(0xFF888888);
        version.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

        // 最低系统版本
        TextView minOsVersion = new TextView(this);
        minOsVersion.setText("兼容 iOS " + appInfo.getMinimumOsVersion() + " 及以上版本");
        minOsVersion.setTextSize(16);
        minOsVersion.setTextColor(0xFF888888);
        minOsVersion.setLayoutParams(new LinearLayout.LayoutParams(
                                         ViewGroup.LayoutParams.WRAP_CONTENT,
                                         ViewGroup.LayoutParams.WRAP_CONTENT));

        leftInfo.addView(version);
        leftInfo.addView(minOsVersion);

        // 包名
        TextView bundleId = new TextView(this);
        bundleId.setText("包名: " + appInfo.getBundleId());
        bundleId.setTextSize(14);
        bundleId.setTextColor(0xFF888888);
        bundleId.setLayoutParams(new LinearLayout.LayoutParams(
                                     ViewGroup.LayoutParams.WRAP_CONTENT,
                                     ViewGroup.LayoutParams.WRAP_CONTENT));

        leftInfo.addView(bundleId);
        
        // 右侧：App大小
        LinearLayout rightInfo = new LinearLayout(this);
        rightInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1);
        rightInfo.setLayoutParams(rightParams);
        rightInfo.setGravity(Gravity.RIGHT | Gravity.TOP);

        // App大小
        TextView fileSize = new TextView(this);
        fileSize.setText("大小: " + formatFileSize(appInfo.getFileSizeBytes()));
        fileSize.setTextSize(16);
        fileSize.setTextColor(0xFF666666);
        fileSize.setLayoutParams(new LinearLayout.LayoutParams(
                                     ViewGroup.LayoutParams.WRAP_CONTENT,
                                     ViewGroup.LayoutParams.WRAP_CONTENT));
        fileSize.setGravity(Gravity.RIGHT);

        // 分享按钮
        Button shareButton = new Button(this);
        shareButton.setText("获取");
        shareButton.setTextSize(18);
        shareButton.setTextColor(Color.WHITE);
        // 创建圆角背景
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(0xFF4CAF50);
        drawable.setCornerRadius(dpToPx(18)); // 8dp圆角
        // 设置背景
        shareButton.setBackground(drawable);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 10, 0, 0);
        shareButton.setLayoutParams(buttonParams);
        shareButton.setPadding(12, 4, 12, 4);

        // 设置分享按钮点击事件
        shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareApp();
                }
            });

        rightInfo.addView(fileSize);
        rightInfo.addView(shareButton);

        infoLayout.addView(leftInfo);
        infoLayout.addView(rightInfo);

        // 3. 应用截图部分 - 只在有截图时显示
        List<String> screenshotUrls = appInfo.getScreenshotUrls();
        boolean hasScreenshots = screenshotUrls != null && !screenshotUrls.isEmpty();

        // 初始化截图相关变量
        TextView screenshotsTitle = null;
        HorizontalScrollView screenshotScrollView = null;

        if (hasScreenshots) {
            // 创建截图标题
            screenshotsTitle = new TextView(this);
            screenshotsTitle.setText("应用截图:");
            screenshotsTitle.setTextSize(20);
            screenshotsTitle.setTextColor(0xFF000000);
            screenshotsTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                                 ViewGroup.LayoutParams.WRAP_CONTENT,
                                                 ViewGroup.LayoutParams.WRAP_CONTENT));
            screenshotsTitle.setPadding(0, 0, 0, 20);

            // 水平滚动的截图容器
            screenshotScrollView = new HorizontalScrollView(this);
            screenshotScrollView.setLayoutParams(new LinearLayout.LayoutParams(
                                                     ViewGroup.LayoutParams.MATCH_PARENT,
                                                     1080));

            LinearLayout screenshotsLayout = new LinearLayout(this);
            screenshotsLayout.setOrientation(LinearLayout.HORIZONTAL);
            screenshotsLayout.setPadding(0, 0, 0, 0);

            // 加载截图
            for (String url : screenshotUrls) {
                ImageView screenshot = new ImageView(this);
                LinearLayout.LayoutParams screenshotParams = new LinearLayout.LayoutParams(610, 1080);
                screenshotParams.setMargins(0, 0, 20, 0);
                screenshot.setLayoutParams(screenshotParams);
                screenshot.setScaleType(ImageView.ScaleType.FIT_CENTER);
                screenshot.setBackgroundColor(0xFFF0F0F0);

                loadScreenshot(screenshot, url);
                screenshotsLayout.addView(screenshot);
            }

            screenshotScrollView.addView(screenshotsLayout);
        }

        // 4. 应用简介
        TextView descriptionTitle = new TextView(this);
        descriptionTitle.setText("应用简介:");
        descriptionTitle.setTextSize(20);
        descriptionTitle.setTextColor(0xFF000000);
        descriptionTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                             ViewGroup.LayoutParams.WRAP_CONTENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT));
                                             
        descriptionTitle.setPadding(0, 30, 0, 15); // 30dp上间距

        // 应用简介正文
        TextView description = new TextView(this);
        description.setText(appInfo.getDescription());
        description.setTextSize(14);
        description.setTextColor(0xFF666666);
        description.setLineSpacing(0, 1.3f);
        description.setLayoutParams(new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT));

        // 5. 更新内容（如果有截图，放在截图下方；如果没有截图，放在应用简介上方）
        boolean hasReleaseNotes = appInfo.getReleaseNotes() != null && !appInfo.getReleaseNotes().isEmpty();
        TextView releaseNotesTitle = null;
        TextView releaseNotes = null;

        if (hasReleaseNotes) {
            // 更新内容标题
            releaseNotesTitle = new TextView(this);
            releaseNotesTitle.setText("更新内容:");
            releaseNotesTitle.setTextSize(20);
            releaseNotesTitle.setTextColor(0xFF000000);
            releaseNotesTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                                  ViewGroup.LayoutParams.WRAP_CONTENT,
                                                  ViewGroup.LayoutParams.WRAP_CONTENT));

            // 根据是否有截图调整上间距
            if (hasScreenshots) {
                releaseNotesTitle.setPadding(0, 30, 0, 15); // 有截图时，30dp上间距
            } else {
                releaseNotesTitle.setPadding(0, 0, 0, 15); // 无截图时，0上间距
            }

            // 更新内容正文
            releaseNotes = new TextView(this);
            releaseNotes.setText(appInfo.getReleaseNotes());
            releaseNotes.setTextSize(14);
            releaseNotes.setTextColor(0xFF666666);
            releaseNotes.setLineSpacing(0, 1.2f);
            releaseNotes.setLayoutParams(new LinearLayout.LayoutParams(
                                             ViewGroup.LayoutParams.MATCH_PARENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // 6. 发布日期和更新日期信息（独立部分）
        boolean hasDateInfo = false;
        LinearLayout dateInfoContainer = new LinearLayout(this);
        dateInfoContainer.setOrientation(LinearLayout.VERTICAL);
        dateInfoContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                              ViewGroup.LayoutParams.MATCH_PARENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT));
        dateInfoContainer.setPadding(0, 30, 0, 0);

        // 首次发布日期
        if (appInfo.getReleaseDate() != null && !appInfo.getReleaseDate().isEmpty()) {
            hasDateInfo = true;
            String formattedReleaseDate = formatDate(appInfo.getReleaseDate());

            // 创建日期标题
            TextView releaseDateTitle = new TextView(this);
            releaseDateTitle.setText("首次发布:");
            releaseDateTitle.setTextSize(16);
            releaseDateTitle.setTextColor(0xFF000000);
            releaseDateTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                                 ViewGroup.LayoutParams.WRAP_CONTENT,
                                                 ViewGroup.LayoutParams.WRAP_CONTENT));
            releaseDateTitle.setPadding(0, 0, 0, 5);

            // 日期内容
            TextView releaseDate = new TextView(this);
            releaseDate.setText(formattedReleaseDate);
            releaseDate.setTextSize(14);
            releaseDate.setTextColor(0xFF666666);
            releaseDate.setLayoutParams(new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT));
            releaseDate.setPadding(0, 0, 0, 15);

            dateInfoContainer.addView(releaseDateTitle);
            dateInfoContainer.addView(releaseDate);
        }

        // 最近更新日期
        if (appInfo.getCurrentVersionReleaseDate() != null && !appInfo.getCurrentVersionReleaseDate().isEmpty()) {
            hasDateInfo = true;
            String formattedUpdateDate = formatDate(appInfo.getCurrentVersionReleaseDate());

            // 创建更新日期标题
            TextView updateDateTitle = new TextView(this);
            updateDateTitle.setText("最近更新:");
            updateDateTitle.setTextSize(16);
            updateDateTitle.setTextColor(0xFF000000);
            updateDateTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT));
            updateDateTitle.setPadding(0, 0, 0, 5);

            // 更新日期内容
            TextView updateDate = new TextView(this);
            updateDate.setText(formattedUpdateDate);
            updateDate.setTextSize(14);
            updateDate.setTextColor(0xFF666666);
            updateDate.setLayoutParams(new LinearLayout.LayoutParams(
                                           ViewGroup.LayoutParams.WRAP_CONTENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT));
            updateDate.setPadding(0, 0, 0, 15);

            dateInfoContainer.addView(updateDateTitle);
            dateInfoContainer.addView(updateDate);
        }

        // 7. 开发者网站
        if (appInfo.getSellerUrl() != null && !appInfo.getSellerUrl().isEmpty()) {
            TextView websiteTitle = new TextView(this);
            websiteTitle.setText("开发者网站:");
            websiteTitle.setTextSize(16);
            websiteTitle.setTextColor(0xFF000000);
            websiteTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                             ViewGroup.LayoutParams.WRAP_CONTENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT));
            websiteTitle.setPadding(0, 0, 0, 5);

            // 可点击的网站链接
            TextView websiteLink = new TextView(this);
            websiteLink.setText(appInfo.getSellerUrl());
            websiteLink.setTextSize(14);
            websiteLink.setTextColor(0xFF2196F3); // 蓝色链接
            websiteLink.setLayoutParams(new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT));

            // 设置点击事件
            websiteLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openWebsite(appInfo.getSellerUrl());
                    }
                });

            dateInfoContainer.addView(websiteTitle);
            dateInfoContainer.addView(websiteLink);
        }

        // 将所有视图添加到主布局
        mainLayout.addView(headerLayout);          // 1. 应用图标和名称
        mainLayout.addView(infoLayout);           // 2. 版本信息和分享按钮

        // 3. 应用截图（只在有截图时添加）
        if (hasScreenshots) {
            mainLayout.addView(screenshotsTitle);     // 截图标题
            mainLayout.addView(screenshotScrollView); // 截图滚动视图
        }

        // 4. 更新内容（如果有）
        if (hasReleaseNotes) {
            mainLayout.addView(releaseNotesTitle); // 更新内容标题
            mainLayout.addView(releaseNotes);      // 更新内容正文
        }

        mainLayout.addView(descriptionTitle);     // 5. 应用简介标题
        mainLayout.addView(description);          // 6. 应用简介内容

        // 7. 日期信息和网站（独立部分，在应用简介下方）
        if (hasDateInfo || (appInfo.getSellerUrl() != null && !appInfo.getSellerUrl().isEmpty())) {
            mainLayout.addView(dateInfoContainer);
        }

        scrollView.addView(mainLayout);

        // 设置内容视图
        setContentView(scrollView);
    }

    // 格式化文件大小
    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "未知";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = 0;

        double size = bytes;
        while (size >= 1024 && digitGroups < units.length - 1) {
            size /= 1024;
            digitGroups++;
        }

        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(size) + " " + units[digitGroups];
    }
    
    // dp转px方法
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
    
    // 格式化日期
    private String formatDate(String dateString) {
        try {
            // 尝试解析ISO格式日期
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            Date date = inputFormat.parse(dateString);

            // 格式化为中文日期格式
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，返回原始字符串
            return dateString;
        }
    }

    // 打开网站
    private void openWebsite(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }

    // 分享应用功能
    private void shareApp() {
        if (appInfo.getTrackViewUrl() == null || appInfo.getTrackViewUrl().isEmpty()) {
            Toast.makeText(this, "无法分享，链接无效", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // 分享内容
            String shareText = "在 iPhone 或 iPad 上安装此 App：" + appInfo.getTrackName() + "\n" +
                "评分: " + String.format("%.1f", appInfo.getAverageUserRating()) + " ★\n" +
                "开发者: " + appInfo.getArtistName() + "\n" +
                "下载链接: " + appInfo.getTrackViewUrl();

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享应用：" + appInfo.getTrackName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "分享应用"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void loadScreenshot(final ImageView imageView, final String imageUrl) {
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
}
