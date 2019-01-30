## Overview
- This app is based on the idea of video cropping and trimming in an advance manner.
- A sample dummy video is fetched from app's raw folder and then trim and cut operations are added on it.
- Video is sliced in an interval and placed in a horizontal RecyclerView and as Bitmap images, you can then scroll to seek to see the current video position.
- User can place a start time by tapping Start Here button and also place an end time by tapping End Here button.
- Then user can do operations like Trim and Cut (Trim is default, you can change current functionality by pressing Trim and or Cut button from top bar).
- Finally after compressing videos will be saved in **advance_vid_cutter** of your device external storage.
- This app only uses FFmpeg to trim and merge videos, other than this no library is used and everything else is built from scratch.
- This app takes help from some open codes (specially utility classes).
- Playing speed of video between VideoView and horizontally scrolling recyclerview is perfectly synchronized.
- From Github and other open source platform we couldn't find any libray or source this much close to our implemented features. 
- App UI is maintained as close as possible to the provided mockups.

## Known issues
- Start time and end time scrubbers are not placed precisely i.e. they will take room beside of any bitmap tile from RecyclerView of the bottom.
- Only dissimilarity between provided mockup UI and app UI is the absense of gray overlay.
- When you cut and merge videos (cut functionality from top bar) all the video are still present in advance_vid_cutter folder. Future task: after finishing the merge succcessfully we need to remove all those redundant videos from that folder which is not implemented yet.
- Sometimes start button scrubber and end button scrubber may produce some UI related bugs

## Tested devices
- This app is tested well in: Samsung Galaxt J5, OnePlus 3, Xiaomi Redme Note 6 etc.
