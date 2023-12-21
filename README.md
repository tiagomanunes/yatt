# Yet Another Time Tracker, that needs another name

Plan your day, note what actually happens, analyse the delta.

## Instructions

1. On launch, your schedule for today will be presented. To work on another day, use the date-picker.
2. Plan a new activity by clicking the + button next to “Planned”, or by pressing the P key[^1].
    1. In the dialog, enter a name and a category for the activity, as well as a start time and duration[^2]. These last two are set using the UP and DOWN keys to modify time in 15-minute increments.
    2. New categories can be created here as well.
    3. Sub-categories can be emulated by using free-text tags, e.g. “interview” tag in “Hiring” category.
    4. Clicking the Next button will record the activity and clear the dialog for a new activity to be entered immediately. Clicking the OK button will record the activity and close the dialog.
    5. Right-clicking on any box on the schedule will open the same dialog, allowing the activity to be edited.
3. Log work done by clicking the + button next to “Done”, or by pressing the D key.
    1. The dialog works the same way[^3].
    2. Editing activities also works the same way.
4. The report button will plot the planned vs actual time per category for a given time period.
    1. Hovering the mouse over a category bar provides a tooltip with a breakdown of time per tag.

[^1]: A key feature (pun unintended) is the ability to interact with the tool using the keyboard only. Keep it running throughout the day, Alt-Tab into the tool, hit D to log another piece of work, Tab between the form fields as you enter the details, Enter to submit and Alt-Tab away.

[^2]: The first activity to be planned will have a default start time of 8AM. The default activity duration is of 60 minutes. The next activity to be planned will have a default start time of the previous activity planned start time plus its duration.

[^3]: When logging work done, the start time will default to the nearest next 15-minute time block start (this is arguably less efficient than defaulting to the end of the last activity - an opportunity for improvement).

## Technology

Powered by Java, JavaFX, ORMLite and SQLite.
