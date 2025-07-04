# DubExoPlayer Extension

Advanced ExoPlayer extension for MIT App Inventor / Kodular / Niotron with comprehensive video playback features.

## 📱 Features

### ✅ Video Playback
- **MP4 Support**: Play standard MP4 video files
- **M3U8/HLS Support**: Stream HLS content seamlessly
- **Auto-start**: Videos start playing automatically when loaded
- **Smooth Playback**: Optimized for buffer-free experience

### ✅ User Interface
- **Fullscreen Mode**: Toggle between normal and fullscreen view
- **Player Controls**: Built-in play/pause/seek controls
- **Quality Selection**: Choose from Auto, 480p, 720p, 1080p
- **Speed Control**: Adjust playback speed (0.5x to 2.0x)

### ✅ Advanced Features
- **Download Videos**: Save videos to device storage
- **Progress Tracking**: Monitor download progress
- **Event Handling**: Comprehensive event system
- **Error Handling**: Robust error management

## 📋 Installation

1. Download the `DubExoPlayer.aix` file
2. Open MIT App Inventor / Kodular / Niotron
3. Go to Extensions and import the `.aix` file
4. Add DubExoPlayer component to your project

## 🔧 Usage

### Basic Setup

```blocks
// Initialize with video URL
DubExoPlayer.VideoUrl = "https://example.com/video.mp4"

// Or load video dynamically
DubExoPlayer.LoadVideo("https://example.com/stream.m3u8")
```

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `VideoUrl` | String | Get/Set video URL (MP4 or M3U8) |
| `PlaybackSpeed` | Float | Get/Set playback speed (0.5-2.0) |
| `Quality` | String | Get/Set video quality (Auto/480p/720p/1080p) |
| `IsFullscreen` | Boolean | Check if in fullscreen mode |

### Functions

| Function | Parameters | Description |
|----------|------------|-------------|
| `LoadVideo` | url: String | Load and play video from URL |
| `Play` | - | Start/Resume playback |
| `Pause` | - | Pause playback |
| `Stop` | - | Stop playback |
| `SeekTo` | positionSeconds: Long | Seek to specific position |
| `ToggleFullscreen` | - | Switch fullscreen mode |
| `EnterFullscreen` | - | Enter fullscreen mode |
| `ExitFullscreen` | - | Exit fullscreen mode |
| `DownloadVideo` | url: String, fileName: String | Download video to storage |
| `GetCurrentPosition` | - | Get current playback position (seconds) |
| `GetDuration` | - | Get total video duration (seconds) |
| `IsPlaying` | - | Check if video is playing |

### Events

| Event | Parameters | Description |
|-------|------------|-------------|
| `OnVideoStarted` | - | Fired when video starts playing |
| `OnVideoPaused` | - | Fired when video is paused |
| `OnVideoResumed` | - | Fired when video is resumed |
| `OnVideoCompleted` | - | Fired when video finishes |
| `OnError` | error: String | Fired when error occurs |
| `OnDownloadProgress` | progress: Integer | Fired during download (0-100%) |
| `OnDownloadCompleted` | filePath: String | Fired when download completes |

## 💡 Examples

### Basic Video Player

```blocks
// Set video URL
DubExoPlayer.VideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

// Handle events
when DubExoPlayer.OnVideoStarted do
  Label1.Text = "Video Started"
end

when DubExoPlayer.OnError do
