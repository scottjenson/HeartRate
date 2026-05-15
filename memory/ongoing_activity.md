---
name: ongoing-activity
description: Three things all required together for the Ongoing Activity chip to appear on the watch face
metadata:
  type: project
---

The watch face chip only appears when ALL THREE of these are present:

1. `setLocusId(LocusIdCompat("hr_session"))` on BOTH `NotificationCompat.Builder` AND `OngoingActivity.Builder`
2. `setCategory("workout")` on `NotificationCompat.Builder`
3. `setLocusContext(LocusId("hr_session"), null)` called in `MainActivity.onCreate()`

Missing any one of these silently prevents the chip from showing.

**Why:** Discovered by trial and error — the system uses all three to correlate the notification, the ongoing activity, and the app.

**How to apply:** If the watch face chip stops appearing after changes to the service or activity, check that all three are still in place.
