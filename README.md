# Roposo Social Hack (Venturesity) Submission
## Team Members: Sushrith (lolster) and Anush (anush)

### Instruction for use
+ Select the video file (mp4 only) using the `Select Video` button.
+ Select the audio file (AAC only) using the `Select Audio` button.
+ Press the save `Save` button to save the new video file. The new video file
will be saved in the root directory of the external storage, named `ouput.mp4`.

### Explanation:
The app uses MediaMuxer from the Android API to mux the audio and video files
together, and MediaExtractor to extract the audio and video data.

The result video file will be as long as the original video or audio file.
