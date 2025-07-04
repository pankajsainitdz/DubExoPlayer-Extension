package com.dubexoplayer.extension;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride;
import com.google.android.exoplayer2.Format;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@DesignerComponent(
    version = YaVersion.TEXTBOX_COMPONENT_VERSION,
    description = "Advanced ExoPlayer extension for playing MP4 and M3U8 videos with fullscreen, quality selection, and download features",
    category = ComponentCategory.EXTENSION,
    nonVisible = false,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "exoplayer-core.jar, exoplayer-ui.jar, exoplayer-hls.jar")
public class DubExoPlayer extends AndroidViewComponent {

    private static final String TAG = "DubExoPlayer";
    
    private Context context;
    private Activity activity;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;
    private ExecutorService downloadExecutor;
    
    private String currentVideoUrl = "";
    private boolean isFullscreen = false;
    private float playbackSpeed = 1.0f;
    private String selectedQuality = "Auto";
    
    // Constructor
    public DubExoPlayer(ComponentContainer container) {
        super(container);
        this.context = container.$context();
        this.activity = (Activity) context;
        this.downloadExecutor = Executors.newSingleThreadExecutor();
        
        initializePlayer();
        initView();
    }
    
    private void initializePlayer() {
        // Create track selector for quality selection
        trackSelector = new DefaultTrackSelector(context);
        
        // Create ExoPlayer instance
        exoPlayer = new ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setLoadControl(new DefaultLoadControl())
            .build();
            
        // Create data source factory
        dataSourceFactory = new DefaultDataSourceFactory(context, 
            Util.getUserAgent(context, "DubExoPlayer"));
            
        // Set up player listener
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_READY:
                        if (exoPlayer.getPlayWhenReady()) {
                            OnVideoStarted();
                        }
                        break;
                    case Player.STATE_ENDED:
                        OnVideoCompleted();
                        break;
                }
            }
            
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    OnVideoResumed();
                } else {
                    OnVideoPaused();
                }
            }
            
            @Override
            public void onPlayerError(PlaybackException error) {
                OnError("Player Error: " + error.getMessage());
            }
        });
    }
    
    private void initView() {
        playerView = new PlayerView(context);
        playerView.setPlayer(exoPlayer);
        playerView.setUseController(true);
        
        // Set layout params
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        playerView.setLayoutParams(layoutParams);
        
        // Set the view
        getView().addView(playerView);
    }
    
    // Properties
    @SimpleProperty(description = "Get current video URL")
    public String VideoUrl() {
        return currentVideoUrl;
    }
    
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "Set video URL (MP4 or M3U8)")
    public void VideoUrl(String url) {
        currentVideoUrl = url;
        loadVideo(url);
    }
    
    @SimpleProperty(description = "Get current playback speed")
    public float PlaybackSpeed() {
        return playbackSpeed;
    }
    
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "1.0")
    @SimpleProperty(description = "Set playback speed (0.5x to 2.0x)")
    public void PlaybackSpeed(float speed) {
        if (speed >= 0.5f && speed <= 2.0f) {
            playbackSpeed = speed;
            if (exoPlayer != null) {
                exoPlayer.setPlaybackSpeed(speed);
            }
        }
    }
    
    @SimpleProperty(description = "Get selected video quality")
    public String Quality() {
        return selectedQuality;
    }
    
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Auto")
    @SimpleProperty(description = "Set video quality (Auto, 480p, 720p, 1080p)")
    public void Quality(String quality) {
        selectedQuality = quality;
        setVideoQuality(quality);
    }
    
    @SimpleProperty(description = "Check if video is in fullscreen mode")
    public boolean IsFullscreen() {
        return isFullscreen;
    }
    
    // Functions
    @SimpleFunction(description = "Load and play video from URL")
    public void LoadVideo(String url) {
        currentVideoUrl = url;
        loadVideo(url);
    }
    
    @SimpleFunction(description = "Start/Resume video playback")
    public void Play() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }
    
    @SimpleFunction(description = "Pause video playback")
    public void Pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }
    
    @SimpleFunction(description = "Stop video playback")
    public void Stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
    }
    
    @SimpleFunction(description = "Seek to specific position in seconds")
    public void SeekTo(long positionSeconds) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(positionSeconds * 1000);
        }
    }
    
    @SimpleFunction(description = "Toggle fullscreen mode")
    public void ToggleFullscreen() {
        isFullscreen = !isFullscreen;
        if (isFullscreen) {
            enterFullscreen();
        } else {
            exitFullscreen();
        }
    }
    
    @SimpleFunction(description = "Enter fullscreen mode")
    public void EnterFullscreen() {
        if (!isFullscreen) {
            isFullscreen = true;
            enterFullscreen();
        }
    }
    
    @SimpleFunction(description = "Exit fullscreen mode")
    public void ExitFullscreen() {
        if (isFullscreen) {
            isFullscreen = false;
            exitFullscreen();
        }
    }
    
    @SimpleFunction(description = "Download video to device storage")
    public void DownloadVideo(String url, String fileName) {
        if (url.isEmpty() || fileName.isEmpty()) {
            OnError("URL and filename cannot be empty");
            return;
        }
        
        downloadExecutor.execute(() -> {
            try {
                downloadVideoFile(url, fileName);
            } catch (Exception e) {
                OnError("Download failed: " + e.getMessage());
            }
        });
    }
    
    @SimpleFunction(description = "Get current playback position in seconds")
    public long GetCurrentPosition() {
        if (exoPlayer != null) {
            return exoPlayer.getCurrentPosition() / 1000;
        }
        return 0;
    }
    
    @SimpleFunction(description = "Get total video duration in seconds")
    public long GetDuration() {
        if (exoPlayer != null) {
            return exoPlayer.getDuration() / 1000;
        }
        return 0;
    }
    
    @SimpleFunction(description = "Check if video is currently playing")
    public boolean IsPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }
    
    // Private helper methods
    private void loadVideo(String url) {
        if (url.isEmpty() || exoPlayer == null) {
            return;
        }
        
        try {
            MediaSource mediaSource;
            Uri uri = Uri.parse(url);
            
            if (url.contains(".m3u8")) {
                // HLS stream
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
            } else {
                // Progressive download (MP4, etc.)
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri));
            }
            
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            
        } catch (Exception e) {
            OnError("Failed to load video: " + e.getMessage());
        }
    }
    
    private void setVideoQuality(String quality) {
        if (trackSelector == null) return;
        
        try {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setMaxVideoSizeSd()
                    .setForceHighestSupportedBitrate(quality.equals("Auto"))
            );
        } catch (Exception e) {
            Log.e(TAG, "Error setting quality: " + e.getMessage());
        }
    }
    
    private void enterFullscreen() {
        if (playerView != null) {
            playerView.getLayoutParams().height = LinearLayout.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            playerView.requestLayout();
        }
    }
    
    private void exitFullscreen() {
        if (playerView != null) {
            playerView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            playerView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            playerView.requestLayout();
        }
    }
    
    private void downloadVideoFile(String url, String fileName) throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = new File(downloadsDir, fileName);
        
        URL videoUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) videoUrl.openConnection();
        connection.connect();
        
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + connection.getResponseCode());
        }
        
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            int fileLength = connection.getContentLength();
            
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // Update progress
                if (fileLength > 0) {
                    int progress = (int) (totalBytesRead * 100 / fileLength);
                    OnDownloadProgress(progress);
                }
            }
            
            OnDownloadCompleted(outputFile.getAbsolutePath());
        }
    }
    
    // Events
    @SimpleEvent(description = "Fired when video starts playing")
    public void OnVideoStarted() {
        EventDispatcher.dispatchEvent(this, "OnVideoStarted");
    }
    
    @SimpleEvent(description = "Fired when video is paused")
    public void OnVideoPaused() {
        EventDispatcher.dispatchEvent(this, "OnVideoPaused");
    }
    
    @SimpleEvent(description = "Fired when video is resumed")
    public void OnVideoResumed() {
        EventDispatcher.dispatchEvent(this, "OnVideoResumed");
    }
    
    @SimpleEvent(description = "Fired when video playback is completed")
    public void OnVideoCompleted() {
        EventDispatcher.dispatchEvent(this, "OnVideoCompleted");
    }
    
    @SimpleEvent(description = "Fired when an error occurs")
    public void OnError(String error) {
        EventDispatcher.dispatchEvent(this, "OnError", error);
    }
    
    @SimpleEvent(description = "Fired during video download with progress percentage")
    public void OnDownloadProgress(int progress) {
        EventDispatcher.dispatchEvent(this, "OnDownloadProgress", progress);
    }
    
    @SimpleEvent(description = "Fired when video download is completed")
    public void OnDownloadCompleted(String filePath) {
        EventDispatcher.dispatchEvent(this, "OnDownloadCompleted", filePath);
    }
    
    // Cleanup
    @Override
    public void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (downloadExecutor != null) {
            downloadExecutor.shutdown();
        }
        super.onDestroy();
    }
}
